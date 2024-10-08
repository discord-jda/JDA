/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.api.exceptions;

/**
 * Exception occurring on {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached entities},
 * indicating that the permissions could not be checked on this combination of channel and member.
 *
 * <p>Getting/Checking the permissions of a {@link net.dv8tion.jda.api.entities.Member Member} in a given {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel GuildChannel},
 * will only work when either:
 * <ul>
 *     <li>The member is the {@link net.dv8tion.jda.api.interactions.Interaction#getMember() interaction caller},
 *         and the channel is the {@link net.dv8tion.jda.api.interactions.Interaction#getGuildChannel() interaction channel}
 *     </li>
 *     <li>The member is an interaction option (such as slash command option or a member {@link net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu EntitySelectMenu} value)
 *         and the channel is the {@link net.dv8tion.jda.api.interactions.Interaction#getGuildChannel() interaction channel}
 *     </li>
 *     <li>The member is the {@link net.dv8tion.jda.api.interactions.Interaction#getMember() interaction caller}, and the channel is an interaction option</li>
 * </ul>
 */
public class MissingEntityInteractionPermissionsException extends RuntimeException
{
    /**
     * Creates a new MissingEntityInteractionPermissionsException
     */
    public MissingEntityInteractionPermissionsException()
    {
        this("Cannot perform action as the bot is not in the guild");
    }

    /**
     * Creates a new MissingEntityInteractionPermissionsException
     *
     * @param reason
     *        The reason for this Exception
     */
    public MissingEntityInteractionPermissionsException(String reason)
    {
        super(reason);
    }
}
