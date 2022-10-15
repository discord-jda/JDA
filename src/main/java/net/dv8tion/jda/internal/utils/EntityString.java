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

package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class EntityString
{
    private final Object entity;
    private Object type;
    private String name;
    private List<String> metadata;

    public EntityString(Object entity)
    {
        this.entity = entity;
    }

    public EntityString setType(@Nonnull Enum<?> type)
    {
        this.type = type.name();
        return this;
    }

    public EntityString setType(@Nonnull Object type)
    {
        this.type = type;
        return this;
    }

    public EntityString setName(@Nonnull String name)
    {
        this.name = name;
        return this;
    }

    public EntityString addMetadata(@Nullable String key, @Nullable Object value)
    {
        if (this.metadata == null) this.metadata = new ArrayList<>();

        this.metadata.add(key == null ? String.valueOf(value) : key + "=" + value);

        return this;
    }

    @Nonnull
    @Override
    public String toString()
    {
        final String entityName;
        if (this.entity instanceof String)
            entityName = (String) this.entity;
        else if (this.entity instanceof Class<?>)
            entityName = getCleanedClassName((Class<?>) this.entity);
        else
            entityName = getCleanedClassName(this.entity.getClass());

        final StringBuilder sb = new StringBuilder(entityName);
        if (this.type != null)
            sb.append('[').append(this.type).append(']');
        if (this.name != null)
            sb.append(':').append(this.name);

        final boolean isSnowflake = entity instanceof ISnowflake;
        if (isSnowflake || this.metadata != null)
        {
            final StringJoiner metadataJoiner = new StringJoiner(", ", "(", ")");
            if (isSnowflake)
                metadataJoiner.add("id=" + ((ISnowflake) entity).getId());
            if (this.metadata != null)
            {
                for (Object metadataItem : this.metadata)
                    metadataJoiner.add(metadataItem.toString());
            }

            sb.append(metadataJoiner);
        }

        return sb.toString();
    }

    @Nonnull
    private static String getCleanedClassName(@Nonnull Class<?> clazz)
    {
        String packageName = clazz.getPackage().getName();
        String fullName = clazz.getName();
        String simpleName = fullName.substring(packageName.length() + 1);

        return simpleName
                .replace("$", ".") //Clean up nested classes
                .replace("Impl", ""); //Don't expose Impl
    }
}
