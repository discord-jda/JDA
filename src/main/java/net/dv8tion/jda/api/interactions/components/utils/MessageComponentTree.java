package net.dv8tion.jda.api.interactions.components.utils;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.MessageComponentTreeImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

// TODO-components-v2 - docs
public interface MessageComponentTree extends ComponentTree<MessageTopLevelComponentUnion>
{
    @Nonnull
    @Override
    @CheckReturnValue
    MessageComponentTree replace(ComponentReplacer replacer);

    @Nonnull
    @Override
    @CheckReturnValue
    MessageComponentTree disableAll();

    static MessageComponentTree of(Collection<? extends MessageTopLevelComponent> components)
    {
        return MessageComponentTreeImpl.of(components);
    }

    static MessageComponentTree of(MessageTopLevelComponent... components)
    {
        return of(Arrays.asList(components));
    }
}
