/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.entities.automod;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.automod.build.AutoModerationMessageSend;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.entities.automod.build.AutoModerationMessageSendImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a Discord auto moderation rule.
 * <br>
 * Auto Moderation is a feature which allows each guild to set up rules that trigger based on some criteria.
 * For example, a rule can trigger whenever a message contains a specific keyword.
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * public class AutoModBot extends ListenerAdapter
 * {
 *     public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 *         if (event.getName().equals("create_keyword_rule")) {
 *             String name = event.getOption("name", OptionMapping::getAsString);
 *             String keyword1 = event.getOption("keyword1", OptionMapping::getAsString);
 *             String keyword2 = event.getOption("keyword2", OptionMapping::getAsString);
 *
 *             AutoModerationRule rule = AutoModerationRule.MessageSend.keyword(name).setKeyword(keyword1, keyword2).build();
 *             event.getGuild().createAutoModerationRule(rule);
 *         } else if (event.getName().equals("create_keyword_preset_rule")) {
 *             String name = event.getOption("name", OptionMapping::getAsString);
 *             int preset1 = event.getOption("preset1", OptionMapping::getAsInt);
 *             int preset2 = event.getOption("preset2", OptionMapping::getAsInt);
 *
 *             AutoModerationRule rule = AutoModerationRule.MessageSend.preset(name)
 *                     .setKeywordPresets(KeywordPresetType.fromValue(preset1), KeywordPresetType.fromValue(preset2)).build();
 *             event.getGuild().createAutoModerationRule(rule);
 *         }
 *     }
 * }
 * }</pre>
 */
public interface AutoModerationRule extends ISnowflake, Comparable<AutoModerationRule>, SerializableData
{
    AutoModerationMessageSend MessageSend = new AutoModerationMessageSendImpl();

    /**
     * Returns the Guild that the rule belongs to.
     *
     * @return {@link Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * Returns the name of the rule.
     *
     * @return {@link String}
     */
    @Nonnull
    String getName();

    /**
     * Used to set the name of the new rule.
     *
     * @param  name
     *         The name of the rule. Must be between 1 and 100 characters.
     *
     * @return The {@link AutoModerationRule}.
     */
    @Nonnull
    AutoModerationRule setName(@Nonnull String name);

    /**
     * Returns the user who created the rule.
     *
     * @return {@link User}
     */
    @Nonnull
    User getUser();

    /**
     * Returns the type of event that can potentially trigger this rule.
     *
     * @return {@link EventType}
     */
    @Nonnull
    EventType getEventType();

    /**
     * Used to set the event that will cause the auto moderation system to check for the specified trigger.
     *
     * @param  eventType
     *         The event type.
     *
     * @return The {@link AutoModerationRule}.
     */
    @Nonnull
    AutoModerationRule setEventType(@Nonnull EventType eventType);

    /**
     * Returns the type of trigger that can cause this rule to be executed.
     *
     * @return {@link TriggerType}
     */
    @Nonnull
    TriggerType getTriggerType();

    /**
     * Used to set the trigger that will cause the auto moderation system to be executed.
     *
     * @param  triggerType
     *         The trigger type.
     *
     * @return The {@link AutoModerationRule}.
     */
    @Nonnull
    AutoModerationRule setTriggerType(@Nonnull TriggerType triggerType);

    /**
     * Returns additional metadata for the trigger of this rule, used whenever this rule is executed.
     *
     * @return {@link TriggerMetadata}
     */
    @Nullable
    TriggerMetadata getTriggerMetadata();

    /**
     * Used to set additional data that can he used to determine whether a rule should be executed or not.
     *
     * @param  triggerMetaData
     *         Additional data.
     *
     * @return The {@link AutoModerationRule}.
     */
    @Nullable
    AutoModerationRule setTriggerMetadata(@Nonnull TriggerMetadata triggerMetaData);

    /**
     * Returns the actions that will be performed when this rule is executed.
     *
     * @return A {@link List} of {@link AutoModerationAction actions}
     */
    @Nonnull
    List<AutoModerationAction> getActions();

    /**
     * Used to set the actions that will be executed when the trigger is met.
     *
     * @param  actions
     *         The actions that will be carried out.
     *
     * @return The {@link AutoModerationRule}.
     */
    @Nonnull
    AutoModerationRule setActions(@Nonnull List<AutoModerationAction> actions);

    /**
     * Returns whether this rule is enabled or not.
     *
     * @return True, if this is enabled
     */
    boolean isEnabled();

    /**
     * Whether the rule is enabled or not.
     *
     * @param  enabled
     *         Whether the rule is enabled or not.
     *
     * @return The {@link AutoModerationRule}.
     */
    @Nonnull
    AutoModerationRule setEnabled(boolean enabled);

    /**
     * Returns the roles that are exempt from this rule.
     *
     * @return A {@link List} of {@link Role roles} which are exempt from this rule.
     */
    @Nullable
    List<Role> getExemptRoles();

    /**
     * Used to set the roles that will not be affected by the rule.
     *
     * @param  exemptRoles
     *         The roles that will not be affected.
     *
     * @return The {@link AutoModerationRule}.
     */
    @Nullable
    AutoModerationRule setExemptRoles(@Nonnull List<Role> exemptRoles);

    /**
     * Returns the channels that are exempt from this rule.
     *
     * @return A {@link List} of {@link GuildChannel GuildChannels} which are exempt from this rule.
     */
    @Nullable
    List<GuildChannel> getExemptChannels();

    /**
     * Used to set the channel that will not be affected by the rule.
     *
     * @param  exemptChannels
     *         The channels that will not be affected.
     *
     * @return The {@link AutoModerationRule}.
     */
    @Nullable
    AutoModerationRule setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);
}
