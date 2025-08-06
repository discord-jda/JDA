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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.interactions.response.InteractionCallbackResponse;

import javax.annotation.Nonnull;

/**
 * Represents a Discord {@link ActivityInstanceResource Activity instance resource}.
 * <br>This is provided by {@link InteractionCallbackResponse#getActivityInstance()}.
 */
public interface ActivityInstanceResource
{
    /**
     * The instance ID of the launched activity.
     * <br>Users joining the activity will receive the same instance ID,
     * when all users leave, the instance will be closed and never used again.
     *
     * @return The instance ID of the launched activity
     */
    @Nonnull
    String getInstanceId();
}
