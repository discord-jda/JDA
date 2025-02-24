package net.dv8tion.jda.internal.interactions.components;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentTree;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComponentTreeImpl implements ComponentTree
{
    private final List<ComponentUnion> components;

    private ComponentTreeImpl(Collection<ComponentUnion> components)
    {
        this.components = new ArrayList<>(components);
    }

    public static ComponentTree of(Collection<? extends Component> _components)
    {
        final Collection<ComponentUnion> components = UnionUtil.membersToUnion(_components);
        // TODO-components-v2 - checks?

        return new ComponentTreeImpl(components);
    }

    @Nonnull
    @Override
    public <T extends Component> ComponentTree replace(ComponentReplacer<T> replacer)
    {
        return IReplacerAware.doReplace(
                components,
                IReplacerAware.castReplacer(replacer),
                ComponentTreeImpl::new
        );
    }
}
