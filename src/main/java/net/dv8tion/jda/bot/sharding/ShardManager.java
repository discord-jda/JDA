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
package net.dv8tion.jda.bot.sharding;

import java.util.Collection;
import java.util.List;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.requests.RestAction;

public interface ShardManager
{

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
    void addEventListener(Object... listeners);

    /**
     * Used to access Bot specific functions like OAuth information.
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         Thrown if the currently logged in account is {@link net.dv8tion.jda.core.AccountType#CLIENT}
     *
     * @return The {@link net.dv8tion.jda.bot.JDABot} registry for this instance of JDA.
     */
    RestAction<ApplicationInfo> getApplicationInfo();

    /**
     * The average time in milliseconds between all shards that discord took to respond to our last heartbeat.
     * <br>This roughly represents the WebSocket ping of this session.
     *
     * <p><b>{@link net.dv8tion.jda.core.requests.RestAction RestAction} request times do not
     * correlate to this value!</b>
     *
     * @return time in milliseconds between heartbeat and the heartbeat ack response
     */
    double getAveragePing();

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild} with matching id.
     */
    Guild getGuildById(long id);

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild} with matching id.
     */
    Guild getGuildById(String id);

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.core.entities.Guild Guilds} that the logged account is connected to.
     * <br>If this account is not connected to any {@link net.dv8tion.jda.core.entities.Guild Guilds}, this will return
     * an empty list.
     *
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.Guild Guilds} that this account is connected to.
     */
    List<Guild> getGuilds();

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param users
     *        The users which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.core.entities.User Users} in them.
     */
    List<Guild> getMutualGuilds(Collection<User> users);

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param  users
     *         The users which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.core.entities.User Users} in them.
     */
    List<Guild> getMutualGuilds(User... users);

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param  users
     *         The users which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.core.entities.User Users} in them.
     */
    List<Guild> getMutualGuilds(User user);

    /**
     * This returns the {@link net.dv8tion.jda.core.JDA JDA} instance which has the same id as the one provided.
     * <br>If there is no shard with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  shardId
     *         The id of the shard.
     *
     * @return The {@link net.dv8tion.jda.core.JDA JDA} instance with the given shardId or
     *         {@code null} if no shard has the given id
     */
    JDA getShard(int shardId);

    Collection<? extends JDA> getShards();

    /**
     * Returns the amount of shards managed by this {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager}.
     * This includes shards currently queued for a restart.
     *
     * @return The managed amount of shards.
     */
    int getShardsCount();

    /**
     * Returns the total shard count.
     *
     * @return The total amount of shards.
     */
    int getShardsTotal();

    /**
     * This returns the {@link net.dv8tion.jda.core.JDA.Status JDA.Status} of the shard which has the same id as the one provided.
     * <br>If there is no shard with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  shardId
     *         The id of the shard.
     *
     * @return The  {@link net.dv8tion.jda.core.JDA.Status JDA.Status} of the shard with the given shardId or
     *         {@code null} if no shard has the given id
     */
    JDA.Status getStatus(int shardId);

    /**
     * Gets the current {@link net.dv8tion.jda.core.JDA.Status Status} of all shards.
     *
     * @return All current shard statuses.
     */
    List<JDA.Status> getStatuses();

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
    TextChannel getTextChannelById(long id);

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
    TextChannel getTextChannelById(String id);

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
    List<TextChannel> getTextChannels();

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.core.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.User User} with matching id.
     */
    User getUserById(long id);

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.core.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.User User} with matching id.
     */
    User getUserById(String id);

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
    List<User> getUsers();

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     */
    VoiceChannel getVoiceChannelById(long id);

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     */
    VoiceChannel getVoiceChannelById(String id);

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} of all connected
     * {@link net.dv8tion.jda.core.entities.Guild Guilds}.
     *
     * @return Possible-empty list of all known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     */
    List<VoiceChannel> getVoiceChannels();

    /**
     * Removes all provided listeners from the event-listeners and no longer uses them to handle events.
     *
     * @param  listeners
     *         The listener(s) to be removed.
     */
    void removeEventListener(Object... listeners);

    /**
     * Restarts all shards
     */
    void restart();

    /**
     * Restarts the shards with the given id.
     *
     * @throws IllegalArgumentException
     *         if shardId is lower than minShardId or higher than maxShardId
     */
    void restart(int shardId);

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for all shards.
     * <br>A Game can be retrieved via {@link net.dv8tion.jda.core.entities.Game#of(String)}.
     * For streams you provide a valid streaming url as second parameter
     *
     * @param  game
     *         A {@link net.dv8tion.jda.core.entities.Game Game} instance or null to reset
     *
     * @see    net.dv8tion.jda.core.entities.Game#of(String)
     * @see    net.dv8tion.jda.core.entities.Game#of(String, String)
     */
    void setGame(Game game);

    /**
     * Sets whether all sessions should be marked as afk or not
     *
     * <p>This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     * @param idle
     *        boolean
     */
    void setIdle(boolean idle);

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for the given shards.
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @param  status
     *         the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     */
    void setStatus(int shardId, OnlineStatus status);

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for all shards.
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @param  status
     *         the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     */
    void setStatus(OnlineStatus status);

    /**
     * Shuts down all JDA shards, closing all their connections.
     * After this command is issShardManageragerImpl instance can not be used anymore.
     *
     * <p>This is the same as calling {@link #shutdown(boolean) shutdown(true)}.
     */
    void shutdown();

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
    void shutdown(boolean free);

    void shutdown(int shardId); // TODO: docs shutdown(int shardId)

    void start(int shardId); // TODO: docs start(int shardId)

}