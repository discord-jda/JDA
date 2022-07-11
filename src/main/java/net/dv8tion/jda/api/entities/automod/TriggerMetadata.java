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

package net.dv8tion.jda.api.entities.automod;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents additional data used to determine whether a rule should be executed or not.
 */
public interface TriggerMetadata
{
    /**
     * Returns the substrings which will be searched for in content.
     * <p>
     * These substrings can be a phrase which contains multiple words.
     * <p>
     * Wildcard symbols ({@code *}) can also be used to customize how each keyword is matched.
     * <p>
     * Associated trigger type is {@link TriggerType#KEYWORD}.
     * </p>
     * @return A {@link List} of {@link String}
     */
    @Nonnull
    List<String> getKeywords();

    /**
     * Returns the internally pre-defined word sets which will be searched for in content.
     * <p>
     * Associated trigger type is {@link TriggerType#KEYWORD_PRESET}.
     * </p>
     * @return A {@link List} of {@link KeywordPresetType KeywordPresets}
     */
    @Nonnull
    List<KeywordPresetType> getKeywordPresetTypes();
}
