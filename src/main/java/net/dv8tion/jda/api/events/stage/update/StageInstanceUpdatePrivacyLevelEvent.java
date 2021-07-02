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

package net.dv8tion.jda.api.events.stage.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.StageInstance;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.StageInstance StageInstance} updated its {@link net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel PrivacyLevel}.
 *
 * <p>Can be used to retrieve the privacy level.
 *
 * <p>Identifier: {@code privacy_level}
 */
@SuppressWarnings("ConstantConditions")
public class StageInstanceUpdatePrivacyLevelEvent extends GenericStageInstanceUpdateEvent<StageInstance.PrivacyLevel>
{
    public static final String IDENTIFIER = "privacy_level";

    public StageInstanceUpdatePrivacyLevelEvent(@Nonnull JDA api, long responseNumber, @Nonnull StageInstance stageInstance, @Nonnull StageInstance.PrivacyLevel previous)
    {
        super(api, responseNumber, stageInstance, previous, stageInstance.getPrivacyLevel(), IDENTIFIER);
    }

    @Nonnull
    @Override
    public StageInstance.PrivacyLevel getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public StageInstance.PrivacyLevel getNewValue()
    {
        return super.getNewValue();
    }
}
