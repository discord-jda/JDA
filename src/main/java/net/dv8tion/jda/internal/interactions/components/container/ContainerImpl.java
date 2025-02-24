package net.dv8tion.jda.internal.interactions.components.container;

import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.Container;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.replacer.IReplaceable;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ContainerImpl
        extends AbstractComponentImpl
        implements Container, MessageTopLevelComponentUnion
{
    private final List<ContainerChildComponentUnion> children;

    private ContainerImpl(Collection<ContainerChildComponentUnion> children)
    {
        this.children = new ArrayList<>(children);
    }

    public static Container of(Collection<? extends ContainerChildComponent> _children)
    {
        final Collection<ContainerChildComponentUnion> children = UnionUtil.membersToUnion(_children);
        // TODO-components-v2 - checks

        return new ContainerImpl(children);
    }

    @Override
    public Container replace(ComponentReplacer<ContainerChildComponentUnion> replacer)
    {
        List<ContainerChildComponentUnion> newComponents = new ArrayList<>();
        for (ContainerChildComponentUnion component : getComponents())
        {
            if (replacer.getComponentType().isInstance(component))
                newComponents.add(replacer.apply(component));
            else if (component instanceof IReplaceable)
                newComponents.add((ContainerChildComponentUnion) ((IReplaceable) component).replace(replacer));
            else
                newComponents.add(component);
        }

        return new ContainerImpl(newComponents);
    }

    @Override
    public List<ContainerChildComponentUnion> getComponents()
    {
        return children;
    }

    @Nullable
    @Override
    public Integer getAccentColorRaw()
    {
        return 0;
    }

    @Override
    public boolean isSpoiler()
    {
        return false;
    }

    @Nonnull
    @Override
    public LayoutComponent<ContainerChildComponentUnion> withDisabled(boolean disabled)
    {
        return this;
    }

    @Nonnull
    @Override
    public LayoutComponent<ContainerChildComponentUnion> asDisabled()
    {
        return this;
    }

    @Nonnull
    @Override
    public LayoutComponent<ContainerChildComponentUnion> asEnabled()
    {
        return this;
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

    @Nonnull
    @Override
    public Iterator<ContainerChildComponentUnion> iterator()
    {
        return getComponents().iterator();
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.CONTAINER;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("type", getType().getKey())
                .put("components", DataArray.fromCollection(getComponents()));
    }
}
