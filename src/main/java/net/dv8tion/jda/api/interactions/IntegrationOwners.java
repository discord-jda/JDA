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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.annotation.Nullable;

//TODO document
public interface IntegrationOwners
{
    //TODO document, not the same as the caller id!
    @Nullable
    UserSnowflake getUserIntegration();

    //TODO document, for user installed commands: guild id in a guild, 0 in bot dms
    // Still very unclear when the field is present, may be a discord bug
    @Nullable
    Long getGuildIntegration();
}
