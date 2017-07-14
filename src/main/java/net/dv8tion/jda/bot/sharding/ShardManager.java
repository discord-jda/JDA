/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

/** 
 * This class acts as a manager for multiple shards. 
 * It contains several methods to make your life with sharding easier.
 * 
 * <br>Custom implementations my not support all methods and throw 
 * {@link java.lang.UnsupportedOperationException UnsupportedOperationExceptions} instead.  
 * 
 * @since  3.1 
 * @author Aljoscha Grebe 
 */ 
public interface ShardManager
{

    /**
     * Adds all provided listeners to the event-listeners that will be used to handle events.
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
     * @param  users
     *         The users which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
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
     * @param  user
     *         The user which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guild} instances which have the {@link net.dv8tion.jda.core.entities.User User} in them.
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

    /**Returns a Collection of all online shards.
     *
     * @return Unmodifiable list of all online shards controlled by this ShardManager.
     */
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
     * client, it is possible that you will not see the channel that this returns. This is because the discord client
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
     * @return List of all {@link net.dv8tion.jda.core.entities.User Users} that are visible to all online shards.
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
     * {@link net.dv8tion.jda.core.entities.Guild Guilds} of all online shards.
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
     * Restarts the shard with the given id.
     *
     * @param  shardId
     *         The id of the shard to be restarted.
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
     * @param  idle
     *         Whether or not the shards should be marked afk.
     */
    void setIdle(boolean idle);

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for the shard with the given id.
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @param  shardId
     *         The id of the shard to set the status for.
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
     * After this command is issued, this ShardManager instance can not be used anymore.
     */
    void shutdown();

    /**
     * Shuts down all JDA shards, closing all their connections.
     * After this command is issued, the ShardManager instance can not be used anymore.
     *
     * @deprecated
     *        Use {@link #shutdown()} instead.
     *
     * @param  free If true, shuts down JDA's rest system permanently for all current and future instances.
     */
    @Deprecated
    default void shutdown(boolean free)
    {
        shutdown();
    }

    /**
     * Shuts down down the JDA shard specified by shardId, closing its connections.
     * After this method is called, getShard(shardId) will return null.
     *
     * @param  shardId
     *         The id of the shard to be shutdown.
     */
    void shutdown(int shardId);

    /**
     * Queues a shard with the provided id to be created at some point in the future, unless there is already an online shard with the provided id.
     *
     * @param  shardId
     *         The id of the shard to be started.
     */
    void start(int shardId);

}