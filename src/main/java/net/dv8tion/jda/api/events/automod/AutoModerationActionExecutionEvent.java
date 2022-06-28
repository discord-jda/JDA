package net.dv8tion.jda.api.events.automod;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AutoModerationRule;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that an {@link net.dv8tion.jda.api.entities.AutoModerationRule AutoModerationRule} was excuted.
 */
public class AutoModerationActionExecutionEvent extends Event
{
    protected final AutoModerationRule rule;

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
        super(api, responseNumber);
        this.rule = rule;
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

    /**
     * The {@link AutoModerationRule} that the action belongs to.
     *
     * @return The rule.
     */
    @Nonnull
    public AutoModerationRule getRule()
    {
        return rule;
    }

    /**
     * The {@link AutoModerationAction} that was executed when the rule was triggered.
     *
     * @return the executed action.
     */
    @Nonnull
    public AutoModerationAction getExecutedAction()
    {
        return executedAction;
    }

    /**
     * The {@link User} that caused the rule to be triggered.
     *
     * @return The user.
     */
    @Nonnull
    public User getTriggerer()
    {
        return triggerer;
    }

    /**
     * The {@link TriggerType} that caused the rule to be triggered.
     *
     * @return The trigger type.
     */
    @Nonnull
    public TriggerType getTriggerType()
    {
        return ruleTriggerer;
    }

    /**
     * The {@link GuildChannel} where the rule was triggered.
     *
     * @return The channel.
     */
    @Nullable
    public GuildChannel getChannel()
    {
        return channel;
    }

    /**
     * The message Id of the message that triggered the rule.
     *
     * @return The message Id.
     */
    public long getMessageId()
    {
        return messageId;
    }

    /**
     * The message Id of the auto moderation system message that was sent to the allocated channel.
     *
     * @return The alert system message Id.
     */
    public long getAlertSystemMessageId()
    {
        return alertSystemMessageId;
    }

    /**
     * The content of the message that triggered the rule.
     *
     * @return The content.
     */
    @Nonnull
    public String getContent()
    {
        return content;
    }

    /**
     * The word or phrase configured in the rule that triggered the rule.
     *
     * @return The matched keyword.
     */
    @Nullable
    public String getMatchedKeyword()
    {
        return matchedKeyword;
    }

    /**
     * the substring in content that triggered the rule
     *
     * @return the matched content.
     */
    @Nullable
    public String getMatchedContent()
    {
        return matchedContent;
    }
}
