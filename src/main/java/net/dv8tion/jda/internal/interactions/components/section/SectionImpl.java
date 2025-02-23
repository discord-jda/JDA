package net.dv8tion.jda.internal.interactions.components.section;

import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.section.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class SectionImpl
        extends AbstractComponentImpl
        implements Section, ContainerChildComponentUnion
{
    private final List<SectionContentComponentUnion> children;
    private SectionAccessoryComponentUnion accessory;

    public SectionImpl(Collection<SectionContentComponentUnion> children, SectionAccessoryComponentUnion accessory)
    {
        this.children = new ArrayList<>(children);
        this.accessory = accessory;
    }

    public static Section of(Collection<? extends SectionContentComponent> _children)
    {
        final Collection<SectionContentComponentUnion> children = UnionUtil.membersToUnion(_children);
        // TODO-components-v2 - checks

        return new SectionImpl(children, null);
    }

    @Override
    public List<SectionContentComponentUnion> getComponents()
    {
        return children;
    }

    @Override
    public SectionAccessoryComponentUnion getAccessory()
    {
        return accessory;
    }

    @Nonnull
    @Override
    public LayoutComponent withDisabled(boolean disabled)
    {
        return null;
    }

    @Nonnull
    @Override
    public LayoutComponent asDisabled()
    {
        return null;
    }

    @Nonnull
    @Override
    public LayoutComponent asEnabled()
    {
        return null;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Nonnull
    @Override
    public LayoutComponent createCopy()
    {
        return null;
    }

    @Nonnull
    @Override
    public Iterator iterator()
    {
        return Arrays.asList(children).iterator();
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.SECTION;
    }

    @Override
    public Section withAccessory(SectionAccessoryComponent accessory)
    {
        this.accessory = (SectionAccessoryComponentUnion) accessory;
        return this;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject data = DataObject.empty();
        data.put("type", getType().getKey());
        data.put("components", DataArray.fromCollection(getComponents()));
        if (accessory != null)
            data.put("accessory", accessory);
        return data;
    }
}
