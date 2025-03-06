package net.dv8tion.jda.internal.interactions.components;

import net.dv8tion.jda.api.interactions.components.MessageComponentTree;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MessageComponentTreeImpl extends AbstractComponentTree<MessageTopLevelComponentUnion> implements MessageComponentTree
{
    private MessageComponentTreeImpl(Collection<MessageTopLevelComponentUnion> components)
    {
        super(components);
    }

    public static MessageComponentTree of(Collection<? extends MessageTopLevelComponent> _components)
    {
        final Collection<MessageTopLevelComponentUnion> components = UnionUtil.membersToUnion(_components);
        // TODO-components-v2 - checks?

        return new MessageComponentTreeImpl(components);
    }

    @Nonnull
    @Override
    public MessageComponentTree replace(ComponentReplacer replacer)
    {
        return IReplacerAware.doReplace(
                MessageTopLevelComponent.class,
                components,
                replacer,
                MessageComponentTreeImpl::new
        );
    }

    @Nonnull
    @Override
    public MessageComponentTree disableAll()
    {
        return (MessageComponentTree) super.disableAll();
    }
}
