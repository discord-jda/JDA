package net.dv8tion.jda.api.interactions.modals.utils;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.tree.ComponentTree;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.internal.interactions.components.tree.ModalComponentTreeImpl;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

// TODO-components-v2 - docs
public interface ModalComponentTree extends ComponentTree<ModalTopLevelComponentUnion>
{
    @Nonnull
    @Override
    ModalComponentTree disableAll();

    @Nonnull
    @Override
    ModalComponentTree replace(ComponentReplacer replacer);

    static ModalComponentTree of(Collection<? extends ModalTopLevelComponent> components)
    {
        return ModalComponentTreeImpl.of(components);
    }

    static ModalComponentTree of(ModalTopLevelComponent... components)
    {
        return of(Arrays.asList(components));
    }
}
