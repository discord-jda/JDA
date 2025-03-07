package net.dv8tion.jda.api.interactions.modals.tree;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.tree.ComponentTree;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.internal.interactions.components.tree.ModalComponentTreeImpl;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * Specialization of {@link ComponentTree} for {@link ModalTopLevelComponentUnion top-level modal components}
 *
 * @see ComponentTree
 */
public interface ModalComponentTree extends ComponentTree<ModalTopLevelComponentUnion>
{
    @Nonnull
    @Override
    ModalComponentTree disableAll();

    @Nonnull
    @Override
    ModalComponentTree replace(ComponentReplacer replacer);

    /**
     * Creates a {@link ModalComponentTree} from the given top-level modal components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link ModalComponentTree} containing the given components
     */
    @Nonnull
    static ModalComponentTree of(@Nonnull Collection<? extends ModalTopLevelComponent> components)
    {
        return ModalComponentTreeImpl.of(components);
    }

    /**
     * Creates a {@link ModalComponentTree} from the given top-level modal components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link ModalComponentTree} containing the given components
     */
    @Nonnull
    static ModalComponentTree of(@Nonnull ModalTopLevelComponent... components)
    {
        return of(Arrays.asList(components));
    }
}
