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

package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Indicates that the accent color of a {@link net.dv8tion.jda.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the user who changed their accent color and their previous accent color.
 *
 * <p>Identifier: {@code accent_color}
 *
 * <h2>Requirements</h2>
 * <p>This event requires that the user be fetched by the {@link net.dv8tion.jda.api.JDA#retrieveUserById(long)} method
 * as the user's profile data is not sent directly. Thus, the old value will be null even if the user has an accent color
 * unless previously fetched.
 */
public class UserUpdateAccentColorEvent extends GenericUserUpdateEvent<Color>
{
    public static final String IDENTIFIER = "accent_color";

    public UserUpdateAccentColorEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nullable Color oldValue)
    {
        super(api, responseNumber, user, oldValue, user.getAccentColor(), IDENTIFIER);
    }

    /**
     * The old accent color
     *
     * @return The old accent color, null if previously unknown or unset.
     */
    @Nullable
    public Color getOldAccentColor()
    {
        return getOldValue();
    }

    /**
     * The old accent color as a raw RGB integer
     *
     * @return The old accent color as a raw RGB integer,
     * or {@link net.dv8tion.jda.api.entities.User#DEFAULT_ACCENT_COLOR_RAW} if previously unknown or unset.
     */
    public int getOldAccentColorRaw()
    {
        Color color = getOldAccentColor();
        return color == null ? User.DEFAULT_ACCENT_COLOR_RAW : color.getRGB();
    }

    /**
     * The new accent color
     *
     * @return The new accent color, null if the user has removed their color.
     */
    @Nullable
    public Color getNewAccentColor()
    {
        return getNewValue();
    }

    /**
     * The new accent color as a raw RGB integer
     *
     * @return The new accent color as a raw RGB integer,
     * or {@link net.dv8tion.jda.api.entities.User#DEFAULT_ACCENT_COLOR_RAW} if the user has removed their color.
     */
    public int getNewAccentColorRaw() {
        Color color = getNewAccentColor();
        return color == null ? User.DEFAULT_ACCENT_COLOR_RAW : color.getRGB();
    }
}
