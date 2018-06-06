/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.managers;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.AbstractChannelImpl;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.PermOverrideData;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.Collection;

/**
 * Manager providing functionality to update one or more fields for a {@link net.dv8tion.jda.core.entities.Channel Guild Channel}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("github-log")
 *        .setTopic("logs for github commits")
 *        .setNSFW(false)
 *        .queue();
 * manager.reset(ChannelManager.TOPIC | ChannelManager.NAME)
 *        .setName("nsfw-commits")
 *        .setTopic(null)
 *        .setNSFW(true)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.core.entities.Channel#getManager()
 */
public class ChannelManager extends ManagerBase
{
    /** Used to reset the name field */
    public static final long NAME      = 0x1;
    /** Used to reset the parent field */
    public static final long PARENT    = 0x2;
    /** Used to reset the topic field */
    public static final long TOPIC     = 0x4;
    /** Used to reset the position field */
    public static final long POSITION  = 0x8;
    /** Used to reset the nsfw field */
    public static final long NSFW      = 0x10;
    /** Used to reset the userlimit field */
    public static final long USERLIMIT = 0x20;
    /** Used to reset the bitrate field */
    public static final long BITRATE   = 0x40;
    /** Used to reset the permission field */
    public static final long PERMISSION = 0x80;

    protected final Channel channel;

    protected String name;
    protected String parent;
    protected String topic;
    protected int position;
    protected boolean nsfw;
    protected int userlimit;
    protected int bitrate;

    protected final Object lock = new Object();
    protected final TLongObjectHashMap<PermOverrideData> overridesAdd;
    protected final TLongSet overridesRem;

    /**
     * Creates a new ChannelManager instance
     *
     * @param channel
     *        {@link net.dv8tion.jda.core.entities.Channel Channel} that should be modified
     *        <br>Either {@link net.dv8tion.jda.core.entities.VoiceChannel Voice}- or {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     */
    public ChannelManager(Channel channel)
    {
        super(channel.getJDA(),
              Route.Channels.MODIFY_CHANNEL.compile(channel.getId()));
        this.channel = channel;
        if (isPermissionChecksEnabled())
            checkPermissions();
        this.overridesAdd = new TLongObjectHashMap<>();
        this.overridesRem = new TLongHashSet();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     *
     * @return The ChannelType
     */
    public ChannelType getType()
    {
        return channel.getType();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Channel Channel} that will
     * be modified by this Manager instance
     *
     * @return The {@link net.dv8tion.jda.core.entities.Channel Channel}
     */
    public Channel getChannel()
    {
        return channel;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Channel Channel} is in.
     * <br>This is logically the same as calling {@code getChannel().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return channel.getGuild();
    }

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ChannelManager.NAME | ChannelManager.PARENT);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #PARENT}</li>
     *     <li>{@link #TOPIC}</li>
     *     <li>{@link #POSITION}</li>
     *     <li>{@link #NSFW}</li>
     *     <li>{@link #USERLIMIT}</li>
     *     <li>{@link #BITRATE}</li>
     *     <li>{@link #PERMISSION}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return ChannelManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public ChannelManager reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & PARENT) == PARENT)
            this.parent = null;
        if ((fields & TOPIC) == TOPIC)
            this.topic = null;
        if ((fields & PERMISSION) == PERMISSION)
        {
            withLock(lock, (lock) ->
            {
                this.overridesRem.clear();
                this.overridesAdd.clear();
            });
        }
        return this;
    }

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(ChannelManager.NAME, ChannelManager.PARENT);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #PARENT}</li>
     *     <li>{@link #TOPIC}</li>
     *     <li>{@link #POSITION}</li>
     *     <li>{@link #NSFW}</li>
     *     <li>{@link #USERLIMIT}</li>
     *     <li>{@link #BITRATE}</li>
     *     <li>{@link #PERMISSION}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return ChannelManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public ChannelManager reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    /**
     * Resets all fields for this manager.
     *
     * @return ChannelManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public ChannelManager reset()
    {
        super.reset();
        this.name = null;
        this.parent = null;
        this.topic = null;
        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            this.overridesAdd.clear();
        });
        return this;
    }

    /**
     * Clears the overrides added via {@link #putPermissionOverride(IPermissionHolder, Collection, Collection)}.
     *
     * @return ChannelManager for chaining convenience
     */
    public ChannelManager clearOverridesAdded()
    {
        withLock(lock, (lock) ->
        {
            this.overridesAdd.clear();
            if (this.overridesRem.isEmpty())
                set &= ~PERMISSION;
        });
        return this;
    }

    /**
     * Clears the overrides removed via {@link #removePermissionOverride(IPermissionHolder)}.
     *
     * @return ChannelManager for chaining convenience
     */
    public ChannelManager clearOverridesRemoved()
    {
        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            if (this.overridesAdd.isEmpty())
                set &= ~PERMISSION;
        });
        return this;
    }

    /**
     * Adds an override for the specified {@link net.dv8tion.jda.core.entities.IPermissionHolder IPermissionHolder}
     * with the provided raw bitmasks as allowed and denied permissions. If the permission holder already
     * had an override on this channel it will be updated instead.
     *
     * @param  permHolder
     *         The permission holder
     * @param  allow
     *         The bitmask to grant
     * @param  deny
     *         The bitmask to deny
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putPermissionOverride(IPermissionHolder, Collection, Collection)
     * @see    net.dv8tion.jda.core.Permission#getRaw(Permission...) Permission.getRaw(Permission...)
     */
    @CheckReturnValue
    public ChannelManager putPermissionOverride(IPermissionHolder permHolder, long allow, long deny)
    {
        Checks.notNull(permHolder, "PermissionHolder");
        Checks.check(permHolder.getGuild().equals(getGuild()), "PermissionHolder is not from the same Guild!");
        if (isPermissionChecksEnabled() && !getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(Permission.MANAGE_PERMISSIONS);
        final long id = getId(permHolder);
        final int type = permHolder instanceof Role ? PermOverrideData.ROLE_TYPE : PermOverrideData.MEMBER_TYPE;
        withLock(lock, (lock) ->
        {
            this.overridesRem.remove(id);
            this.overridesAdd.put(id, new PermOverrideData(type, id, allow, deny));
            set |= PERMISSION;
        });
        return this;
    }

    /**
     * Adds an override for the specified {@link net.dv8tion.jda.core.entities.IPermissionHolder IPermissionHolder}
     * with the provided permission sets as allowed and denied permissions. If the permission holder already
     * had an override on this channel it will be updated instead.
     * <br>Example: {@code putPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ), null)}
     *
     * @param  permHolder
     *         The permission holder
     * @param  allow
     *         The permissions to grant, or null
     * @param  deny
     *         The permissions to deny, or null
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putPermissionOverride(IPermissionHolder, long, long)
     * @see    java.util.EnumSet EnumSet
     */
    @CheckReturnValue
    public ChannelManager putPermissionOverride(IPermissionHolder permHolder, Collection<Permission> allow, Collection<Permission> deny)
    {
        long allowRaw = allow == null ? 0 : Permission.getRaw(allow);
        long denyRaw  = deny  == null ? 0 : Permission.getRaw(deny);
        return putPermissionOverride(permHolder, allowRaw, denyRaw);
    }

    /**
     * Removes the {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} for the specified
     * {@link net.dv8tion.jda.core.entities.IPermissionHolder IPermissionHolder}. If no override existed for this member
     * this does nothing.
     *
     * @param  permHolder
     *         The permission holder
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel
     *
     * @return ChannelManager for chaining convenience
     */
    @CheckReturnValue
    public ChannelManager removePermissionOverride(IPermissionHolder permHolder)
    {
        Checks.notNull(permHolder, "PermissionHolder");
        Checks.check(permHolder.getGuild().equals(getGuild()), "PermissionHolder is not from the same Guild!");
        if (isPermissionChecksEnabled() && !getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(Permission.MANAGE_PERMISSIONS);
        final long id = getId(permHolder);
        withLock(lock, (lock) ->
        {
            this.overridesRem.add(id);
            this.overridesAdd.remove(id);
            set |= PERMISSION;
        });
        return this;
    }

    /**
     * Syncs all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} of this Channel with
     * its parent ({@link net.dv8tion.jda.core.entities.Category Category}).
     *
     * <p>After this operation, all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     * will be exactly the same as the ones from the parent.
     * <br><b>That means that all current PermissionOverrides are lost!</b>
     *
     * <p>This behaves as if calling {@link #sync(Channel)} with this Channel's {@link net.dv8tion.jda.core.entities.Channel#getParent() Parent}.
     *
     * @throws  java.lang.IllegalStateException
     *          If this Channel has no parent
     * @throws  net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *          If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *          in this channel
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see     <a href="https://discordapp.com/developers/docs/topics/permissions#permission-syncing" target="_blank">Discord Documentation - Permission Syncing</a>
     */
    @CheckReturnValue
    public ChannelManager sync()
    {
        if(channel.getParent() == null)
            throw new IllegalStateException("sync() requires a parent category");
        return sync(channel.getParent());
    }

    /**
     * Syncs all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} of this Channel with
     * the given ({@link net.dv8tion.jda.core.entities.Channel Channel}).
     *
     * <p>After this operation, all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     * will be exactly the same as the ones from the syncSource.
     * <br><b>That means that all current PermissionOverrides are lost!</b>
     *
     * <p>This will only work for Channels of the same {@link net.dv8tion.jda.core.entities.Guild Guild}!.
     *
     * @param   syncSource
     *          The Channel from where all PermissionOverrides should be copied from
     *
     * @throws  java.lang.IllegalArgumentException
     *          If the given snySource is {@code null}, this Channel or from a different Guild.
     * @throws  net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *          If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *          in this channel
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see     <a href="https://discordapp.com/developers/docs/topics/permissions#permission-syncing" target="_blank">Discord Documentation - Permission Syncing</a>
     */
    @CheckReturnValue
    public ChannelManager sync(Channel syncSource)
    {
        Checks.notNull(syncSource, "SyncSource");
        Checks.check(channel.getGuild().equals(syncSource.getGuild()), "Sync only works for channels of same guild");

        if(syncSource.equals(channel))
            return this;

        if (isPermissionChecksEnabled() && !getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(Permission.MANAGE_PERMISSIONS);

        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            this.overridesAdd.clear();

            //set all current overrides to-be-removed
            channel.getPermissionOverrides().forEach(permO ->
                this.overridesRem.add(getId(permO.isRoleOverride() ? permO.getRole() : permO.getMember()))
            );

            //re-add all perm-overrides of syncSource
            syncSource.getPermissionOverrides().forEach(permO ->
            {
                int type = permO.isRoleOverride() ? PermOverrideData.ROLE_TYPE : PermOverrideData.MEMBER_TYPE;
                long id = getId(permO.isRoleOverride() ? permO.getRole() : permO.getMember());

                this.overridesRem.remove(id);
                this.overridesAdd.put(id, new PermOverrideData(type, id, permO.getAllowedRaw(), permO.getDeniedRaw()));
            });

            set |= PERMISSION;
        });
        return this;
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>A channel name <b>must not</b> be {@code null} nor less than 2 characters or more than 100 characters long!
     * <br>TextChannel names may only be populated with alphanumeric (with underscore and dash).
     *
     * <p><b>Example</b>: {@code mod-only} or {@code generic_name}
     * <br>Characters will automatically be lowercased by Discord for text channels!
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.core.entities.Channel Channel}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-100 characters long
     *
     * @return ChannelManager for chaining convenience
     */
    @CheckReturnValue
    public ChannelManager setName(String name)
    {
        Checks.notBlank(name, "Name");
        Checks.check(name.length() >= 2 && name.length() <= 100, "Name must be between 2-100 characters long");
        if (getType() == ChannelType.TEXT)
            Checks.noWhitespace(name, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    /**
     * Sets the <b><u>{@link net.dv8tion.jda.core.entities.Category Parent Category}</u></b>
     * of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     *
     * @param  category
     *         The new parent for the selected {@link net.dv8tion.jda.core.entities.Channel Channel}
     *
     * @throws IllegalStateException
     *         If the target is a category itself
     * @throws IllegalArgumentException
     *         If the provided category is not from the same Guild
     *
     * @return ChannelManager for chaining convenience
     *
     * @since  3.4.0
     */
    @CheckReturnValue
    public ChannelManager setParent(Category category)
    {
        if (category != null)
        {
            if (getType() == ChannelType.CATEGORY)
                throw new IllegalStateException("Cannot set the parent of a category");
            Checks.check(category.getGuild().equals(getGuild()), "Category is not from the same guild");
        }
        this.parent = category == null ? null : category.getId();
        set |= PARENT;
        return this;
    }

    /**
     * Sets the <b><u>position</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p><b>To modify multiple channels you should use
     * <code>Guild.{@link net.dv8tion.jda.core.managers.GuildController getController()}.{@link GuildController#modifyTextChannelPositions() modifyTextChannelPositions()}</code>
     * instead! This is not the same as looping through channels and using this to update positions!</b>
     *
     * @param  position
     *         The new position for the selected {@link net.dv8tion.jda.core.entities.Channel Channel}
     *
     * @return ChannelManager for chaining convenience
     */
    @CheckReturnValue
    public ChannelManager setPosition(int position)
    {
        this.position = position;
        set |= POSITION;
        return this;
    }

    /**
     * Sets the <b><u>topic</u></b> of the selected {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * <p>A channel topic <b>must not</b> be more than {@code 1024} characters long!
     * <br><b>This is only available to {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}</b>
     *
     * @param  topic
     *         The new topic for the selected {@link net.dv8tion.jda.core.entities.TextChannel TextChannel},
     *         {@code null} or empty String to reset
     *
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}
     * @throws IllegalArgumentException
     *         If the provided topic is greater than {@code 1024} in length
     *
     * @return ChannelManager for chaining convenience
     */
    @CheckReturnValue
    public ChannelManager setTopic(String topic)
    {
        if (getType() != ChannelType.TEXT)
            throw new IllegalStateException("Can only set topic on text channels");
        Checks.check(topic == null || topic.length() <= 1024, "Topic must be less or equal to 1024 characters in length");
        this.topic = topic;
        set |= TOPIC;
        return this;
    }

    /**
     * Sets the <b><u>nsfw flag</u></b> of the selected {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @param  nsfw
     *         The new nsfw flag for the selected {@link net.dv8tion.jda.core.entities.TextChannel TextChannel},
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}
     *
     * @return ChannelManager for chaining convenience
     */
    @CheckReturnValue
    public ChannelManager setNSFW(boolean nsfw)
    {
        if (getType() != ChannelType.TEXT)
            throw new IllegalStateException("Can only set nsfw on text channels");
        this.nsfw = nsfw;
        set |= NSFW;
        return this;
    }

    /**
     * Sets the <b><u>user-limit</u></b> of the selected {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     * <br>Provide {@code 0} to reset the user-limit of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     *
     * <p>A channel user-limit <b>must not</b> be negative nor greater than {@code 99}!
     * <br><b>This is only available to {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}</b>
     *
     * @param  userLimit
     *         The new user-limit for the selected {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided user-limit is negative or greater than {@code 99}
     *
     * @return ChannelManager for chaining convenience
     */
    @CheckReturnValue
    public ChannelManager setUserLimit(int userLimit)
    {
        if (getType() != ChannelType.VOICE)
            throw new IllegalStateException("Can only set userlimit on voice channels");
        Checks.notNegative(userLimit, "Userlimit");
        Checks.check(userLimit <= 99, "Userlimit may not be greater than 99");
        this.userlimit = userLimit;
        set |= USERLIMIT;
        return this;
    }

    /**
     * Sets the <b><u>bitrate</u></b> of the selected {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     * <br>The default value is {@code 64000}
     *
     * <p>A channel bitrate <b>must not</b> be less than {@code 8000} nor greater than {@code 96000} (for non-vip Guilds)!
     * {@link net.dv8tion.jda.core.entities.Guild#getFeatures() VIP Guilds} allow a bitrate for up to {@code 128000}.
     * <br><b>This is only available to {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}</b>
     *
     * @param  bitrate
     *         The new bitrate for the selected {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided bitrate is not between 8000-96000 (or 128000 for VIP Guilds)
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    net.dv8tion.jda.core.entities.Guild#getFeatures()
     */
    @CheckReturnValue
    public ChannelManager setBitrate(int bitrate)
    {
        if (getType() != ChannelType.VOICE)
            throw new IllegalStateException("Can only set bitrate on voice channels");
        final int maxBitrate = getGuild().getFeatures().contains("VIP_REGIONS") ? 128000 : 96000;
        Checks.check(bitrate >= 8000, "Bitrate must be greater or equal to 8000");
        Checks.check(bitrate <= maxBitrate, "Bitrate must be less or equal to %s", maxBitrate);
        this.bitrate = bitrate;
        set |= BITRATE;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject frame = new JSONObject().put("name", channel.getName());
        if (shouldUpdate(NAME))
            frame.put("name", name);
        if (shouldUpdate(POSITION))
            frame.put("position", position);
        if (shouldUpdate(TOPIC))
            frame.put("topic", opt(topic));
        if (shouldUpdate(NSFW))
            frame.put("nsfw", nsfw);
        if (shouldUpdate(USERLIMIT))
            frame.put("user_limit", userlimit);
        if (shouldUpdate(BITRATE))
            frame.put("bitrate", bitrate);
        if (shouldUpdate(PARENT))
            frame.put("parent_id", opt(parent));
        withLock(lock, (lock) ->
        {
            if (shouldUpdate(PERMISSION))
                frame.put("permission_overwrites", getOverrides());
        });

        reset();
        return getRequestBody(frame);
    }

    @Override
    protected boolean checkPermissions()
    {
        final Member selfMember = getGuild().getSelfMember();
        if (!selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(Permission.MANAGE_CHANNEL);
        return super.checkPermissions();
    }

    protected Collection<PermOverrideData> getOverrides()
    {
        //note: overridesAdd and overridesRem are mutually disjoint
        TLongObjectHashMap<PermOverrideData> data = new TLongObjectHashMap<>(this.overridesAdd);

        AbstractChannelImpl<?> impl = (AbstractChannelImpl<?>) channel;
        impl.getOverrideMap().forEachEntry((id, override) ->
        {
            //removed by not adding them here, this data set overrides the existing one
            //we can use remove because it will be reset afterwards either way
            if (!overridesRem.remove(id) && !data.containsKey(id))
                data.put(id, new PermOverrideData(override));
            return true;
        });
        return data.valueCollection();
    }

    protected long getId(IPermissionHolder holder)
    {
        if (holder instanceof Role)
            return ((Role) holder).getIdLong();
        else
            return ((Member) holder).getUser().getIdLong();
    }
}
