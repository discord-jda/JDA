package net.dv8tion.jda.internal.interactions.components.tree;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.modals.tree.ModalComponentTree;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.ComponentsUtil;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ModalComponentTreeImpl extends AbstractComponentTree<ModalTopLevelComponentUnion> implements ModalComponentTree
{
    private ModalComponentTreeImpl(Collection<ModalTopLevelComponentUnion> components)
    {
        super(components);
    }

    @Nonnull
    public static ModalComponentTree of(@Nonnull Collection<? extends ModalTopLevelComponent> _components)
    {
        // Empty trees are allowed
        Checks.noneNull(_components, "Components");

        final Collection<ModalTopLevelComponentUnion> components = ComponentsUtil.membersToUnion(_components, ModalTopLevelComponentUnion.class);
        return new ModalComponentTreeImpl(components);
    }

    @Nonnull
    @Override
    public ModalComponentTree replace(ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                ModalTopLevelComponent.class,
                components,
                replacer,
                ModalComponentTreeImpl::new
        );
    }

    @Nonnull
    @Override
    public ModalComponentTree disableAll()
    {
        return (ModalComponentTree) super.disableAll();
    }
}
