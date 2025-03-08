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

package net.dv8tion.jda.internal.components.separator;

import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SeparatorImpl
        extends AbstractComponentImpl
        implements Separator, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final int uniqueId;
    private final Spacing spacing;
    private final boolean isDivider;

    public SeparatorImpl(DataObject obj)
    {
        this(
                obj.getInt("id"),
                Spacing.fromKey(obj.getInt("spacing", 1)),
                obj.getBoolean("divider", true)
        );
    }

    public SeparatorImpl(Spacing spacing, boolean isDivider)
    {
        this(-1, spacing, isDivider);
    }

    private SeparatorImpl(int uniqueId, Spacing spacing, boolean isDivider)
    {
        this.uniqueId = uniqueId;
        this.spacing = spacing;
        this.isDivider = isDivider;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.SEPARATOR;
    }

    @Nonnull
    @Override
    public Separator withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new SeparatorImpl(uniqueId, spacing, isDivider);
    }

    @Nonnull
    @Override
    public Separator withDivider(boolean divider)
    {
        return new SeparatorImpl(uniqueId, spacing, divider);
    }

    @Nonnull
    @Override
    public Separator withSpacing(@Nonnull Spacing spacing)
    {
        Checks.notNull(spacing, "Spacing");
        return new SeparatorImpl(uniqueId, spacing, isDivider);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Override
    public boolean isDivider()
    {
        return isDivider;
    }

    @Nonnull
    @Override
    public Spacing getSpacing()
    {
        return spacing;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("divider", isDivider)
                .put("spacing", spacing.getKey());
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof SeparatorImpl)) return false;
        SeparatorImpl separator = (SeparatorImpl) o;
        return uniqueId == separator.uniqueId && isDivider == separator.isDivider && spacing == separator.spacing;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, spacing, isDivider);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("divider", isDivider)
                .addMetadata("spacing", spacing)
                .toString();
    }
}
