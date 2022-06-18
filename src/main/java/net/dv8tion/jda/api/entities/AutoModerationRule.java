package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;
import java.util.List;

public interface AutoModerationRule extends ISnowflake {
    /**
     * The Guild in which the role belongs to.
     *
     * @return {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * The name of the rule.
     *
     * @return {@link java.lang.String String}
     */
    @Nonnull
    String getName();

    /**
     * The user who created the rule.
     *
     * @return {@link net.dv8tion.jda.api.entities.User User}
     */
    @Nonnull
    User getUser();

    /**
     * The event type which triggers the rule.
     *
     * @return {@link net.dv8tion.jda.api.entities.EventType EventType}
     */
    @Nonnull
    EventType getEventType();

    /**
     * The trigger of the rule.
     *
     * @return {@link net.dv8tion.jda.api.entities.TriggerType TriggerTypes}
     */
    @Nonnull
    TriggerType getTriggerType();

    /**
     * The metadata of the trigger.
     *
     * @return {@link net.dv8tion.jda.api.entities.TriggerMetadata TriggerMetadata}
     */
    @Nonnull
    TriggerMetadata getTriggerMetadata();

    /**
     * The actions which will be performed when the rule is triggered.
     *
     * @return A {@link java.util.List List} of {@link net.dv8tion.jda.api.entities.AutoModerationAction ActionTypes}
     */
    @Nonnull
    List<AutoModerationAction> getActions();

    /**
     * Weather the rule is enabled or not.
     *
     * @return {@link java.lang.Boolean Boolean}
     */
    boolean isEnabled();

    /**
     * The roles which are exempt from the rules.
     *
     * @return A {@link java.util.List List} of {@link net.dv8tion.jda.api.entities.Role Roles} which are exempt from the rules.
     */
    List<Role> getExemptRoles();

    /**
     * The channels which are exempt from the rules.
     *
     * @return A {@link java.util.List List} of {@link net.dv8tion.jda.api.entities.Channel Channels} which are exempt from the rules.
     */
    List<Channel> getExemptChannels();
}
