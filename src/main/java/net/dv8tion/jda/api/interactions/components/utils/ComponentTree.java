package net.dv8tion.jda.api.interactions.components.utils;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.ComponentTreeImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.UnionUtil;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

// TODO-components-v2 - docs
public interface ComponentTree<E extends ComponentUnion>
{
    @Nonnull
    static <E extends Component, T extends ComponentUnion> ComponentTree<T> of(@Nonnull Class<T> componentType, @Nonnull Collection<E> components)
    {
        Checks.notNull(componentType, "Component type");
        Checks.notNull(components, "Components");
        for (E component : components)
            Checks.check(componentType.isInstance(component), "Component %s is not a subclass of %s", component, componentType);
        return new ComponentTreeImpl<>(componentType, UnionUtil.membersToUnion(components));
    }

    @Nonnull
    @Unmodifiable
    List<E> getComponents();

    @Nonnull
    default <T extends Component> List<T> findAll(@Nonnull Class<? super T> type)
    {
        return findAll(type, c -> true);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    default <T extends Component> List<T> findAll(@Nonnull Class<? super T> type, @Nonnull Predicate<? super T> filter)
    {
        final List<T> elements = new ArrayList<>();
        final ComponentIterator iterator = ComponentIterator.create(getComponents());
        while (iterator.hasNext())
        {
            final Component component = iterator.next();
            if (!type.isInstance(component)) continue;

            final T t = (T) component;
            if (filter.test(t))
                elements.add(t);
        }
        return elements;
    }

    @Nonnull
    @CheckReturnValue
    ComponentTree<E> replace(ComponentReplacer replacer);

    @Nonnull
    @CheckReturnValue
    ComponentTree<E> disableAll();
}
