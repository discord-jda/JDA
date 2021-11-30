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

package net.dv8tion.jda.api.managers.channel.middleman;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.channel.attribute.ICategorizableChannelManager;
import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager;
import net.dv8tion.jda.api.managers.channel.attribute.IPositionableChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BaseGuildMessageChannelManager<T extends BaseGuildMessageChannel, M extends BaseGuildMessageChannelManager<T, M>>
        extends IPermissionContainerManager<T, M>,
        IPositionableChannelManager<T, M>,
        ICategorizableChannelManager<T, M>
{
    /**
     * Sets the <b><u>topic</u></b> of the selected
     * {@link TextChannel TextChannel} or {@link StageChannel StageChannel}.
     *
     * <p>A channel topic <b>must not</b> be more than {@code 1024} characters long!
     * <br><b>This is only available to {@link TextChannel TextChannels}</b>
     *
     * @param  topic
     *         The new topic for the selected channel,
     *         {@code null} or empty String to reset
     *
     * @throws UnsupportedOperationException
     *         If the selected {@link GuildChannel GuildChannel}'s type is not {@link ChannelType#TEXT TEXT}
     * @throws IllegalArgumentException
     *         If the provided topic is greater than {@code 1024} in length
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M setTopic(@Nullable String topic);

    /**
     * Sets the <b><u>nsfw flag</u></b> of the selected {@link TextChannel TextChannel} or {@link NewsChannel}.
     *
     * @param  nsfw
     *         The new nsfw flag for the selected {@link TextChannel TextChannel},
     *
     * @throws IllegalStateException
     *         If the selected {@link GuildChannel GuildChannel}'s type is not {@link ChannelType#TEXT TEXT}
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M setNSFW(boolean nsfw);
}
