package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.ComponentTreeImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

public interface ComponentTree
{
    @Nonnull
    @CheckReturnValue
    <T extends Component> ComponentTree replace(ComponentReplacer<T> replacer);

    static ComponentTree of(Collection<? extends Component> components)
    {
        return ComponentTreeImpl.of(components);
    }

    static ComponentTree of(Component... components)
    {
        return of(Arrays.asList(components));
    }
}
