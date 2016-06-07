/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

import java.util.Collection;

/**
 * Represents a Discord Text Channel. See {@link net.dv8tion.jda.entities.Channel Channel} and
 * {@link net.dv8tion.jda.entities.MessageChannel MessageChannel} for more information.
 */
public interface TextChannel extends Channel, MessageChannel, Comparable<TextChannel>
{
    /**
     * Internal implementation of this class is available at
     * {@link net.dv8tion.jda.entities.impl.TextChannelImpl TextChannelImpl}.<br>
     * Note: Internal implementation should not be used directly.
     */

    /**
     * Returns the String needed to mention this TextChannel in a {@link net.dv8tion.jda.entities.Message Message}.
     *
     * @return
     *      The String needed to mention this Channel
     */
    String getAsMention();
    
    /**
     * Bulk deletes a list of messages. Must not be more than 100 messages at a time.
     * You must have channel permission to delete messages.
     * If only a single message is supplied then this function will resort to using {@link net.dv8tion.jda.entities.Message#deleteMessage} instead.
     * @param messages
     *      The messages to delete.
     * @throws IllegalArgumentException
     *      If the size of the list is more than 100 messages.
     * @throws IllegalArgumentException
     *      If this account does not have MANAGE_MESSAGES
     */
    void deleteMessages(Collection<Message> messages);
}
