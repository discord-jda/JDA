package net.dv8tion.jda.internal.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface IReplacerAware<T extends Component>
{
    T replace(ComponentReplacer replacer);

    @SuppressWarnings("unchecked")
    static <R, E extends ComponentUnion> R doReplace(
            // This isn't '? extends E' as users are not required to return unions
            Class<? extends Component> expectedChildrenType,
            Iterable<E> children,
            ComponentReplacer replacer,
            Function<List<E>, R> finisher
    )
    {
        List<E> newComponents = new ArrayList<>();
        for (E component : children)
        {
            Component newComponent = replacer.apply(component);
            // If it returned a different component, then use it and don't try to recurse
            if (newComponent != component)
            {
                Checks.checkComponentType(expectedChildrenType, component, newComponent);
            }
            else if (component instanceof IReplacerAware)
            {
                newComponent = ((IReplacerAware<?>) component).replace(replacer);
                Checks.checkComponentType(expectedChildrenType, component, newComponent);
            }
            newComponents.add((E) newComponent);
        }

        return finisher.apply(newComponents);
    }
}
