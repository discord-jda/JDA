package net.dv8tion.jda.api.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.StringSelectMenu;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Predicate;

public class ComponentReplacers
{
    static class ComponentReplacerImpl<T extends Component> implements ComponentReplacer {
        private final Class<? super T> type;
        private final Predicate<? super T> filter;
        private final Function<? super T, Component> updater;

        ComponentReplacerImpl(Class<? super T> type, Predicate<? super T> filter, Function<? super T, Component> updater)
        {
            this.type = type;
            this.filter = filter;
            this.updater = updater;
        }

        @SuppressWarnings("unchecked")
        @Nonnull
        @Override
        public Component apply(@Nonnull Component oldComponent)
        {
            if (!type.isInstance(oldComponent))
                return oldComponent;

            if (filter.test((T) oldComponent))
                return updater.apply((T) oldComponent);

            return oldComponent;
        }
    }

    private ComponentReplacers() {}

    // TODO-components-v2 - Another solution to all of these methods below:
    //  1. componentTree.replace(ComponentReplacer.button(id, Button::asDisabled))
    //  2. componentTree.replace(Button.class, b -> true, Button::asDisabled)

    // TODO-components-v2 - docs
    // TODO-components-v2 - Can the Class<T> be removed?
    public static <T extends IdentifiableComponent> ComponentReplacer identifiableReplacer(Class<T> type, T toReplace, Function<T, T> update)
    {
        return new ComponentReplacerImpl<>(type, component -> component.getUniqueId() == toReplace.getUniqueId(), update);
    }

    public static <T extends Component> ComponentReplacer generic(Class<? super T> type, Predicate<? super T> filter, Function<? super T, T> update)
    {
        ComponentReplacer b = null;
        ComponentReplacer ac = null;
        ComponentReplacer s1 = null;
        ComponentReplacer s2 = null;

        all(b, ac, s1, s2);

        return new ComponentReplacerImpl<>(type, filter, update);
    }

    public static ComponentReplacer all(ComponentReplacer first, ComponentReplacer... others)
    {
        return new ComponentReplacer()
        {
            @Nonnull
            @Override
            public Component apply(@Nonnull Component oldComponent)
            {
                Component newComponent = first.apply(oldComponent);
                for (ComponentReplacer other : others)
                    newComponent = other.apply(newComponent);
                return newComponent;
            }
        };
    }

    // TODO-components-v2 - docs
    public static ComponentReplacer buttonReplacer(Predicate<Button> filter, Function<Button, Button> update)
    {
        return new ComponentReplacerImpl<>(Button.class, filter, update);
    }

    // TODO-components-v2 - docs
    public static ComponentReplacer selectMenuReplacer(Predicate<SelectMenu> filter, Function<SelectMenu, SelectMenu> update)
    {
        return new ComponentReplacerImpl<>(SelectMenu.class, filter, update);
    }

    // TODO-components-v2 - docs
    public static ComponentReplacer stringSelectMenuReplacer(Predicate<StringSelectMenu> filter, Function<StringSelectMenu, StringSelectMenu> update)
    {
        return new ComponentReplacerImpl<>(StringSelectMenu.class, filter, update);
    }

    // TODO-components-v2 - docs
    public static ComponentReplacer entitySelectMenuReplacer(Predicate<EntitySelectMenu> filter, Function<EntitySelectMenu, EntitySelectMenu> update)
    {
        return new ComponentReplacerImpl<>(EntitySelectMenu.class, filter, update);
    }
}
