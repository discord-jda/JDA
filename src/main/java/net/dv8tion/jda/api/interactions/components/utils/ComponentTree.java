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
import java.util.Collection;
import java.util.List;

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
    @CheckReturnValue
    ComponentTree<E> replace(ComponentReplacer replacer);

    @Nonnull
    @CheckReturnValue
    ComponentTree<E> disableAll();
}
