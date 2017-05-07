/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.bot.entities.impl;

import com.mashape.unirest.http.Unirest;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.bot.sharding.ShardManagerBuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.apache.http.util.Args;

public class ShardManagerImpl implements ShardManager
{
    protected final IAudioSendFactory audioSendFactory;

    protected final boolean autoReconnect;
    protected final int corePoolSize;
    protected final boolean enableBulkDeleteSplitting;
    protected final boolean enableVoice;
    protected final IEventManager eventManager;
    protected final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r ->
    {
        final Thread t = new Thread(r, "ShardManagerImpl");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY + 1);
        return t;
    });
    protected final Game game;
    protected final boolean idle;
    protected final List<Object> listeners;
    protected final int maxReconnectDelay;
    protected final Queue<Integer> queue = new ConcurrentLinkedQueue<>();
    protected TIntObjectMap<JDAImpl> shards = new TIntObjectHashMap<>();
    protected int shardsTotal;

    protected final AtomicBoolean shutdown = new AtomicBoolean(false);
    protected final Thread shutdownHook;
    protected final OnlineStatus status;
    protected final String token;
    protected final int websocketTimeout;

    protected ScheduledFuture<?> worker;

    public ShardManagerImpl(final int shardsTotal, final TIntSet shardIds, // disgusting
            final List<Object> listeners, final String token, final IEventManager eventManager,
            final IAudioSendFactory audioSendFactory, final Game game, final OnlineStatus status,
            final int websocketTimeout, final int maxReconnectDelay, final int corePoolSize, final boolean enableVoice,
            final boolean enableShutdownHook, final boolean enableBulkDeleteSplitting, final boolean autoReconnect,
            final boolean idle)
    {
        this.shardsTotal = shardsTotal;
        this.listeners = listeners;
        this.token = token;
        this.eventManager = eventManager;
        this.audioSendFactory = audioSendFactory;
        this.game = game;
        this.status = status;
        this.websocketTimeout = websocketTimeout;
        this.maxReconnectDelay = maxReconnectDelay;
        this.corePoolSize = corePoolSize;
        this.enableVoice = enableVoice;
        this.shutdownHook = enableShutdownHook ? new Thread(() -> ShardManagerImpl.this.shutdown(true), "JDA Shutdown Hook") : null;;
        this.enableBulkDeleteSplitting = enableBulkDeleteSplitting;
        this.autoReconnect = autoReconnect;
        this.idle = idle;

        if (shardsTotal != -1)
        {
            if (shardIds == null)
            {
                this.shards = new TIntObjectHashMap<>(shardsTotal);
                for (int i = 0; i < this.shardsTotal; i++)
                    queue.offer(i);
            }
            else
            {
                this.shards = new TIntObjectHashMap<>(shardIds.size());
                shardIds.forEach(queue::offer);
            }
        }
    }

    @Override
    public void addEventListener(final Object... listeners)
    {
        this.shards.valueCollection().forEach(jda -> jda.addEventListener(listeners));
    }

    @Override
    public RestAction<ApplicationInfo> getApplicationInfo()
    {
        return this.shards.valueCollection().stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("no active shards"))
                .asBot().getApplicationInfo();
    }

    @Override
    public double getAveragePing()
    {
        return this.shards.valueCollection().stream()
                .mapToLong(jda -> jda.getPing())
                .filter(ping -> ping != -1)
                .average()
                .getAsDouble();
    }

    @Override
    public Guild getGuildById(final long id)
    {
        return this.findSnowflakeInCombinedCollection(jda -> jda.getGuildMap().valueCollection(), id);
    }

    @Override
    public Guild getGuildById(final String id)
    {
        return this.findSnowflakeInCombinedCollection(jda -> jda.getGuildMap().valueCollection(), id);
    }

    @Override
    public List<Guild> getGuilds()
    {
        return this.getDistinctUnmodifiableCombinedList(api -> api.getGuildMap().valueCollection());
    }

    @Override
    public List<Guild> getMutualGuilds(final Collection<User> users)
    {
        Args.notNull(users, "users");
        for (final User u : users)
            Args.notNull(u, "All users");

        return Collections.unmodifiableList(
                this.getCombinedStream(jda -> jda.getGuildMap().valueCollection())
                .filter(guild -> users.stream().allMatch(guild::isMember))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Guild> getMutualGuilds(final User... users)
    {
        Args.notNull(users, "users");
        return this.getMutualGuilds(Arrays.asList(users));
    }

    @Override
    public List<Guild> getMutualGuilds(final User user)
    {
        Args.notNull(user, "user");
        return Collections.unmodifiableList(
                this.getCombinedStream(jda -> jda.getGuildMap().valueCollection())
                    .filter(guild -> guild.isMember(user))
                    .collect(Collectors.toList()));
    }

    @Override
    public JDA getShard(final int shardId)
    {
        return this.shards.get(shardId);
    }

    @Override
    public Collection<JDA> getShards()
    {
        return Collections.unmodifiableCollection(new ArrayList<>(this.shards.valueCollection()));
    }

    @Override
    public int getShardsCount()
    {
        return this.shards.size() + queue.size();
    }

    @Override
    public int getShardsTotal()
    {
        return this.shardsTotal;
    }

    @Override
    public JDA.Status getStatus(final int shardId)
    {
        final JDA api = this.shards.get(shardId);
        return api == null ? null : api.getStatus();
    }

    @Override
    public List<JDA.Status> getStatuses()
    {
        return this.getUnmodifiableList(jda -> jda.getStatus());
    }

    @Override
    public TextChannel getTextChannelById(final long id)
    {
        return this.findSnowflakeInCombinedCollection(jda -> jda.getTextChannelMap().valueCollection(), id);
    }

    @Override
    public TextChannel getTextChannelById(final String id)
    {
        return this.findSnowflakeInCombinedCollection(jda -> jda.getTextChannelMap().valueCollection(), id);
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        return this.getDistinctUnmodifiableCombinedList(api -> api.getTextChannelMap().valueCollection());
    }

    @Override
    public User getUserById(final long id)
    {
        return this.findSnowflakeInCombinedCollection(jda -> jda.getUserMap().valueCollection(), id);

    }

    @Override
    public User getUserById(final String id)
    {
        return this.findSnowflakeInCombinedCollection(jda -> jda.getUserMap().valueCollection(), id);
    }

    @Override
    public List<User> getUsers()
    {
        return this.getDistinctUnmodifiableCombinedList(api -> api.getUserMap().valueCollection());

    }

    @Override
    public VoiceChannel getVoiceChannelById(final long id)
    {
        return this.findSnowflakeInCombinedCollection(jda -> jda.getVoiceChannelMap().valueCollection(), id);
    }

    @Override
    public VoiceChannel getVoiceChannelById(final String id)
    {
        return this.findSnowflakeInCombinedCollection(jda -> jda.getVoiceChannelMap().valueCollection(), id);
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return this.getDistinctUnmodifiableCombinedList(api -> api.getVoiceChannelMap().valueCollection());
    }

    public void login() throws LoginException, IllegalArgumentException
    {
        // building the first one in the currrent thread ensures that LoginException and IllegalArgumentException can be thrown on login
        JDAImpl jda = null;
        try
        {
            if (this.queue.isEmpty())
            {
                jda = this.buildInstance(0);

                this.shards = new TIntObjectHashMap<>(shardsTotal);
                for (int i = 0; i < this.shardsTotal; i++)
                    queue.offer(i);

                this.shards.put(0, jda);
            }
            else
            {
                final int shardId = this.queue.peek();

                this.shards.put(shardId, jda = this.buildInstance(shardId));
                this.queue.remove(shardId);
            }
        }
        catch (final RateLimitedException e)
        {
            // do not remove 'shardId' from the queue and try the first one again after 5 seconds in the async thread
        }
        catch (final Exception e)
        {
            if (jda != null)
                jda.shutdown(false);
            throw e;
        }

        this.worker = this.executor.scheduleAtFixedRate(() ->
        {
            if (this.queue.isEmpty())
                return;

            JDAImpl api = null;
            try
            {
                int shardId = -1;
                do
                {
                    final int i = this.queue.peek();
                    if (this.shards.containsKey(i))
                        this.queue.poll();
                    else
                        shardId = i;
                }
                while (shardId == -1 && !this.queue.isEmpty());

                if (shardId == -1)
                {
                    this.queue.poll();
                    return;
                }

                this.shards.put(shardId, api = this.buildInstance(shardId));
                this.queue.remove(shardId);
            }
            catch (LoginException | IllegalArgumentException e)
            {
                // TODO: this should never happen unless the token changes between, still needs to be handled somehow
                e.printStackTrace();
            }
            catch (final Exception e)
            {
                // do not remove 'shardId' from the queue and try again after 5 seconds
                if (api != null)
                    api.shutdown(false);
                throw new RuntimeException(e);
            }
        }, 0, 5, TimeUnit.SECONDS);

        if (this.shutdownHook != null)
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    @Override
    public void removeEventListener(final Object... listeners)
    {
        this.shards.valueCollection().forEach(jda -> jda.removeEventListener(listeners));
    }

    @Override
    public void restart()
    {
        for (int shardId : this.shards.keys())
        {
            final JDAImpl jda = this.shards.remove(shardId);
            if (jda != null)
            {
                jda.shutdown(false);
                this.queue.offer(shardId);
            }
        }
    }

    @Override
    public void restart(final int shardId)
    {
        final JDAImpl jda = this.shards.remove(shardId);
        if (jda != null)
        {
            jda.shutdown(false);
            this.queue.offer(shardId);
        }
    }

    @Override
    public void setGame(final Game game)
    {
        this.shards.valueCollection().forEach(jda -> jda.getPresence().setGame(game));
    }

    @Override
    public void setIdle(final boolean idle)
    {
        this.shards.valueCollection().forEach(jda -> jda.getPresence().setIdle(idle));
    }

    @Override
    public void setStatus(final int shardId, final OnlineStatus status)
    {
        final JDA api = this.shards.get(shardId);
        if (api != null)
            api.getPresence().setStatus(status);
    }

    @Override
    public void setStatus(final OnlineStatus status)
    {
        this.shards.valueCollection().forEach(jda -> jda.getPresence().setStatus(status));
    }

    @Override
    public void shutdown()
    {
        this.shutdown(true);
    }

    @Override
    public void shutdown(final boolean free)
    {
        if (this.shutdown.getAndSet(true))
            return; // shutdown has already been requested

        if (this.worker != null && !this.worker.isDone())
            this.worker.cancel(true);

        if (this.shutdownHook != null)
            try
            {
                Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
            }
            catch (final Exception ignored) {}

        this.executor.shutdownNow();

        if (this.shards != null)
            for (final JDA jda : this.shards.valueCollection())
                jda.shutdown(false);

        // shutdown Unirest after all JDA instances (if requested) so they can still make rest calls while shutting down
        if (free)
            try
            {
                Unirest.shutdown();
            }
            catch (final IOException ignored)
            {}
    }

    @Override
    public void shutdown(final int shardId)
    {
        final JDAImpl jda = this.shards.remove(shardId);
        if (jda != null)
        {
            jda.shutdown(false);
        }
    }

    @Override
    public void start(final int shardId)
    {
        Args.notNegative(shardId, "shardId");
        Args.check(shardId < this.shardsTotal, "shardId must be lower than shardsTotal");
        this.queue.offer(shardId);
    }

    protected JDAImpl buildInstance(final int shardId) throws LoginException, RateLimitedException
    {
        final WebSocketFactory wsFactory = new WebSocketFactory();
        wsFactory.setConnectionTimeout(this.websocketTimeout);
        if (ShardManagerBuilder.proxy != null)
        {
            final ProxySettings settings = wsFactory.getProxySettings();
            settings.setHost(ShardManagerBuilder.proxy.getHostName());
            settings.setPort(ShardManagerBuilder.proxy.getPort());
        }

        final JDAImpl jda = new JDAImpl(AccountType.BOT, ShardManagerBuilder.proxy, wsFactory, this.autoReconnect,
                this.enableVoice, false, this.enableBulkDeleteSplitting, this.corePoolSize, this.maxReconnectDelay);

        if (this.eventManager != null)
            jda.setEventManager(this.eventManager);

        if (this.audioSendFactory != null)
            jda.setAudioSendFactory(this.audioSendFactory);

        this.listeners.forEach(jda::addEventListener);
        jda.setStatus(JDA.Status.INITIALIZED); //This is already set by JDA internally, but this is to make sure the listeners catch it.

        // Set the presence information before connecting to have the correct information ready when sending IDENTIFY
        ((PresenceImpl) jda.getPresence()).setCacheGame(this.game).setCacheIdle(this.idle).setCacheStatus(this.status);

        final JDAImpl.ShardInfoImpl shardInfo = new JDAImpl.ShardInfoImpl(shardId, this.shardsTotal);

        final int shardTotal = jda.login(this.token, shardInfo);
        if (this.shardsTotal == -1)
            this.shardsTotal = shardTotal;
        return jda;
    }

    protected <T extends ISnowflake> T findSnowflakeInCombinedCollection(final Function<JDAImpl, ? extends Collection<? extends T>> mapper, final long id)
    {
        return this.getCombinedStream(mapper).filter(g -> g.getIdLong() == id).findFirst().orElse(null);
    }

    protected <T extends ISnowflake> T findSnowflakeInCombinedCollection(final Function<JDAImpl, ? extends Collection<? extends T>> mapper, final String id)
    {
        return findSnowflakeInCombinedCollection(mapper, MiscUtil.parseSnowflake(id));
    }

    protected <T> Stream<T> getCombinedStream(final Function<JDAImpl, ? extends Collection<? extends T>> mapper)
    {
        return this.shards.valueCollection().stream().flatMap(mapper.andThen(c -> c.stream()));
    }

    protected <T> List<T> getDistinctUnmodifiableCombinedList(final Function<JDAImpl, ? extends Collection<? extends T>> mapper)
    {
        return Collections.unmodifiableList(this.getCombinedStream(mapper).distinct().collect(Collectors.toList()));
    }

    protected <T> Stream<T> getStream(final Function<JDAImpl, ? extends T> mapper)
    {
        return this.shards.valueCollection().stream().map(mapper);
    }

    protected <T> List<T> getUnmodifiableList(final Function<JDAImpl, ? extends T> mapper)
    {
        return Collections.unmodifiableList(this.getStream(mapper).collect(Collectors.toList()));
    }
}
