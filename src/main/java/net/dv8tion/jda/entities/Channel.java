/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.managers.ChannelManager;

import java.util.List;

public interface Channel
{
    /**
     * The Id of the Channel. This is typically 18 characters long.
     * @return
     *      The Id of this Channel
     */
    String getId();

    /**
     * The human readable name of the  Channel.<br>
     * If no name has been set, this returns null.
     *
     * @return
     *      The name of this Channel
     */
    String getName();

    /**
     * The topic set for this Channel.
     * If no topic has been set, this returns null.
     *
     * @return
     *      Possibly-null String containing the topic of this Channel.
     */
    String getTopic();

    /**
     * Returns the {@link net.dv8tion.jda.entities.Guild Guild} that this Channel is part of.
     *
     * @return
     *      Never-null {@link net.dv8tion.jda.entities.Guild Guild} that this Channel is part of.
     */
    Guild getGuild();

    /**
     * A List of all {@link net.dv8tion.jda.entities.User Users} that are in this Channel
     * For {@link net.dv8tion.jda.entities.TextChannel TextChannels}, this returns all Users with the {@link net.dv8tion.jda.Permission#MESSAGE_READ} Permission.
     * In {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}, this returns all Users that joined that VoiceChannel.
     *
     * @return
     *      A List of {@link net.dv8tion.jda.entities.User Users} that are in this Channel.
     */
    List<User> getUsers();

    /**
     * The position this Channel is displayed at.<br>
     * Higher values mean they are displayed lower in the Client. Position 0 is the top most Channel
     * Channels of a {@link net.dv8tion.jda.entities.Guild Guild} do not have to have continuous positions
     *
     * @return
     *      Zero-based int of position of the Channel.
     */
    int getPosition();

    /**
     * Checks if the given {@link net.dv8tion.jda.entities.User User} has the given {@link net.dv8tion.jda.Permission Permission}
     * in this Channel
     *
     * @param user
     *          the User to check the Permission against
     * @param permission
     *          the Permission to check for
     * @return
     *      if the given User has the given Permission in this Channel
     */
    boolean checkPermission(User user, Permission permission);

    /**
     * Returns the {@link net.dv8tion.jda.managers.ChannelManager ChannelManager} for this Channel.
     * In the ChannelManager, you can modify the name, topic and position of this Channel.
     *
     * @return
     *      The ChannelManager of this Channel
     */
    ChannelManager getManager();

    /**
     * Returns the {@link net.dv8tion.jda.JDA JDA} instance of this Channel
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();
}
