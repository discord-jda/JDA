package net.dv8tion.jda.internal.interactions.components;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.modals.ModalComponentTree;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ModalComponentTreeImpl extends AbstractComponentTree<ModalTopLevelComponentUnion> implements ModalComponentTree
{
    private ModalComponentTreeImpl(Collection<ModalTopLevelComponentUnion> components)
    {
        super(components);
    }

    public static ModalComponentTree of(Collection<? extends ModalTopLevelComponent> _components)
    {
        final Collection<ModalTopLevelComponentUnion> components = UnionUtil.membersToUnion(_components);
        // TODO-components-v2 - checks?

        return new ModalComponentTreeImpl(components);
    }

    @Nonnull
    @Override
    public ModalComponentTree replace(ComponentReplacer replacer)
    {
        return IReplacerAware.doReplace(
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
