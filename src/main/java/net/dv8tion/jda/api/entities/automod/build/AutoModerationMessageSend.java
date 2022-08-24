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

package net.dv8tion.jda.api.entities.automod.build;

import net.dv8tion.jda.api.entities.automod.build.sent.Keyword;
import net.dv8tion.jda.api.entities.automod.build.sent.KeywordPreset;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Used to create a new {@link net.dv8tion.jda.api.entities.automod.AutoModerationRule},
 */
public interface AutoModerationMessageSend
{
    @Nonnull
    @CheckReturnValue
    Keyword keyword(String name);

    @Nonnull
    @CheckReturnValue
    KeywordPreset preset(String name);

    /**
     * Returns the {@link Keyword} instances that can be used to create a new {@link net.dv8tion.jda.api.entities.automod.AutoModerationRule}.
     *
     * @return {@link Keyword}
     */
    @Nonnull
    Keyword getKeyword();

    /**
     * Returns the {@link KeywordPreset} instances that can be used to create a new {@link net.dv8tion.jda.api.entities.automod.AutoModerationRule}.
     *
     * @return {@link KeywordPreset}
     */
    @Nonnull
    KeywordPreset getPreset();
}
