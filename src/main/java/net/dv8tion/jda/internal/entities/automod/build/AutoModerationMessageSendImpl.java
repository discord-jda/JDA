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

package net.dv8tion.jda.internal.entities.automod.build;

import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.build.AutoModerationMessageSend;
import net.dv8tion.jda.api.entities.automod.build.sent.ExemptSubstrings;
import net.dv8tion.jda.api.entities.automod.build.sent.Keyword;
import net.dv8tion.jda.api.entities.automod.build.sent.KeywordPreset;
import net.dv8tion.jda.internal.entities.automod.build.sent.ExemptSubstringsImpl;
import net.dv8tion.jda.internal.entities.automod.build.sent.KeywordImpl;
import net.dv8tion.jda.internal.entities.automod.build.sent.KeywordPresetImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class AutoModerationMessageSendImpl implements AutoModerationMessageSend
{
    private final EventType eventType = EventType.MESSAGE_SEND;
    private Keyword keyword;
    private KeywordPreset preset;
    private ExemptSubstrings exemptSubstrings;

    @Nonnull
    @Override
    @CheckReturnValue
    public Keyword keyword(String name)
    {
        keyword = new KeywordImpl(name, eventType);
        return keyword;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public KeywordPreset preset(String name)
    {
        preset = new KeywordPresetImpl(name, eventType);
        return preset;
    }

    @NotNull
    @Override
    public ExemptSubstrings exemptSubstrings(String name)
    {
        exemptSubstrings = new ExemptSubstringsImpl(name, eventType);
        return exemptSubstrings;
    }

    @Override
    @Nonnull
    public Keyword getKeyword()
    {
        return keyword;
    }

    @Override
    @Nonnull
    public KeywordPreset getPreset()
    {
        return preset;
    }

    @NotNull
    @Override
    public ExemptSubstrings getExemptSubstrings()
    {
        return exemptSubstrings;
    }
}
