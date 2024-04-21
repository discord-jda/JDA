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
package net.dv8tion.jda.api.managers

import net.dv8tion.jda.api.entities.StageInstance
import net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for a [StageInstance][net.dv8tion.jda.api.entities.StageInstance].
 *
 *
 * **Example**
 * <pre>`manager.setTopic("LMAO JOIN FOR FREE NITRO")
 * .setPrivacyLevel(PrivacyLevel.PUBLIC)
 * .queue();
 * manager.reset(ChannelManager.TOPIC | ChannelManager.PRIVACY_LEVEL)
 * .setTopic("Talent Show | WINNER GETS FREE NITRO")
 * .setPrivacyLevel(PrivacyLevel.GUILD_ONLY)
 * .queue();
`</pre> *
 *
 * @see net.dv8tion.jda.api.entities.StageInstance.getManager
 */
interface StageInstanceManager : Manager<StageInstanceManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(ChannelManager.TOPIC | ChannelManager.PRIVACY_LEVEL);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.TOPIC]
     *  * [.PRIVACY_LEVEL]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return StageInstanceManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): StageInstanceManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(ChannelManager.TOPIC, ChannelManager.PRIVACY_LEVEL);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.TOPIC]
     *  * [.PRIVACY_LEVEL]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return StageInstanceManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): StageInstanceManager?

    @get:Nonnull
    val stageInstance: StageInstance?

    /**
     * Sets the topic for this stage instance.
     * <br></br>This shows up in stage discovery and in the stage view.
     *
     * @param  topic
     * The topic or null to reset, must be 1-120 characters long
     *
     * @throws IllegalArgumentException
     * If the topic is longer than 120 characters
     *
     * @return StageInstanceManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setTopic(topic: String?): StageInstanceManager?

    /**
     * Sets the [PrivacyLevel][net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel] for this stage instance.
     * <br></br>This indicates whether guild lurkers are allowed to join the stage instance or only guild members.
     *
     * @param  level
     * The privacy level
     *
     * @throws IllegalArgumentException
     * If the privacy level is null, [UNKNOWN][net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel.UNKNOWN],
     * or [PUBLIC][net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel.PUBLIC].
     *
     * @return StageInstanceManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    @Suppress("deprecation")
    fun setPrivacyLevel(@Nonnull level: PrivacyLevel?): StageInstanceManager?

    companion object {
        /** Used to reset the topic field  */
        const val TOPIC: Long = 1

        /** Used to reset the privacy level field  */
        const val PRIVACY_LEVEL = (1 shl 1).toLong()
    }
}
