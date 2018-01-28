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

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.ChannelManagerUpdatable ChannelManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class ChannelManager extends ManagerBase
{
    /*
       ~ TODO

       - Checking whether we need to perform an update at all
       - Documentation for bit-flag system
     */
    public static final int NAME      = 0x1;
    public static final int PARENT    = 0x2;
    public static final int TOPIC     = 0x4;
    public static final int POSITION  = 0x8;
    public static final int NSFW      = 0x10;
    public static final int USERLIMIT = 0x20;
    public static final int BITRATE   = 0x40;

    protected final Channel channel;

    protected String name;
    protected String parent;
    protected String topic;
    protected int position;
    protected boolean nsfw;
    protected int userlimit;
    protected int bitrate;

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
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Channel Channel} that will
     * be modified by this Manager instance
     *
     * @return The {@link net.dv8tion.jda.core.entities.Channel Channel}
     *
     * @see    ChannelManagerUpdatable#getChannel()
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
     *
     * @see    ChannelManagerUpdatable#getGuild()
     */
    public Guild getGuild()
    {
        return channel.getGuild();
    }

    @Override
    @CheckReturnValue
    public ChannelManager reset(int fields)
    {
        super.reset(fields);
        return this;
    }

    @Override
    @CheckReturnValue
    public ChannelManager reset()
    {
        super.reset();
        return this;
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>A channel name <b>must not</b> be {@code null} nor less than 2 characters or more than 100 characters long!
     * <br>TextChannel names may only be populated with alphanumeric (with underscore and dash).
     *
     * <p><b>Example</b>: {@code mod-only} or {@code generic_name}
     * <br>Characters will automatically be lowercased by Discord!
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
     * @throws IllegalArgumentException
     *         If the provided category is not from the same Guild
     * @throws UnsupportedOperationException
     *         If the target is a category itself
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
            Checks.check(channel.getType() != ChannelType.CATEGORY, "Cannot set the parent of a category");
            Checks.check(category.getGuild().equals(getGuild()), "Category is not from the same guild");
        }
        this.parent = category == null ? null : category.getId();
        set |= PARENT;
        return this;
    }

    /**
     * Sets the <b><u>position</u></b>
     * of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
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
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}
     *
     * @return ChannelManager for chaining convenience
     */
    @CheckReturnValue
    public ChannelManager setNSFW(boolean nsfw)
    {
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
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided user-limit is negative or greater than {@code 99}
     *
     * @return ChannelManager for chaining convenience
     */
    @CheckReturnValue
    public ChannelManager setUserLimit(int userLimit)
    {
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
     * @throws UnsupportedOperationException
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
        //todo: use finalizeChecks instead
        if (!getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(Permission.MANAGE_CHANNEL);

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

        reset();
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, frame.toString());
    }
}
