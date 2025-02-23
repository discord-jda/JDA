package net.dv8tion.jda.internal.interactions.components.separator;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.separator.Separator;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;

import javax.annotation.Nonnull;

public class SeparatorImpl
        extends AbstractComponentImpl
        implements Separator, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final Spacing spacing;
    private final boolean isDivider;

    public SeparatorImpl(Spacing spacing, boolean isDivider)
    {
        this.spacing = spacing;
        this.isDivider = isDivider;
    }

    @Override
    public Spacing getSpacing()
    {
        return spacing;
    }

    @Override
    public boolean hasDivider()
    {
        return isDivider;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.SEPARATOR;
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
        return DataObject.empty()
                .put("type", getType().getKey())
                .put("divider", isDivider)
                .put("spacing", spacing.getKey());
    }
}
