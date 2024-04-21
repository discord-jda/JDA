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
package net.dv8tion.jda.api.events.user.update

import net.dv8tion.jda.annotations.ForRemoval
import javax.annotation.Nonnull

/**
 * Indicates that the discriminator of a [User][net.dv8tion.jda.api.entities.User] changed.
 *
 *
 * Can be used to retrieve the User who changed their discriminator and their previous discriminator.
 *
 *
 * Identifier: `discriminator`
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * Additionally, this event requires the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 *
 */
@ForRemoval @Deprecated(
    """This will become obsolete in the future.
              Discriminators are being phased out and replaced by globally unique usernames.
              For more information, see <a href=""" https ://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.") 
    class UserUpdateDiscriminatorEvent (@Nonnull api:JDA?, responseNumber:kotlin.Long, @Nonnull user:net.dv8tion.jda.api.entities.User?, @Nonnull oldDiscriminator:kotlin.String?) : GenericUserUpdateEvent<kotlin.String?>(api, responseNumber, user, oldDiscriminator, user.getDiscriminator(), UserUpdateDiscriminatorEvent.Companion.IDENTIFIER) {
    /**
     * The old discriminator
     *
     * @return The old discriminator
     */
    @Nonnull
    fun getOldDiscriminator(): String {
        return getOldValue()
    }

    /**
     * The new discriminator
     *
     * @return The new discriminator
     */
    @Nonnull
    fun getNewDiscriminator(): String {
        return getNewValue()
    }

    @Nonnull
    fun getOldValue(): String {
        return super.getOldValue()
    }

    @Nonnull
    fun getNewValue(): String {
        return super.getNewValue()
    }
    companion object {
        val IDENTIFIER = "discriminator"
    }
}
