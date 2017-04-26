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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.ChannelManagerUpdatable ChannelManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class ChannelManager
{
    protected final ChannelManagerUpdatable updatable;

    /**
     * Creates a new ChannelManager instance
     *
     * @param channel
     *        {@link net.dv8tion.jda.core.entities.Channel Channel} that should be modified
     *        <br>Either {@link net.dv8tion.jda.core.entities.VoiceChannel Voice}- or {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     */
    public ChannelManager(Channel channel)
    {
        this.updatable = new ChannelManagerUpdatable(channel);
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return updatable.getJDA();
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
        return updatable.getChannel();
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
        return updatable.getGuild();
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
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-100 characters long
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ChannelManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.ChannelManagerUpdatable#getNameField()
     * @see    net.dv8tion.jda.core.managers.ChannelManagerUpdatable#update()
     */
    public RestAction<Void> setName(String name)
    {
        return updatable.getNameField().setValue(name).update();
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
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}
     * @throws IllegalArgumentException
     *         If the provided topic is greater than {@code 1024} in length
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ChannelManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.ChannelManagerUpdatable#getTopicField()
     * @see    net.dv8tion.jda.core.managers.ChannelManagerUpdatable#update()
     */
    public RestAction<Void> setTopic(String topic)
    {
        return updatable.getTopicField().setValue(topic).update();
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
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}
     * @throws IllegalArgumentException
     *         If the provided user-limit is negative or greater than {@code 99}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ChannelManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.ChannelManagerUpdatable#getUserLimitField()
     * @see    net.dv8tion.jda.core.managers.ChannelManagerUpdatable#update()
     */
    public RestAction<Void> setUserLimit(int userLimit)
    {
        return updatable.getUserLimitField().setValue(userLimit).update();
    }

    /**
     * Sets the <b><u>bitrate</u></b> of the selected {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     * <br>The default value is {@code 64000}
     *
     * <p>A channel user-limit <b>must not</b> be less than {@code 8000} nor greater than {@code 96000} (for non-vip Guilds)!
     * <br><b>This is only available to {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}</b>
     *
     * @param  bitrate
     *         The new bitrate for the selected {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided bitrate is not between 8000-96000
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ChannelManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.ChannelManagerUpdatable#getBitrateField()
     * @see    net.dv8tion.jda.core.managers.ChannelManagerUpdatable#update()
     */
    public RestAction<Void> setBitrate(int bitrate)
    {
        return updatable.getBitrateField().setValue(bitrate).update();
    }
}
