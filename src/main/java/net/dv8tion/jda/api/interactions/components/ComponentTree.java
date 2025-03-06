package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

// TODO-components-v2 - docs
public interface ComponentTree<E extends ComponentUnion>
{
    @Nonnull
    @Unmodifiable
    List<E> getComponents();

    @Nonnull
    @CheckReturnValue
    ComponentTree<E> replace(ComponentReplacer replacer);

    @Nonnull
    @CheckReturnValue
    ComponentTree<E> disableAll();
}
