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

package net.dv8tion.jda.internal.entities.automod;

import net.dv8tion.jda.api.entities.automod.KeywordPresetType;
import net.dv8tion.jda.api.entities.automod.TriggerMetadata;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class TriggerMetadataImpl implements TriggerMetadata
{
    private List<String> keywords;
    private EnumSet<KeywordPresetType> keywordPresets;

    public TriggerMetadataImpl() {}

    public TriggerMetadataImpl(@Nonnull List<String> keywords, @Nonnull EnumSet<KeywordPresetType> keywordPresets)
    {
        setKeywords(keywords);
        setKeywordPresets(keywordPresets);
    }

    @Override
    @Nonnull
    public List<String> getKeywords()
    {
        return Collections.unmodifiableList(keywords);
    }

    public TriggerMetadataImpl setKeywords(@Nonnull List<String> keywords)
    {
        this.keywords = keywords;
        return this;
    }

    @Override
    @Nonnull
    public EnumSet<KeywordPresetType> getKeywordPresetTypes()
    {
        return EnumSet.copyOf(keywordPresets);
    }

    public TriggerMetadataImpl setKeywordPresets(@Nonnull EnumSet<KeywordPresetType> keywordPresets)
    {
        this.keywordPresets = keywordPresets;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriggerMetadataImpl that = (TriggerMetadataImpl) o;
        return Objects.equals(keywords, that.keywords) && Objects.equals(keywordPresets, that.keywordPresets);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(keywords, keywordPresets);
    }

    @Override
    public String toString()
    {
        return "TriggerMetadataImpl(" +
                "keywords=" + keywords +
                ", keywordPresets=" + keywordPresets +
                ')';
    }
}
