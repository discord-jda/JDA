package net.dv8tion.jda.api.events.automod;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.AutoModerationRule;
import net.dv8tion.jda.api.entities.automod.TriggerType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that an {@link AutoModerationRule AutoModerationRule} was executed.
 */
public class AutoModerationActionExecutionEvent extends GenericAutoModerationEvent
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

    public AutoModerationActionExecutionEvent(@Nonnull JDA api, long responseNumber, @Nonnull AutoModerationRule rule, @Nonnull AutoModerationAction executedAction, @Nonnull User triggerer, @Nonnull TriggerType ruleTriggerer, @Nullable GuildChannel channel, long messageId, long alertSystemMessageId, @Nullable String content, @Nullable String matchedKeyword, @Nullable String matchedContent)
    {
        super(api, responseNumber, rule);
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
     * Returns the {@link AutoModerationRule} that the action belongs to.
     *
     * @return The rule.
     */
    @Nonnull
    public AutoModerationRule getRule()
    {
        return rule;
    }

    /**
     * Returns the {@link AutoModerationAction} that was executed when the rule was triggered.
     *
     * @return the executed action.
     */
    @Nonnull
    public AutoModerationAction getExecutedAction()
    {
        return executedAction;
    }

    /**
     * Returns the {@link User} that caused the rule to be triggered.
     *
     * @return The user.
     */
    @Nonnull
    public User getTriggerer()
    {
        return triggerer;
    }

    /**
     * Returns the {@link TriggerType} that caused the rule to be triggered.
     *
     * @return The trigger type.
     */
    @Nonnull
    public TriggerType getTriggerType()
    {
        return ruleTriggerer;
    }

    /**
     * Returns the {@link GuildChannel} where the rule was triggered.
     *
     * @return The channel.
     */
    @Nullable
    public GuildChannel getChannel()
    {
        return channel;
    }

    /**
     * Returns the id of the message that triggered the rule.
     *
     * @return The message Id.
     */
    public long getMessageId()
    {
        return messageId;
    }

    /**
     * Returns the id of the system message that was sent to the designated alert channel.
     *
     * @return The alert system message Id.
     */
    @Nullable
    public Long getAlertSystemMessageId()
    {
        return alertSystemMessageId;
    }

    /**
     * Returns the content of the message that triggered the rule.
     * <p>
     * Will return null if the gateway intent {@link net.dv8tion.jda.api.requests.GatewayIntent#MESSAGE_CONTENT} is not enabled.
     * </p>
     * @return The content.
     */
    @Nonnull
    public String getContent()
    {
        return content;
    }

    /**
     * Returns the word or phrase that triggered the rule.
     *
     * @return The matched keyword.
     */
    @Nullable
    public String getMatchedKeyword()
    {
        return matchedKeyword;
    }

    /**
     * Returns the substring in the content that triggered the rule.
     * <p>
     * Will return null if the gateway intent {@link net.dv8tion.jda.api.requests.GatewayIntent#MESSAGE_CONTENT} is not enabled.
     * </p>
     * @return the matched content.
     */
    @Nullable
    public String getMatchedContent()
    {
        return matchedContent;
    }
}
