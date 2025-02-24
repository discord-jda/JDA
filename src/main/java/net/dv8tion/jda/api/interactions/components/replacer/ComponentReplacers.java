package net.dv8tion.jda.api.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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

    public static ComponentReplacer<Button> buttonReplacer(Predicate<Button> filter, Function<Button, Button> update)
    {
        return new ComponentReplacerImpl<>(Button.class, filter, update);
    }
}
