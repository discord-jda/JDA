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

package net.dv8tion.jda.api.managers.channel.concrete;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.channel.middleman.BaseGuildMessageChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface NewsChannelManager extends BaseGuildMessageChannelManager<NewsChannel, NewsChannelManager>
{
    /**
     * Converts the selected channel to a different {@link ChannelType}.
     *
     * <br><br>
     * This can only be done in the follow situations:
     * <table>
     *     <caption style="display: none">Javadoc is stupid, this is not a required tag</caption>
     *     <thead>
     *         <tr>
     *             <th>Current Channel Type</th>
     *             <th></th>
     *             <th>New Channel Type</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>{@link ChannelType#NEWS}</td>
     *             <td> -&gt; </td>
     *             <td>{@link ChannelType#TEXT}</td>
     *         </tr>
     *         <tr>
     *             <td>{@link ChannelType#TEXT}</td>
     *             <td> -&gt; </td>
     *             <td>{@link ChannelType#NEWS}</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * @param  type
     *         The new not-null {@link ChannelType} of the channel
     *
     * @throws IllegalArgumentException
     *         If {@code channelType} is not {@link ChannelType#TEXT} or {@link ChannelType#NEWS}
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a {@link TextChannel} or {@link NewsChannel}
     * @throws IllegalStateException
     *         If {@code channelType} is {@link ChannelType#NEWS} and the guild doesn't have the {@code NEWS} feature in {@link Guild#getFeatures()}.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    Guild#getFeatures()
     */
    @Nonnull
    @CheckReturnValue
    NewsChannelManager setType(@Nonnull ChannelType type);
}
