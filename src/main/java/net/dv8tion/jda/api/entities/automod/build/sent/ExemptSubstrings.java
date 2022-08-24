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

package net.dv8tion.jda.api.entities.automod.build.sent;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Used to build an {@link net.dv8tion.jda.api.entities.automod.AutoModerationRule} with a {@link List} of {@link String exemptSubstrings}
 */
public interface ExemptSubstrings
{
    /**
     * Used to set the substrings that will be exempt from triggering the preset trigger type.
     *
     * @param  exemptSubstrings
     *         A {@link List} of {@link String}
     *
     * @return The current {@link ExemptSubstrings} instance.
     */
    ExemptSubstrings setExemptSubstrings(@Nonnull String... exemptSubstrings);
}
