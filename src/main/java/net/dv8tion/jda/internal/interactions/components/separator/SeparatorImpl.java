package net.dv8tion.jda.internal.interactions.components.separator;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.separator.Separator;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

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

    @Override
    public boolean isMessageCompatible()
    {
        return true;
    }

    @Override
    public boolean isModalCompatible()
    {
        return false;
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
}
