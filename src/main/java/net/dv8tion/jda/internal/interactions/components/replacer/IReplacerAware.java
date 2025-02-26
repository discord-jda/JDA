package net.dv8tion.jda.internal.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface IReplacerAware<T extends Component> {
    T replace(ComponentReplacer<?> replacer);

    @SuppressWarnings("unchecked")
    static <C, E extends ComponentUnion> C doReplace(
            Class<? extends Component> expectedChildrenType,
            Iterable<E> children,
            ComponentReplacer<E> replacer,
            Function<List<E>, C> finisher
    )
    {
        List<E> newComponents = new ArrayList<>();
        for (E component : children)
        {
            if (replacer.getComponentType().isInstance(component))
            {
                final E newComponent = replacer.apply(component);
                Checks.checkComponentType(expectedChildrenType, component, newComponent);
                newComponents.add(newComponent);
            }
            else if (component instanceof IReplacerAware)
            {
                final E newComponent = ((IReplacerAware<E>) component).replace(replacer);
                Checks.checkComponentType(expectedChildrenType, component, newComponent);
                newComponents.add(newComponent);
            }
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
