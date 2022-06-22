package net.dv8tion.jda.api.events.automod;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.TriggerType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that an {@link net.dv8tion.jda.api.entities.AutoModerationRule AutoModerationRule} was excuted..
 */
public class AutoModerationActionExecutionEvent extends GenericAutoModerationEvent {
    private final AutoModerationAction executedAction;
    private final User triggerer;
    private final TriggerType ruleTriggerer;
    private final GuildChannel channel;
    private final long messageId;
    private final long alertSystemMessageId;
    private final String content;
    private final String matchedKeyword;
    private final String matchedContent;


    public AutoModerationActionExecutionEvent(@Nonnull JDA api, long responseNumber, @Nonnull AutoModerationRule rule, @Nonnull AutoModerationAction executedAction, @Nonnull User triggerer, @Nonnull TriggerType ruleTriggerer, @Nullable GuildChannel channel, @Nullable long messageId, @Nullable long alertSystemMessageId, @Nonnull String content, @Nullable String matchedKeyword, @Nullable String matchedContent)
    {
        super(api, responseNumber, rule);
        this.executedAction = executedAction;
        this.triggerer = triggerer;
        this.ruleTriggerer = ruleTriggerer;
        this.channel = channel;
        this.messageId = messageId;
        this.alertSystemMessageId = alertSystemMessageId;
        this.content = content;
        this.matchedKeyword = matchedKeyword;
        this.matchedContent = matchedContent;
    }

    public AutoModerationAction getExecutedAction()
    {
        return executedAction;
    }

    public User getTriggerer()
    {
        return triggerer;
    }

    public TriggerType getRuleTriggerer()
    {
        return ruleTriggerer;
    }

    public GuildChannel getChannel()
    {
        return channel;
    }

    public long getMessageId()
    {
        return messageId;
    }

    public long getAlertSystemMessageId()
    {
        return alertSystemMessageId;
    }

    public String getContent()
    {
        return content;
    }

    public String getMatchedKeyword()
    {
        return matchedKeyword;
    }

    public String getMatchedContent()
    {
        return matchedContent;
    }
}
