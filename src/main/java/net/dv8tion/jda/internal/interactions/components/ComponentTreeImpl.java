package net.dv8tion.jda.internal.interactions.components;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentTree;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class ComponentTreeImpl implements ComponentTree
{
    private final List<MessageTopLevelComponentUnion> components;

    private ComponentTreeImpl(Collection<MessageTopLevelComponentUnion> components)
    {
        this.components = Helpers.copyAsUnmodifiableList(components);
    }

    public static ComponentTree of(Collection<? extends MessageTopLevelComponent> _components)
    {
        final Collection<MessageTopLevelComponentUnion> components = UnionUtil.membersToUnion(_components);
        // TODO-components-v2 - checks?

        return new ComponentTreeImpl(components);
    }

    @Nonnull
    @Override
    public List<MessageTopLevelComponentUnion> getComponents()
    {
        return components;
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
