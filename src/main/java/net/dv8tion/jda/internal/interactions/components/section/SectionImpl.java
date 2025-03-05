package net.dv8tion.jda.internal.interactions.components.section;

import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.attribute.IDisableable;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.section.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.UnionUtil;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class SectionImpl
        extends AbstractComponentImpl
        implements Section, MessageTopLevelComponentUnion, ContainerChildComponentUnion, IReplacerAware<Section>
{
    private final int uniqueId;
    private final List<SectionContentComponentUnion> components;
    private final SectionAccessoryComponentUnion accessory;

    public SectionImpl(Collection<SectionContentComponentUnion> components, SectionAccessoryComponentUnion accessory)
    {
        this(-1, components, accessory);
    }

    private SectionImpl(int uniqueId, Collection<SectionContentComponentUnion> components, SectionAccessoryComponentUnion accessory)
    {
        this.uniqueId = uniqueId;
        this.components = Helpers.copyAsUnmodifiableList(components);
        this.accessory = accessory;
    }

    public static Section of(SectionAccessoryComponent _accessory, Collection<? extends SectionContentComponent> _components)
    {
        Checks.notNull(_accessory, "Accessory");
        Checks.notEmpty(_components, "Components");
        Checks.noneNull(_components, "Components");

        final Collection<SectionContentComponentUnion> components = UnionUtil.membersToUnion(_components);
        final SectionAccessoryComponentUnion accessory = UnionUtil.safeUnionCast("accessory", _accessory, SectionAccessoryComponentUnion.class);
        // TODO-components-v2 - checks

        return new SectionImpl(components, accessory);
    }

    public static SectionImpl fromData(DataObject data)
    {
        Checks.notNull(data, "Data");
        if (data.getInt("type", 0) != Type.SECTION.getKey())
            throw new IllegalArgumentException("Data has incorrect type. Expected: " + Type.SECTION.getKey() + " Found: " + data.getInt("type"));

        final int uniqueId = data.getInt("id");
        final List<SectionContentComponentUnion> components = SectionContentComponentUnion.fromData(data.getArray("components"));
        final SectionAccessoryComponentUnion accessory = SectionAccessoryComponentUnion.fromData(data.getObject("accessory"));

        return new SectionImpl(uniqueId, components, accessory);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.SECTION;
    }

    @Nonnull
    @Override
    public Section withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new SectionImpl(uniqueId, components, accessory);
    }

    @Nonnull
    @Override
    public Section withDisabled(boolean disabled)
    {
        return IReplacerAware.doReplace(
                SectionContentComponent.class,
                components,
                ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)),
                components -> new SectionImpl(uniqueId, components, accessory)
        );
    }

    @Nonnull
    @Override
    public Section asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @Override
    public Section asEnabled()
    {
        return withDisabled(false);
    }

    @Override
    public Section replace(ComponentReplacer replacer)
    {
        final List<SectionContentComponentUnion> newContent = IReplacerAware.doReplace(
                SectionContentComponent.class,
                getContentComponents(),
                replacer,
                Function.identity()
        );

        final SectionAccessoryComponentUnion newAccessory = accessory != null ? IReplacerAware.doReplace(
                SectionAccessoryComponent.class,
                Collections.singletonList(accessory),
                replacer,
                newAccessories -> newAccessories.get(0)
        ) : null;

        return new SectionImpl(newContent, newAccessory);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    @Unmodifiable
    public List<SectionContentComponentUnion> getContentComponents()
    {
        return components;
    }

    @Nonnull
    @Override
    public SectionAccessoryComponentUnion getAccessory()
    {
        return accessory;
    }

    @Override
    public boolean isEmpty()
    {
        return components.isEmpty() && accessory == null;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Nonnull
    @Override
    public Section createCopy()
    {
        return this;
    }

    @Nullable
    @Override
    @Deprecated
    public ItemComponent updateComponent(@Nonnull ItemComponent component, @Nullable ItemComponent newComponent)
    {
        throw new UnsupportedOperationException("This layout is immutable, use ComponentTree#replace instead");
    }

    @Nullable
    @Override
    @Deprecated
    public ItemComponent updateComponent(@Nonnull String id, @Nullable ItemComponent newComponent)
    {
        throw new UnsupportedOperationException("This layout is immutable, use ComponentTree#replace instead");
    }

    @Nonnull
    @Override
    public Iterator<SectionContentComponentUnion> iterator()
    {
        return components.iterator();
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty();
        json.put("type", getType().getKey());
        json.put("components", DataArray.fromCollection(getContentComponents()));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (accessory != null)
            json.put("accessory", accessory);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof SectionImpl)) return false;
        SectionImpl that = (SectionImpl) o;
        return uniqueId == that.uniqueId && Objects.equals(components, that.components) && Objects.equals(accessory, that.accessory);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, components, accessory);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("components", components)
                .toString();
    }
}
