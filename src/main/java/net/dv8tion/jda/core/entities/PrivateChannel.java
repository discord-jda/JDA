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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.client.entities.CallableChannel;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.FormattableFlags;
import java.util.Formatter;

/**
 * Represents the connection used for direct messaging.
 */
public interface PrivateChannel extends MessageChannel, CallableChannel, IFakeable
{
    /**
     * The {@link net.dv8tion.jda.core.entities.User User} that this {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} communicates with.
     *
     * @return A non-null {@link net.dv8tion.jda.core.entities.User User}.
     */
    User getUser();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this PrivateChannel
     *
     * @return the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Closes a PrivateChannel. After being closed successfully the PrivateChannel is removed from the JDA mapping.
     * <br>As a note, this does not remove the history of the PrivateChannel. If the channel is reopened the history will
     * still be present.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     */
    RestAction<Void> close();

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        if (alt)
            out = "#" + (upper ?  getName().toUpperCase(formatter.locale()) : getName());
        else
            out = upper ?  getName().toUpperCase(formatter.locale()) : getName();

        try
        {
            Appendable appendable = formatter.out();
            if (precision > -1 && out.length() > precision)
            {
                appendable.append(StringUtils.truncate(out, precision));
                return;
            }

            if (leftJustified)
                appendable.append(StringUtils.rightPad(out, width));
            else
                appendable.append(StringUtils.leftPad(out, width));
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }
}
