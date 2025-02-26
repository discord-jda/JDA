package net.dv8tion.jda.internal.interactions.components.section;

import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.section.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class SectionImpl
        extends AbstractComponentImpl
        implements Section, ContainerChildComponentUnion, IReplacerAware<Section>
{
    private final int uniqueId;
    private final List<SectionContentComponentUnion> children;
    private final SectionAccessoryComponentUnion accessory;

    public SectionImpl(Collection<SectionContentComponentUnion> children, SectionAccessoryComponentUnion accessory)
    {
        this(-1, children, accessory);
    }

    private SectionImpl(int uniqueId, Collection<SectionContentComponentUnion> children, SectionAccessoryComponentUnion accessory)
    {
        this.uniqueId = uniqueId;
        this.children = Helpers.copyAsUnmodifiableList(children);
        this.accessory = accessory;
    }

    public static Section of(Collection<? extends SectionContentComponent> _children)
    {
        final Collection<SectionContentComponentUnion> children = UnionUtil.membersToUnion(_children);
        // TODO-components-v2 - checks

        return new SectionImpl(children, null);
    }

    @Nonnull
    @Override
    public Section withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new SectionImpl(uniqueId, children, accessory);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Override
    public Section replace(ComponentReplacer<?> replacer)
    {
        final List<SectionContentComponentUnion> newContent = IReplacerAware.doReplace(
                SectionContentComponent.class,
                getComponents(),
                IReplacerAware.castReplacer(replacer),
                Function.identity()
        );

        final SectionAccessoryComponentUnion newAccessory = accessory != null ? IReplacerAware.doReplace(
                SectionAccessoryComponent.class,
                Collections.singletonList(accessory),
                IReplacerAware.castReplacer(replacer),
                newAccessories -> newAccessories.get(0)
        ) : null;

        return new SectionImpl(newContent, newAccessory);
    }

    @Nonnull
    @Override
    public List<SectionContentComponentUnion> getComponents()
    {
        return children;
    }

    @Nullable
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
        return children.iterator();
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.SECTION;
    }

    @Override
    public Section withAccessory(@Nullable SectionAccessoryComponent accessory)
    {
        // TODO-components-v2 Check union
        return new SectionImpl(uniqueId, children, (SectionAccessoryComponentUnion) accessory);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty();
        json.put("type", getType().getKey());
        json.put("components", DataArray.fromCollection(getComponents()));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (accessory != null)
            json.put("accessory", accessory);
        return json;
    }
}
