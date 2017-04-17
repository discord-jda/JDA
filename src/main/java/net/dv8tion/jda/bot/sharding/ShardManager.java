package net.dv8tion.jda.bot.sharding;

import com.mashape.unirest.http.Unirest;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.http.util.Args;

public class ShardManager // TODO: think about what methods ShardManager should contain
{
    private final JDABuilder builder;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r ->
    {
        final Thread t = new Thread(r, "ShardManager");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY + 1);
        return t;
    });
    private final int maxShardId;
    private final int minShardId;
    private final int numShards;
    private final Queue<Integer> queue = new ConcurrentLinkedQueue<>();
    private final TIntObjectMap<JDAImpl> shards;
    private final int shardsTotal;
    private TIntObjectMap<JDAImpl> shardsUnmodifiable;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private ScheduledFuture<?> worker;

    ShardManager(final JDABuilder builder, final int shardsTotal, final int lowerBound, final int upperBound)
    {
        Args.notNull(builder, "builder");
        Args.positive(shardsTotal, "shardsTotal");
        Args.check(!(lowerBound == -1 ^ upperBound == -1),
                "Huh, how could this happen? only one of lowerBound or upperBound is -1");

        this.builder = builder;

        if (lowerBound == -1 && upperBound == -1)
        {
            this.minShardId = 0;
            this.maxShardId = shardsTotal - 1;
        }
        else
        {
            this.minShardId = lowerBound;
            this.maxShardId = upperBound;
        }

        this.shardsTotal = shardsTotal;
        this.numShards = upperBound - lowerBound + 1;

        this.shards = new TIntObjectHashMap<>(this.numShards);

        for (int i = this.minShardId; i <= this.maxShardId; i++)
            this.queue.offer(i);
    }

    /**
     * Adds all provided listeners to the event-listeners that will be used to handle events.
     * This uses the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * To switch to the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager}, use {@link #setEventManager(IEventManager)}.
     *
     * Note: when using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) which will react to events.
     */
    public void addEventListener(final Object... listeners)
    {
        this.shards.valueCollection().forEach(jda -> jda.addEventListener(listeners));
    }

    /**
     * Used to access Bot specific functions like OAuth information.
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         Thrown if the currently logged in account is {@link net.dv8tion.jda.core.AccountType#CLIENT}
     *
     * @return The {@link net.dv8tion.jda.bot.JDABot} registry for this instance of JDA.
     */
    public RestAction<ApplicationInfo> getApplicationInfo()
    {
        return this.shards.valueCollection().stream().findAny()
                .orElseThrow(() -> new IllegalStateException("no active shards")).asBot().getApplicationInfo();
    }

    /**
     * The average time in milliseconds between all shards that discord took to respond to our last heartbeat.
     * <br>This roughly represents the WebSocket ping of this session.
     *
     * <p><b>{@link net.dv8tion.jda.core.requests.RestAction RestAction} request times do not
     * correlate to this value!</b>
     *
     * @return time in milliseconds between heartbeat and the heartbeat ack response
     */
    public double getAveragePing()
    {
        return this.shards.valueCollection().stream().mapToLong(jda -> jda.getPing()).average().getAsDouble();
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild} with matching id.
     */
    public Guild getGuildById(final long id)
    {
        return this.getStream(jda -> jda.getGuildMap().valueCollection().stream()).filter(g -> g.getIdLong() == id)
                .findFirst().orElse(null);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild} with matching id.
     */
    public Guild getGuildById(final String id)
    {
        return this.getGuildById(Long.parseLong(id));
    }

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.core.entities.Guild Guilds} that the logged account is connected to.
     * <br>If this account is not connected to any {@link net.dv8tion.jda.core.entities.Guild Guilds}, this will return
     * an empty list.
     *
     * <p>If the developer is sharding ({@link net.dv8tion.jda.core.JDABuilder#useSharding(int, int)},
     * then this list will only contain the {@link net.dv8tion.jda.core.entities.Guild Guilds} that the shard is
     * actually connected to. Discord determines which guilds a shard is connect to using the following format:
     * <br>Guild connected if shardId == (guildId {@literal >>} 22) % totalShards;
     * <br>Source for formula: <a href="https://discordapp.com/developers/docs/topics/gateway#sharding">Discord Documentation</a>
     *
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.Guild Guilds} that this account is connected to.
     */
    public List<Guild> getGuilds()
    {
        return Collections.unmodifiableList(this.getStream(jda -> jda.getGuildMap().valueCollection().stream())
                .distinct().collect(Collectors.toList()));
    }

    public JDA getShard(final int shardId)
    {
        Args.positive(shardId, "shardId");
        Args.check(shardId < this.shardsTotal, "shardId may not be higher than shardsTotal");

        return this.shards.get(shardId);
    }

    public Collection<? extends JDA> getShards()
    {
        if (this.shardsUnmodifiable == null)
            this.shardsUnmodifiable = TCollections.unmodifiableMap(this.shards);

        return this.shardsUnmodifiable.valueCollection();
    }

    /**
     * Gets the current {@link net.dv8tion.jda.core.JDA.Status Status} of the shard.
     *
     * @return Current shard status.
     */
    public JDA.Status getStatus(final int shardId)
    {
        Args.positive(shardId, "shardId");
        Args.check(shardId < this.shardsTotal, "shardId may not be higher than shardsTotal");

        return this.shards.get(shardId).getStatus();
    }

    /**
     * Gets the current {@link net.dv8tion.jda.core.JDA.Status Status} of all shards.
     *
     * @return Current shard statuses.
     */
    public List<JDA.Status> getStatuses()
    {
        return Collections.unmodifiableList(
                this.shards.valueCollection().stream().map(jda -> jda.getStatus()).collect(Collectors.toList()));
    }

    private <T> Stream<T> getStream(final Function<? super JDAImpl, ? extends Stream<? extends T>> mapper)
    {
        return this.shards.valueCollection().stream().flatMap(mapper);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with matching id.
     */
    public TextChannel getTextChannelById(final long id)
    {
        return this.getStream(jda -> jda.getTextChannelMap().valueCollection().stream())
                .filter(g -> g.getIdLong() == id).findFirst().orElse(null);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with matching id.
     */
    public TextChannel getTextChannelById(final String id)
    {
        return this.getTextChannelById(Long.parseLong(id));
    }

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} of all connected
     * {@link net.dv8tion.jda.core.entities.Guild Guilds}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present in this list does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is possible that you will see fewer channels than this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @return Possibly-empty list of all known {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     */
    public List<TextChannel> getTextChannels()
    {
        return Collections.unmodifiableList(
                this.getStream(jda -> jda.getTextChannelMap().valueCollection().stream()).collect(Collectors.toList()));
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.core.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.User User} with matching id.
     */
    public User getUserById(final long id)
    {
        return this.getStream(jda -> jda.getUserMap().valueCollection().stream()).filter(g -> g.getIdLong() == id)
                .findFirst().orElse(null);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.core.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.User User} with matching id.
     */
    public User getUserById(final String id)
    {
        return this.getUserById(Long.parseLong(id));
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.User Users} that share a
     * {@link net.dv8tion.jda.core.entities.Guild Guild} with the currently logged in account.
     * <br>This list will never contain duplicates and represents all {@link net.dv8tion.jda.core.entities.User Users}
     * that JDA can currently see.
     *
     * <p>If the developer is sharding, then only users from guilds connected to the specifically logged in
     * shard will be returned in the List.
     *
     * @return List of all {@link net.dv8tion.jda.core.entities.User Users} that are visible to JDA.
     */
    public List<User> getUsers() // TODO: think about how resource intensive this is on large bots (over 1-2M users)
    {
        return Collections.unmodifiableList(this.getStream(jda -> jda.getUserMap().valueCollection().stream())
                .distinct().collect(Collectors.toList()));
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     */
    public VoiceChannel getVoiceChannelById(final long id)
    {
        return this.getStream(jda -> jda.getVoiceChannelMap().valueCollection().stream())
                .filter(g -> g.getIdLong() == id).findFirst().orElse(null);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     */
    public VoiceChannel getVoiceChannelById(final String id)
    {
        return this.getVoiceChannelById(Long.parseLong(id));
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} of all connected
     * {@link net.dv8tion.jda.core.entities.Guild Guilds}.
     *
     * @return Possible-empty list of all known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     */
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(this.getStream(jda -> jda.getVoiceChannelMap().valueCollection().stream())
                .collect(Collectors.toList()));
    }

    void login() throws LoginException, IllegalArgumentException
    {
        // building the first one in the currrent thread ensures that LoginException and IllegalArgumentException can be thrown on login
        JDAImpl jda = null;
        try
        {
            final int shardId = this.queue.peek();
            this.builder.useSharding(shardId, this.shardsTotal);
            this.shards.put(shardId, jda = (JDAImpl) this.builder.buildAsync());
            this.queue.remove(shardId);
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

                this.builder.useSharding(shardId, this.shardsTotal);
                this.shards.put(shardId, api = (JDAImpl) this.builder.buildAsync());
                this.queue.remove(shardId);
            }
            catch (LoginException | IllegalArgumentException e)
            {
                // TODO: this should never happen unless the token changes inbetween
                e.printStackTrace();
            }
            catch (final RateLimitedException e)
            {
                // do not remove 'shardId' from the queue and try the current one again after 5 seconds
            }
            catch (final Exception e)
            {
                if (api != null)
                    api.shutdown(false);
                throw e;
            }
        }, 0, 5, TimeUnit.SECONDS);

    }

    /**
     * Removes all provided listeners from the event-listeners and no longer uses them to handle events.
     *
     * @param  listeners
     *         The listener(s) to be removed.
     */
    public void removeEventListener(final Object... listeners)
    {
        this.shards.valueCollection().forEach(jda -> jda.removeEventListener(listeners));
    }

    public void restart(final int shardId)
    {
        Args.check(shardId >= this.minShardId, "shardId must not be less than minShardId");
        Args.check(shardId <= this.maxShardId, "shardId must not be greater than maxShardId");

        final JDAImpl old = this.shards.remove(shardId);
        if (old != null)
            old.shutdown(false);

        this.queue.offer(shardId);
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for all sessions.
     * <br>A Game can be retrieved via {@link net.dv8tion.jda.core.entities.Game#of(String)}.
     * For streams you provide a valid streaming url as second parameter
     *
     * @param  game
     *         A {@link net.dv8tion.jda.core.entities.Game Game} instance or null to reset
     *
     * @see    net.dv8tion.jda.core.entities.Game#of(String)
     * @see    net.dv8tion.jda.core.entities.Game#of(String, String)
     */
    public void setGame(final Game game)
    {
        this.shards.valueCollection().forEach(jda -> jda.getPresence().setGame(game));
    }

    /**
     * Sets whether all sessions should be marked as afk or not
     *
     * <p>This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     * @param idle
     *        boolean
     */
    public void setIdle(final boolean idle)
    {
        this.shards.valueCollection().forEach(jda -> jda.getPresence().setIdle(idle));
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for all sessions
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @param  status
     *         the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     */
    public void setStatus(final OnlineStatus status)
    {
        this.shards.valueCollection().forEach(jda -> jda.getPresence().setStatus(status));
    }

    /**
     * Shuts down all JDA shards, closing all their connections.
     * After this command is issued the ShardManager instance can not be used anymore.
     *
     * <p>This is the same as calling {@link #shutdown(boolean) shutdown(true)}.
     */
    public void shutdown()
    {
        this.shutdown(true);
    }

    /**
     * Shuts down all JDA shards, closing all their connections.
     * After this command is issued the ShardManager instance can not be used anymore.
     *
     * <p>Depending on the value of {@code free}, this will also close the background-thread used for requests by Unirest.
     * <br>If the background-thread is closed, the system can exit properly, but no further JDA requests are possible (includes other JDA instances).
     * If you want to create any new instances or if you have any other instances running in parallel, then {@code free}
     * should be set to false.
     *
     * @param  free If true, shuts down JDA's rest system permanently for all current and future instances.
     */
    public void shutdown(final boolean free)
    {
        if (this.shutdown.getAndSet(true))
            return; // shutdown has already been requested

        if (this.worker != null && !this.worker.isDone())
            this.worker.cancel(true);

        this.executor.shutdownNow();

        if (this.shards != null)
            for (final JDA jda : this.shards.valueCollection()) // TODO: decide weather this should be done in parallel
                jda.shutdown(false);

        // shutdown Unirest after all JDA instances (if requested) so they can still make rest calls
        if (free)
            try
            {
                Unirest.shutdown();
            }
            catch (final IOException ignored)
            {}
    }
}
