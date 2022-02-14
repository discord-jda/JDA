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

package net.dv8tion.jda.api.events.self;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the avatar of the current user changed.
 *
 * <p>Can be used to retrieve the old avatar.
 *
 * <p>Identifier: {@code avatar}
 */
public class SelfUpdateAvatarEvent extends GenericSelfUpdateEvent<String>
{
    public static final String IDENTIFIER = "avatar";
    private static final String AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s%s";

    public SelfUpdateAvatarEvent(@Nonnull JDA api, long responseNumber, @Nullable String oldAvatarId)
    {
        super(api, responseNumber, oldAvatarId, api.getSelfUser().getAvatarId(), IDENTIFIER);
    }

    /**
     * The old avatar id
     *
     * @return The old avatar id
     */
    @Nullable //TODO remove
    public String getOldAvatarId()
    {
        return getOldValue();
    }

    /**
     * The old avatar url
     *
     * @return  The old avatar url
     */
    @Nullable //TODO remove
    public String getOldAvatarUrl()
    {
        return previous == null ? null : String.format(AVATAR_URL, getSelfUser().getId(), previous, previous.startsWith("a_") ? ".gif" : ".png");
    }

    //TODO docs
    @Nullable
    public ImageProxy getOldAvatar()
    {
        if (previous == null) return null;

        final String extension = previous.startsWith("a_") ? ".gif" : ".png";
        final String oldAvatarUrl = String.format(AVATAR_URL, getSelfUser().getId(), previous, extension);

        return new ImageProxy(oldAvatarUrl);
    }

    /**
     * The new avatar id
     *
     * @return The new avatar id
     */
    @Nullable //TODO remove
    public String getNewAvatarId()
    {
        return getNewValue();
    }

    /**
     * The new avatar url
     *
     * @return  The new avatar url
     */
    @Nullable //TODO remove
    public String getNewAvatarUrl()
    {
        return next == null ? null : String.format(AVATAR_URL, getSelfUser().getId(), next, next.startsWith("a_") ? ".gif" : ".png");
    }

    //TODO docs
    @Nullable
    public ImageProxy getNewAvatar()
    {
        if (next == null) return null;

        final String extension = next.startsWith("a_") ? ".gif" : ".png";
        final String newAvatarUrl = String.format(AVATAR_URL, getSelfUser().getId(), next, extension);

        return new ImageProxy(newAvatarUrl);
    }
}
