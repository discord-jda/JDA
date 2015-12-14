/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

import java.util.List;


/**
 * Represents a Discord Text Channel.
 * This should provide all necessary functions for interacting with a channel.
 */
public interface TextChannel
{
    /**
     * The Id of the Channel. This is typically 18 characters long.
     * @return
     */
    String getId();

    /**
     * The human readable name of the Channel. If no name has been set, this returns null.
     * @return
     */
    String getName();

    /**
     * The topic set for the Channel. Can also be thought of as the description of this Channel.
     * If no topic has been set, this returns null.
     * @return
     */
    String getTopic();

    /**
     * Returns the Guild that this Channel is a part of.
     * @return
     */
    Guild getGuild();

    /**
     * A List of all uses that have the {@link net.dv8tion.jda.Permission#MESSAGE_READ} for this channel.
     * @return
     *      A List of Users that can read the messages in this channel.
     */
    List<User> getUsers();

    /**
     * The position the Channel is displayed at
     *
     * @return The channels position
     */
    int getPosition();
}
