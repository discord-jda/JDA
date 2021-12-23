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
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the Avatar of a {@link net.dv8tion.jda.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the User who changed their avatar and their previous Avatar ID/URL.
 *
 * <p>Identifier: {@code avatar}
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateAvatarEvent extends GenericUserUpdateEvent<String>
{
    public static final String IDENTIFIER = "avatar";

    public UserUpdateAvatarEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nullable String oldAvatar)
    {
        super(api, responseNumber, user, oldAvatar, user.getAvatarId(), IDENTIFIER);
    }

    /**
     * The previous avatar id
     *
     * @return The previous avatar id
     */
    @Nullable //TODO remove
    public String getOldAvatarId()
    {
        return getOldValue();
    }

    /**
     * The previous avatar url
     *
     * @return The previous avatar url
     */
    @Nullable //TODO remove
    public String getOldAvatarUrl()
    {
        return previous == null ? null : String.format(User.AVATAR_URL, getUser().getId(), previous, previous.startsWith("a_") ? "gif" : "png");
    }

    //TODO docs
    @Nullable
    public ImageProxy getOldAvatar()
    {
        if (previous == null) return null;

        final String extension = previous.startsWith("a_") ? "gif" : "png";
        final String oldAvatarUrl = String.format(User.AVATAR_URL, getUser().getId(), previous, extension);
        if (oldAvatarUrl == null) return null;

        return new ImageProxy(getJDA(), oldAvatarUrl, previous, extension);
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
     * The url of the new avatar
     *
     * @return The url of the new avatar
     */
    @Nullable //TODO remove
    public String getNewAvatarUrl()
    {
        return next == null ? null : String.format(User.AVATAR_URL, getUser().getId(), next, next.startsWith("a_") ? "gif" : "png");
    }

    //TODO docs
    @Nullable
    public ImageProxy getNewAvatar()
    {
        if (next == null) return null;

        final String extension = next.startsWith("a_") ? "gif" : "png";
        final String newAvatarUrl = String.format(User.AVATAR_URL, getUser().getId(), next, extension);

        return new ImageProxy(getJDA(), newAvatarUrl, next, extension);
    }
}
