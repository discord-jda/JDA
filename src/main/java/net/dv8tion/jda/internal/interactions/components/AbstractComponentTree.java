package net.dv8tion.jda.internal.interactions.components;

import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.attribute.IDisableable;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.utils.ComponentTree;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public abstract class AbstractComponentTree<E extends ComponentUnion> implements ComponentTree<E>
{
    protected final List<E> components;

    protected AbstractComponentTree(Collection<E> components)
    {
        this.components = Helpers.copyAsUnmodifiableList(components);
    }

    @Nonnull
    @Override
    public List<E> getComponents()
    {
        return components;
    }

    @Nonnull
    @Override
    public ComponentTree<E> disableAll()
    {
        return replace(ComponentReplacer.of(IDisableable.class, c -> true, IDisableable::asDisabled));
    }
}
