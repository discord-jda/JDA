package net.dv8tion.jda.api.interactions.components.tree;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.tree.MessageComponentTreeImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * Specialization of {@link ComponentTree} for {@link MessageTopLevelComponentUnion top-level message components}
 *
 * @see ComponentTree
 */
public interface MessageComponentTree extends ComponentTree<MessageTopLevelComponentUnion>
{
    @Nonnull
    @Override
    @CheckReturnValue
    MessageComponentTree replace(ComponentReplacer replacer);

    @Nonnull
    @Override
    @CheckReturnValue
    MessageComponentTree disableAll();

    /**
     * Creates a {@link MessageComponentTree} from the given top-level message components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link MessageComponentTree} containing the given components
     */
    @Nonnull
    static MessageComponentTree of(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        return MessageComponentTreeImpl.of(components);
    }

    /**
     * Creates a {@link MessageComponentTree} from the given top-level message components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link MessageComponentTree} containing the given components
     */
    @Nonnull
    static MessageComponentTree of(@Nonnull MessageTopLevelComponent... components)
    {
        return of(Arrays.asList(components));
    }
}
