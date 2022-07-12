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

package net.dv8tion.jda.api.entities.automod.build.sent;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;

public interface GenericKeyWord
{
    /**
     * Whether the rule is enabled or not.
     *
     * @param  enabled
     *         Whether the rule is enabled or not.
     *
     * @return The {@link GenericKeyWord}.
     */
    @Nonnull
    @CheckReturnValue
    GenericKeyWord setEnabled(boolean enabled);

    /**
     * Used to set the actions that will be executed when the trigger is met.
     * @param  type
     *         The type of action.
     * @param  channel
     *         The channel where an alert message will be sent when the rule is executed.
     * @param  duration
     *         The duration of the timeout.
     *
     * @return The {@link GenericKeyWord}.
     */
    @CheckReturnValue
    GenericKeyWord setAction(@Nonnull AutoModerationActionType type, @Nullable GuildChannel channel, @Nullable Duration duration);

    /**
     * Used to set the actions that will be executed when the trigger is met.
     * @param  type
     *         The type of action.
     * @param  channel
     *         The channel where an alert message will be sent when the rule is executed.
     *
     * @return The {@link GenericKeyWord}.
     */
    @CheckReturnValue
    default GenericKeyWord setAction(@Nonnull AutoModerationActionType type, @Nullable GuildChannel channel)
    {
        return setAction(type, channel, null);
    }

    /**
     * Used to set the actions that will be executed when the trigger is met.
     * @param  type
     *         The type of action.
     * @param  duration
     *         The duration of the timeout.
     *
     * @return The {@link GenericKeyWord}.
     */
    @CheckReturnValue
    default GenericKeyWord setAction(@Nonnull AutoModerationActionType type, @Nullable Duration duration)
    {
        return setAction(type, null, duration);
    }

    /**
     * Used to set the roles that will not be affected by the rule.
     *
     * @param  exemptRoles
     *         The roles that will not be affected.
     *
     * @return The {@link GenericKeyWord}.
     */
    @Nullable
    @CheckReturnValue
    GenericKeyWord setExemptRoles(@Nonnull List<Role> exemptRoles);

    /**
     * Used to set the channel that will not be affected by the rule.
     *
     * @param  exemptChannels
     *         The channels that will not be affected.
     *
     * @return The {@link Keyword}.
     */
    @Nullable
    @CheckReturnValue
    GenericKeyWord setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);

    AutoModerationRule build();

    /**
     * Returns the name of the rule.
     *
     * @return {@link String}
     */
    @Nonnull
    String getName();

    /**
     * Returns the type of event that can potentially trigger this rule.
     *
     * @return {@link EventType}
     */
    @Nonnull
    EventType getEventType();

    /**
     * Returns the type of trigger that can cause this rule to be executed.
     *
     * @return {@link TriggerType}
     */
    @Nonnull
    TriggerType getTriggerType();

    /**
     * Returns whether this rule is enabled or not.
     *
     * @return True, if this is enabled
     */
    boolean isEnabled();

    /**
     * Returns the actions that will be performed when this rule is executed.
     *
     * @return A {@link List} of {@link AutoModerationAction actions}
     */
    @Nonnull
    List<AutoModerationAction> getActions();

    /**
     * Returns additional metadata for the trigger of this rule, used whenever this rule is executed.
     *
     * @return {@link TriggerMetadata}
     */
    @Nullable
    TriggerMetadata getTriggerMetadata();

    /**
     * Returns the roles that are exempt from this rule.
     *
     * @return A {@link List} of {@link Role roles} which are exempt from this rule.
     */
    @Nullable
    List<Role> getExemptRoles();

    /**
     * Returns the channels that are exempt from this rule.
     *
     * @return A {@link List} of {@link GuildChannel GuildChannels} which are exempt from this rule.
     */
    @Nullable
    List<GuildChannel> getExemptChannels();
}
