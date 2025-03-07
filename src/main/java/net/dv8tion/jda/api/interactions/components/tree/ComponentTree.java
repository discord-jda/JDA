package net.dv8tion.jda.api.interactions.components.tree;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.utils.ComponentIterator;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.interactions.modals.tree.ModalComponentTree;
import net.dv8tion.jda.internal.interactions.components.tree.ComponentTreeImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.UnionUtil;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// TODO-components-v2 - docs
public interface ComponentTree<E extends ComponentUnion>
{
    @Nonnull
    static <E extends Component, T extends ComponentUnion> ComponentTree<T> of(@Nonnull Class<T> unionType, @Nonnull Collection<E> components)
    {
        Checks.notNull(unionType, "Component union type");
        Checks.noneNull(components, "Components");
        for (E component : components)
            Checks.check(unionType.isInstance(component), "Component %s is not a subclass of %s", component, unionType);
        return new ComponentTreeImpl<>(unionType, UnionUtil.membersToUnion(components));
    }

    @Nonnull
    static ComponentTree<ComponentUnion> of(@Nonnull Collection<? extends Component> components)
    {
        Checks.noneNull(components, "Components");
        return new ComponentTreeImpl<>(ComponentUnion.class, UnionUtil.membersToUnion(components));
    }

    @Nonnull
    static MessageComponentTree forMessage(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        return MessageComponentTree.of(components);
    }

    @Nonnull
    static ModalComponentTree forModal(@Nonnull Collection<? extends ModalTopLevelComponent> components)
    {
        return ModalComponentTree.of(components);
    }

    @Nonnull
    @Unmodifiable
    List<E> getComponents();

    @Nonnull
    default <T extends Component> List<T> findAll(@Nonnull Class<T> type)
    {
        return findAll(type, c -> true);
    }

    @Nonnull
    default <T extends Component> List<T> findAll(@Nonnull Class<T> type, @Nonnull Predicate<? super T> filter)
    {
        Checks.notNull(type, "Component type");
        Checks.notNull(filter, "Component filter");

        return ComponentIterator.createStream(getComponents())
                .filter(type::isInstance)
                .map(type::cast)
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Nonnull
    @CheckReturnValue
    ComponentTree<E> replace(ComponentReplacer replacer);

    @Nonnull
    @CheckReturnValue
    ComponentTree<E> disableAll();
}
