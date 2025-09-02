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

package net.dv8tion.jda.internal.components.label;

import net.dv8tion.jda.api.components.Components;
import net.dv8tion.jda.api.components.ModalTopLevelComponentUnion;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.label.LabelChildComponentUnion;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class LabelImpl
        extends AbstractComponentImpl
        implements Label, ModalTopLevelComponentUnion
{
    private final int uniqueId;
    private final String label;
    private final String description;
    private final LabelChildComponentUnion child;

    public LabelImpl(@Nonnull ComponentDeserializer deserializer, @Nonnull DataObject object)
    {
        this(
            object.getInt("id", -1),
            object.getString("label"),
            object.getString("description", null),
            deserializer.deserializeAs(LabelChildComponentUnion.class, object.getObject("component"))
        );
    }

    public LabelImpl(@Nonnull DataObject object)
    {
        this(
                object.getInt("id", -1),
                object.getString("label"),
                object.getString("description", null),
                Components.parseComponent(LabelChildComponentUnion.class, object.getObject("component"))
        );
    }

    public LabelImpl(@Nonnull String label, @Nullable String description, @Nonnull LabelChildComponentUnion child)
    {
        this(-1, label, description, child);
    }

    private LabelImpl(int uniqueId, @Nonnull String label, @Nullable String description, @Nonnull LabelChildComponentUnion child)
    {
        this.uniqueId = uniqueId;
        this.label = label;
        this.description = description;
        this.child = child;
    }

    public static Label of(@Nonnull String label, @Nullable String description, @Nonnull LabelChildComponent child)
    {
        Checks.notBlank(label, "Label");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        Checks.notNull(child, "Child");
        if (description != null)
        {
            Checks.notBlank(description, "Description");
            Checks.notLonger(description, DESCRIPTION_MAX_LENGTH, "Description");
        }

        LabelChildComponentUnion childUnion = ComponentsUtil.safeUnionCast("child", child, LabelChildComponentUnion.class);
        return new LabelImpl(label, description, childUnion);
    }

    @Nonnull
    @Override
    public Label withLabel(@Nonnull String label)
    {
        return of(label, this.description, this.child);
    }

    @Nonnull
    @Override
    public Label withDescription(@Nullable String description)
    {
        return of(this.label, description, this.child);
    }

    @Nonnull
    @Override
    public Label withChild(@Nonnull LabelChildComponent child)
    {
        return of(this.label, this.description, child);
    }

    @Nonnull
    @Override
    public LabelImpl withUniqueId(int uniqueId)
    {
        return new LabelImpl(uniqueId, label, description, child);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.LABEL;
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public String getLabel()
    {
        return label;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return description;
    }

    @Nonnull
    @Override
    public LabelChildComponentUnion getChild()
    {
        return child;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject obj = DataObject.empty()
                .put("type", getType().getKey())
                .put("label", label)
                .put("description", description)
                .put("component", child);
        if (uniqueId >= 0)
            obj.put("id", uniqueId);

        return obj;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
            .addMetadata("id", uniqueId)
            .addMetadata("label", label)
            .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (!(o instanceof LabelImpl)) return false;
        LabelImpl that = (LabelImpl) o;
        return uniqueId == that.uniqueId
            && Objects.equals(label, that.label)
            && Objects.equals(description, that.description)
            && Objects.equals(child, that.child);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, label, description, child);
    }
}
