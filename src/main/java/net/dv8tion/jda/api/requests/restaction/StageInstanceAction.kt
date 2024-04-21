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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.entities.StageInstance
import net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel
import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Specialized [RestAction] used to create a [StageInstance]
 *
 * @see net.dv8tion.jda.api.entities.channel.concrete.StageChannel.createStageInstance
 */
interface StageInstanceAction : RestAction<StageInstance?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): StageInstanceAction?
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): StageInstanceAction?
    @Nonnull
    override fun deadline(timestamp: Long): StageInstanceAction?

    /**
     * Sets the topic for the stage instance.
     * <br></br>This shows up in stage discovery and in the stage view.
     *
     * @param  topic
     * The topic, must be 1-120 characters long
     *
     * @throws IllegalArgumentException
     * If the topic is null, empty, or longer than 120 characters
     *
     * @return The StageInstanceAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    fun setTopic(@Nonnull topic: String?): StageInstanceAction?

    /**
     * Sets the [PrivacyLevel][net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel] for the stage instance.
     * <br></br>This indicates whether guild lurkers are allowed to join the stage instance or only guild members.
     *
     * @param  level
     * The [net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel]
     *
     * @throws IllegalArgumentException
     * If the privacy level is null, [UNKNOWN][net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel.UNKNOWN],
     * or [PUBLIC][net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel.PUBLIC].
     *
     * @return The StageInstanceAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    @Suppress("deprecation")
    fun setPrivacyLevel(@Nonnull level: PrivacyLevel?): StageInstanceAction?
}
