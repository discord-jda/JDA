package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface ComponentTree
{
    @Nonnull
    @CheckReturnValue
    <T extends Component> ComponentTree replace(ComponentReplacer<T> replacer);
}
