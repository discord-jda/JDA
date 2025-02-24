package net.dv8tion.jda.api.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;

import javax.annotation.Nonnull;

public interface ComponentReplacer<T extends Component>
{
    @Nonnull
    Class<T> getComponentType();

    @Nonnull
    T apply(@Nonnull T oldComponent);
}
