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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.channel.middleman.BaseGuildMessageChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface TextChannelManager extends BaseGuildMessageChannelManager<TextChannel, TextChannelManager>
{
    /**
     * Sets the <b><u>slowmode</u></b> of the selected {@link TextChannel TextChannel}.
     * <br>Provide {@code 0} to reset the slowmode of the {@link TextChannel TextChannel}
     *
     * <p>A channel slowmode <b>must not</b> be negative nor greater than {@link TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}!
     * <br><b>This is only available to {@link TextChannel TextChannels}</b>
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having {@link Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     *         The new slowmode for the selected {@link TextChannel TextChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link GuildChannel GuildChannel}'s type is not {@link ChannelType#TEXT TEXT}
     * @throws IllegalArgumentException
     *         If the provided slowmode is negative or greater than {@link TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    TextChannelManager setSlowmode(int slowmode);
}
