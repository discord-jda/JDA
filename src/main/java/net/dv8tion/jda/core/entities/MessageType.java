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
package net.dv8tion.jda.core.entities;

/**
 * Represents the different types of {@link net.dv8tion.jda.core.entities.Message Messages} that can be received from Discord.
 * <br>A normal text based message is {@link #DEFAULT}.
 */
public enum MessageType
{
    /**
     * The normal text messages received when a user or bot sends a Message.
     */
    DEFAULT(0),

    /**
     * Specialized messages used for Groups as a System-Message showing that a new User has been added to the Group.
     */
    RECIPIENT_ADD(1),

    /**
     * Specialized messages used for Groups as a System-Message showing that a new User has been removed from the Group.
     */
    RECIPIENT_REMOVE(2),

    /**
     * Specialized message used for Groups as a System-Message showing that a Call was started.
     */
    CALL(3),

    /**
     * Specialized message used for Groups as a System-Message showing that the name of the Group was changed.
     */
    CHANNEL_NAME_CHANGE(4),

    /**
     * Specialized message used for Groups as a System-Message showing that the icon of the Group was changed.
     */
    CHANNEL_ICON_CHANGE(5),

    /**
     * Specialized message used in MessageChannels as a System-Message to announce new pins
     */
    CHANNEL_PINNED_ADD(6),

    /**
     * Specialized message used to welcome new members in a Guild
     */
    GUILD_MEMBER_JOIN(7),

    /**
     * Unknown MessageType.
     */
    UNKNOWN(-1);

    protected final int id;
    MessageType(int id)
    {
        this.id = id;
    }

    /**
     * The Discord id key used to reference the MessageType.
     *
     * @return the Discord id key.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Used to retrieve a MessageType based on the Discord id key.
     * <br>If the {@code id} provided is not a known id, {@link #UNKNOWN} is returned
     *
     * @param  id
     *         The Discord key id of the requested MessageType.
     *
     * @return A MessageType with the same Discord id key as the one provided, or {@link #UNKNOWN}.
     */
    public static MessageType fromId(int id)
    {
        for (MessageType type : values())
        {
            if (type.id == id)
                return type;
        }
        return UNKNOWN;
    }
}
