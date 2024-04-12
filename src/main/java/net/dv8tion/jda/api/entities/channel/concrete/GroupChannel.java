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

package net.dv8tion.jda.api.entities.channel.concrete;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.detached.IDetachableEntity;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Group DM channel.
 *
 * <p>This is only used for user-installed apps.
 */
public interface GroupChannel extends MessageChannel, IDetachableEntity
{
    /** Template for {@link #getIconUrl()}. */
    String ICON_URL = "https://cdn.discordapp.com/channel-icons/%s/%s.png";

    /**
     * The Discord hash-id of the group channel icon image.
     * If no icon has been set, this returns {@code null}.
     *
     * @return Possibly-null String containing the group channel's icon hash-id.
     */
    @Nullable
    String getIconId();

    /**
     * The URL of the group channel icon image.
     * If no icon has been set, this returns {@code null}.
     *
     * @return Possibly-null String containing the group channel's icon URL.
     */
    @Nullable
    default String getIconUrl()
    {
        String iconId = getIconId();
        return iconId == null ? null : String.format(ICON_URL, getId(), iconId);
    }

    /**
     * Returns an {@link ImageProxy} for this group channel's icon.
     *
     * @return Possibly-null {@link ImageProxy} of this group channel's icon
     *
     * @see    #getIconUrl()
     */
    @Nullable
    default ImageProxy getIcon()
    {
        final String iconUrl = getIconUrl();
        return iconUrl == null ? null : new ImageProxy(iconUrl);
    }

    @Nonnull
    UserSnowflake getOwnerId();

    /**
     * Retrieves the {@link User} which owns this {@link GroupChannel}.
     *
     * @return A {@link RestAction} to retrieve the {@link User User} which owns this {@link GroupChannel}.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<User> retrieveOwner()
    {
        return getJDA().retrieveUserById(getOwnerId().getIdLong());
    }
}
