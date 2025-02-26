package net.dv8tion.jda.api.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.StringSelectMenu;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Predicate;

public class ComponentReplacers
{
    private static class ComponentReplacerImpl<T extends Component> implements ComponentReplacer<T> {
        private final Class<T> type;
        private final Predicate<T> filter;
        private final Function<T, T> updater;

        private ComponentReplacerImpl(Class<T> type, Predicate<T> filter, Function<T, T> updater)
        {
            this.type = type;
            this.filter = filter;
            this.updater = updater;
        }

        @Nonnull
        @Override
        public Class<T> getComponentType()
        {
            return type;
        }

        @SuppressWarnings("unchecked")
        @Nonnull
        @Override
        public T apply(@Nonnull T oldComponent)
        {
            if (filter.test(oldComponent))
                return updater.apply(oldComponent);
            if (oldComponent instanceof IReplacerAware)
                return ((IReplacerAware<T>) oldComponent).replace(this);

            return oldComponent;
        }
    }

    private ComponentReplacers() {}

    // TODO-components-v2 - docs
    // TODO-components-v2 - Can the Class<T> be removed?
    public static <T extends IdentifiableComponent> ComponentReplacer<T> identifiableReplacer(Class<T> type, T toReplace, Function<T, T> update)
    {
        return new ComponentReplacerImpl<>(type, component -> component.getUniqueId() == toReplace.getUniqueId(), update);
    }

    // TODO-components-v2 - docs
    public static ComponentReplacer<Button> buttonReplacer(Predicate<Button> filter, Function<Button, Button> update)
    {
        return new ComponentReplacerImpl<>(Button.class, filter, update);
    }

    // TODO-components-v2 - docs
    public static ComponentReplacer<SelectMenu> selectMenuReplacer(Predicate<SelectMenu> filter, Function<SelectMenu, SelectMenu> update)
    {
        return new ComponentReplacerImpl<>(SelectMenu.class, filter, update);
    }

    // TODO-components-v2 - docs
    public static ComponentReplacer<StringSelectMenu> stringSelectMenuReplacer(Predicate<StringSelectMenu> filter, Function<StringSelectMenu, StringSelectMenu> update)
    {
        return new ComponentReplacerImpl<>(StringSelectMenu.class, filter, update);
    }

    // TODO-components-v2 - docs
    public static ComponentReplacer<EntitySelectMenu> entitySelectMenuReplacer(Predicate<EntitySelectMenu> filter, Function<EntitySelectMenu, EntitySelectMenu> update)
    {
        return new ComponentReplacerImpl<>(EntitySelectMenu.class, filter, update);
    }
}
