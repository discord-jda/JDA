package net.dv8tion.jda.internal.interactions.components.tree;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.tree.MessageComponentTree;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.ComponentsUtil;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MessageComponentTreeImpl extends AbstractComponentTree<MessageTopLevelComponentUnion> implements MessageComponentTree
{
    private MessageComponentTreeImpl(Collection<MessageTopLevelComponentUnion> components)
    {
        super(components);
    }

    @Nonnull
    public static MessageComponentTree of(@Nonnull Collection<? extends MessageTopLevelComponent> _components)
    {
        // Empty trees are allowed
        Checks.noneNull(_components, "Components");

        final Collection<MessageTopLevelComponentUnion> components = ComponentsUtil.membersToUnion(_components, MessageTopLevelComponentUnion.class);
        return new MessageComponentTreeImpl(components);
    }

    @Nonnull
    @Override
    public MessageComponentTree replace(ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
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
