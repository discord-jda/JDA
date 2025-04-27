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

package net.dv8tion.jda.api.entities.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.detached.IDetachableEntity;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.FormattableFlags;
import java.util.Formatter;

/**
 * Abstract Channel interface for all {@link ChannelType ChannelTypes}.
 */
public interface Channel extends IMentionable, IDetachableEntity
{
    /**
     * The maximum length a channel name can be. ({@value #MAX_NAME_LENGTH})
     */
    int MAX_NAME_LENGTH = 100;

    /**
     * The flags configured for this channel.
     * <br>This feature is currently primarily used for {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * @return {@link EnumSet} of the configured {@link ChannelFlag ChannelFlags}, changes to this enum set are not reflected in the API.
     */
    @Nonnull
    default EnumSet<ChannelFlag> getFlags()
    {
        return EnumSet.noneOf(ChannelFlag.class);
    }

    /**
     * The human readable name of this channel.
     *
     * <p>May be an empty string for {@link net.dv8tion.jda.api.entities.channel.concrete.GroupChannel GroupChannels}
     * with no name (Group DMs with no name displays the recipients on the Discord client).
     *
     * @return The name of this channel
     */
    @Nonnull
    String getName();

    /**
     * The {@link ChannelType ChannelType} for this channel
     *
     * @return The channel type
     */
    @Nonnull
    ChannelType getType();

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this channel
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Deletes this Channel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> delete();

    @Nonnull
    @Override
    default String getAsMention()
    {
        return "<#" + getId() + '>';
    }

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        if (alt)
            out = "#" + (upper ? getName().toUpperCase(formatter.locale()) : getName());
        else
            out = getAsMention();

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}
