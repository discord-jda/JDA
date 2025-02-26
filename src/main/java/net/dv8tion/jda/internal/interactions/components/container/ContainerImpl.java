package net.dv8tion.jda.internal.interactions.components.container;

import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
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
    private final List<ContainerChildComponentUnion> children;

    private ContainerImpl(Collection<ContainerChildComponentUnion> children)
    {
        this(-1, children);
    }

    private ContainerImpl(int uniqueId, Collection<ContainerChildComponentUnion> children)
    {
        this.uniqueId = uniqueId;
        this.children = Helpers.copyAsUnmodifiableList(children);
    }

    public static Container of(Collection<? extends ContainerChildComponent> _children)
    {
        final Collection<ContainerChildComponentUnion> children = UnionUtil.membersToUnion(_children);
        // TODO-components-v2 - checks

        return new ContainerImpl(children);
    }

    @Nonnull
    @Override
    public Container withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new ContainerImpl(uniqueId, children);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Override
    public Container replace(ComponentReplacer<?> replacer)
    {
        return IReplacerAware.doReplace(
                getComponents(),
                IReplacerAware.castReplacer(replacer),
                ContainerImpl::new
        );
    }

    @Nonnull
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
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("components", DataArray.fromCollection(getComponents()));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }
}
