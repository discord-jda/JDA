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

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

/**
 * Indicates that the discriminator of a {@link net.dv8tion.jda.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the User who changed their discriminator and their previous discriminator.
 *
 * <p>Identifier: {@code discriminator}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 *
 * @deprecated This will become obsolete in the future.
 *             Discriminators are being phased out and replaced by globally unique usernames.
 *             For more information, see <a href="https://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.
 */
@Deprecated
@ForRemoval
public class UserUpdateDiscriminatorEvent extends GenericUserUpdateEvent<String>
{
    public static final String IDENTIFIER = "discriminator";

    public UserUpdateDiscriminatorEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nonnull String oldDiscriminator)
    {
        super(api, responseNumber, user, oldDiscriminator, user.getDiscriminator(), IDENTIFIER);
    }

    /**
     * The old discriminator
     *
     * @return The old discriminator
     */
    @Nonnull
    public String getOldDiscriminator()
    {
        return getOldValue();
    }

    /**
     * The new discriminator
     *
     * @return The new discriminator
     */
    @Nonnull
    public String getNewDiscriminator()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public String getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public String getNewValue()
    {
        return super.getNewValue();
    }
}
