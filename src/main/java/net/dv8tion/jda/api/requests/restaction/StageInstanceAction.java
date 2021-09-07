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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Specialized {@link RestAction} used to create a {@link StageInstance}
 *
 * @see net.dv8tion.jda.api.entities.StageChannel#createStageInstance(String)
 */
public interface StageInstanceAction extends RestAction<StageInstance>
{
    @NotNull
    @Override
    StageInstanceAction setCheck(@Nullable BooleanSupplier checks);

    @NotNull
    @Override
    StageInstanceAction timeout(long timeout, @NotNull TimeUnit unit);

    @NotNull
    @Override
    StageInstanceAction deadline(long timestamp);

    /**
     * Sets the topic for the stage instance.
     * <br>This shows up in stage discovery and in the stage view.
     *
     * @param  topic
     *         The topic, must be 1-120 characters long
     *
     * @throws IllegalArgumentException
     *         If the topic is null, empty, or longer than 120 characters
     *
     * @return The StageInstanceAction for chaining
     */
    @NotNull
    @CheckReturnValue
    StageInstanceAction setTopic(@NotNull String topic);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel PrivacyLevel} for the stage instance.
     * <br>This indicates whether guild lurkers are allowed to join the stage instance or only guild members.
     *
     * @param  level
     *         The {@link net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel}
     *
     * @throws IllegalArgumentException
     *         If the provided level is null or {@link net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel#UNKNOWN UNKNOWN}
     *
     * @return The StageInstanceAction for chaining
     */
    @NotNull
    @CheckReturnValue
    StageInstanceAction setPrivacyLevel(@NotNull StageInstance.PrivacyLevel level);
}
