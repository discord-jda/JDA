package net.dv8tion.jda.internal.interactions.components.tree;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.tree.ComponentTree;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.ComponentsUtil;

import javax.annotation.Nonnull;
import java.util.Collection;

public final class ComponentTreeImpl<E extends ComponentUnion> extends AbstractComponentTree<E>
{
    private final Class<? extends Component> componentType;

    public ComponentTreeImpl(Class<? extends Component> componentType, Collection<E> components)
    {
        super(components);
        this.componentType = componentType;
    }

    @Nonnull
    @Override
    public ComponentTree<E> replace(ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                componentType,
                components,
                replacer,
                (newComponents) -> new ComponentTreeImpl<>(componentType, newComponents)
        );
    }
}
