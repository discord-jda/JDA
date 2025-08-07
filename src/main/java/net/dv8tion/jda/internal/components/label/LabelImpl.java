package net.dv8tion.jda.internal.components.label;

import net.dv8tion.jda.api.components.Components;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.label.LabelChildComponentUnion;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LabelImpl
        extends AbstractComponentImpl
        implements Label, ModalTopLevelComponentUnion
{
    private final int uniqueId;
    private final String label;
    private final String description;
    private final LabelChildComponentUnion child;

    public LabelImpl(@Nonnull DataObject object)
    {
        this(
                object.getInt("id"),
                object.getString("label"),
                object.getString("description", null),
                Components.parseComponent(LabelChildComponentUnion.class, object.getObject("child"))
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
        Checks.notNull(label, "Label");
        Checks.notNull(child, "Child");

        LabelChildComponentUnion childUnion = ComponentsUtil.safeUnionCast("child", child, LabelChildComponentUnion.class);
        return new LabelImpl(label, description, childUnion);
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
    public  LabelChildComponentUnion getChild()
    {
        return child;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("type", getType().getKey())
                .put("label", label)
                .put("description", description)
                .put("component", child);
    }
}
