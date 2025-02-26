package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.ComponentTreeImpl;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface ComponentTree
{
    @Nonnull
    @Unmodifiable
    List<MessageTopLevelComponentUnion> getComponents();

    @Nonnull
    @CheckReturnValue
    <T extends Component> ComponentTree replace(ComponentReplacer<T> replacer);

    static ComponentTree of(Collection<? extends MessageTopLevelComponent> components)
    {
        return ComponentTreeImpl.of(components);
    }

    static ComponentTree of(MessageTopLevelComponent... components)
    {
        return of(Arrays.asList(components));
    }
}
