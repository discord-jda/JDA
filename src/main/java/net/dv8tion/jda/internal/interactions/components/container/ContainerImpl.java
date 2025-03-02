package net.dv8tion.jda.internal.interactions.components.container;

import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.attribute.IDisableable;
import net.dv8tion.jda.api.interactions.components.container.Container;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
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
import java.util.Iterator;
import java.util.List;

public class ContainerImpl
        extends AbstractComponentImpl
        implements Container, MessageTopLevelComponentUnion, IReplacerAware<Container>
{
    private final int uniqueId;
    private final List<ContainerChildComponentUnion> components;
    private final boolean spoiler;
    private final Integer accentColor;

    private ContainerImpl(Collection<ContainerChildComponentUnion> components)
    {
        this(-1, components, false, null);
    }

    private ContainerImpl(int uniqueId, Collection<ContainerChildComponentUnion> components, boolean spoiler, Integer accentColor)
    {
        this.uniqueId = uniqueId;
        this.components = Helpers.copyAsUnmodifiableList(components);
        this.spoiler = spoiler;
        this.accentColor = accentColor;
    }

    public static Container of(Collection<? extends ContainerChildComponent> _children)
    {
        final Collection<ContainerChildComponentUnion> children = UnionUtil.membersToUnion(_children);
        // TODO-components-v2 - checks

        return new ContainerImpl(children);
    }

    public static ContainerImpl fromData(DataObject data)
    {
        Checks.notNull(data, "Data");
        if (data.getInt("type", 0) != Type.CONTAINER.getKey())
            throw new IllegalArgumentException("Data has incorrect type. Expected: " + Type.CONTAINER.getKey() + " Found: " + data.getInt("type"));

        final int uniqueId = data.getInt("id");
        final List<ContainerChildComponentUnion> components = ContainerChildComponentUnion.fromData(data.getArray("components"));
        final boolean spoiler = data.getBoolean("spoiler", false);
        final Integer accentColor = (Integer) data.opt("accent_color").orElse(null);

        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.CONTAINER;
    }

    @Nonnull
    @Override
    public Container withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Container withSpoiler(boolean spoiler)
    {
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Container withAccentColor(int accentColor)
    {
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Container withDisabled(boolean disabled)
    {
        return IReplacerAware.doReplace(
                ContainerChildComponent.class,
                components,
                ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)),
                components -> new ContainerImpl(uniqueId, components, spoiler, accentColor)
        );
    }

    @Nonnull
    @Override
    public Container asDisabled()
    {
        return this;
    }

    @Nonnull
    @Override
    public Container asEnabled()
    {
        return this;
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Override
    public Container replace(ComponentReplacer replacer)
    {
        return IReplacerAware.doReplace(
                ContainerChildComponent.class,
                getComponents(),
                replacer,
                components -> new ContainerImpl(uniqueId, components, spoiler, accentColor)
        );
    }

    @Nonnull
    @Override
    public List<ContainerChildComponentUnion> getComponents()
    {
        return components;
    }

    @Nullable
    @Override
    public Integer getAccentColorRaw()
    {
        return accentColor;
    }

    @Override
    public boolean isSpoiler()
    {
        return spoiler;
    }

    @Override
    public boolean isEmpty()
    {
        return components.isEmpty();
    }

    @Override
    public boolean isValid()
    {
        return false;
    }

    @Nonnull
    @Override
    public LayoutComponent<ContainerChildComponentUnion> createCopy()
    {
        return null;
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
    public Iterator<ContainerChildComponentUnion> iterator()
    {
        return getComponents().iterator();
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("components", DataArray.fromCollection(getComponents()))
                .put("spoiler", spoiler);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (accentColor != null)
            json.put("accent_color", accentColor & 0xFFFFFF);
        return json;
    }
}
