package net.dv8tion.jda.internal.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface IReplacerAware<T extends Component> {
    T replace(ComponentReplacer<?> replacer);

    @SuppressWarnings("unchecked")
    static <C, E extends ComponentUnion> C doReplace(Iterable<E> children, ComponentReplacer<E> replacer, Function<List<E>, C> finisher)
    {
        List<E> newComponents = new ArrayList<>();
        for (E component : children)
        {
            if (replacer.getComponentType().isInstance(component))
                newComponents.add(replacer.apply(component));
            else if (component instanceof IReplacerAware)
                newComponents.add(((IReplacerAware<E>) component).replace(replacer));
            else
                newComponents.add(component);
        }

        return finisher.apply(newComponents);
    }

    @SuppressWarnings("unchecked")
    static <T extends Component> ComponentReplacer<T> castReplacer(ComponentReplacer<?> replacer) {
        return (ComponentReplacer<T>) replacer;
    }
}
