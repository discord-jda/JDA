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
package net.dv8tion.jda.api.entities.channel.attribute

import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Channel with a modifiable voice status.
 * <br></br>This can be used to indicate what is going on to people outside the channel.
 */
interface IVoiceStatusChannel : Channel {
    @JvmField
    @get:Nonnull
    val status: String?

    /**
     * Change the current voice channel status.
     * <br></br>This can be configured by users who are connected
     * and have the [set voice channel status][net.dv8tion.jda.api.Permission.VOICE_SET_STATUS] permission.
     *
     * @param  status
     * The new status, or empty to unset
     *
     * @throws IllegalArgumentException
     * If the status is null or longer than {@value #MAX_STATUS_LENGTH} characters
     * @throws net.dv8tion.jda.api.exceptions.MissingAccessException
     * If the currently logged in account does not have [Permission.VIEW_CHANNEL] in this channel
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *
     *  * If the currently logged in account is **not connected** and does not have the [MANAGE_CHANNEL][Permission.MANAGE_CHANNEL] permission.
     *  * If the currently logged in account is **connected** and does not have the [VOICE_SET_STATUS][Permission.VOICE_SET_STATUS] permission.
     *
     *
     * @return [AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun modifyStatus(@Nonnull status: String?): AuditableRestAction<Void?>?

    companion object {
        /** The maximum length of a voice status {@value}  */
        const val MAX_STATUS_LENGTH = 500
    }
}
