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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.SKU;
import net.dv8tion.jda.api.entities.SKUFlag;
import net.dv8tion.jda.api.entities.SKUType;

import java.util.Set;

import javax.annotation.Nonnull;

public class SKUImpl extends SkuSnowflakeImpl implements SKU {
    private final SKUType type;
    private final String name;
    private final String slug;
    private final Set<SKUFlag> flags;

    public SKUImpl(long id, SKUType type, String name, String slug, Set<SKUFlag> flags) {
        super(id);
        this.type = type;
        this.name = name;
        this.slug = slug;
        this.flags = flags;
    }

    @Override
    @Nonnull
    public SKUType getType() {
        return type;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public String getSlug() {
        return slug;
    }

    @Override
    @Nonnull
    public Set<SKUFlag> getFlags() {
        return flags;
    }
}
