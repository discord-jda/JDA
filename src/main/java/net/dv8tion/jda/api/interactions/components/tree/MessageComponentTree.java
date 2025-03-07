package net.dv8tion.jda.api.interactions.components.tree;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.tree.MessageComponentTreeImpl;

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

    @Nonnull
    static MessageComponentTree of(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        return MessageComponentTreeImpl.of(components);
    }

    @Nonnull
    static MessageComponentTree of(@Nonnull MessageTopLevelComponent... components)
    {
        return of(Arrays.asList(components));
    }
}
