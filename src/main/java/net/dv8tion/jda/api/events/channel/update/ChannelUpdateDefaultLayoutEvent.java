package net.dv8tion.jda.api.events.channel.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelField;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;

import javax.annotation.Nonnull;

/**
 * Indicates that the {@link ForumChannel#getDefaultSortOrder() default sort order} of a {@link ForumChannel} changed.
 *
 * <p>Can be used to retrieve the old default sort order and the new one.
 *
 * @see ChannelField#DEFAULT_SORT_ORDER
 */
@SuppressWarnings("ConstantConditions")
public class ChannelUpdateDefaultLayoutEvent extends GenericChannelUpdateEvent<ForumChannel.Layout>
{
    public ChannelUpdateDefaultLayoutEvent(@Nonnull JDA api, long responseNumber, @Nonnull Channel channel, @Nonnull ForumChannel.Layout oldValue, @Nonnull ForumChannel.Layout newValue)
    {
        super(api, responseNumber, channel, ChannelField.DEFAULT_FORUM_LAYOUT, oldValue, newValue);
    }

    @Nonnull
    @Override
    public ForumChannel.Layout getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public ForumChannel.Layout getNewValue()
    {
        return super.getNewValue();
    }
}
