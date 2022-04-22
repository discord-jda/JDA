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

package net.dv8tion.jda.internal.entities.sticker;

import net.dv8tion.jda.api.entities.sticker.RichSticker;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public class RichStickerImpl extends StickerItemImpl implements RichSticker
{
    private final Type type;
    private final Set<String> tags;
    private final String description;

    public RichStickerImpl(long id, StickerFormat format, String name,
                           Type type, Set<String> tags, String description)
    {
        super(id, format, name);
        this.type = type;
        this.tags = Collections.unmodifiableSet(tags);
        this.description = description;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return type;
    }

    @Nonnull
    @Override
    public Set<String> getTags()
    {
        return tags;
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return description;
    }
}
