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
 * Indicates that a {@link net.dv8tion.jda.api.entities.StageInstance StageInstance} updated its {@code topic}.
 *
 * <p>Can be used to retrieve the topic.
 *
 * <p>Identifier: {@code topic}
 */
@SuppressWarnings("ConstantConditions")
public class StageInstanceUpdateTopicEvent extends GenericStageInstanceUpdateEvent<String>
{
    public static final String IDENTIFIER = "topic";

    public StageInstanceUpdateTopicEvent(@Nonnull JDA api, long responseNumber, @Nonnull StageInstance stageInstance, String previous)
    {
        super(api, responseNumber, stageInstance, previous, stageInstance.getTopic(), IDENTIFIER);
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
