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
import net.dv8tion.jda.api.entities.automod.AutoModerationRule;
import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.TriggerType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface Keyword
{
    Keyword setKeyword(@Nonnull String keyword);

    /**
     * Used to set the event that will cause the auto moderation system to check for the specified trigger.
     *
     * @param  eventType
     *         The event type.
     *
     * @return The {@link Keyword}.
     */
    @Nonnull
    Keyword setEventType(@Nonnull EventType eventType);

    /**
     * Used to set the trigger that will cause the auto moderation system to be executed.
     *
     * @param  triggerType
     *         The trigger type.
     *
     * @return The {@link Keyword}.
     */
    @Nonnull
    Keyword setTriggerType(@Nonnull TriggerType triggerType);


    /**
     * Whether the rule is enabled or not.
     *
     * @param  enabled
     *         Whether the rule is enabled or not.
     *
     * @return The {@link Keyword}.
     */
    @Nonnull
    Keyword setEnabled(boolean enabled);


    /**
     * Used to set the roles that will not be affected by the rule.
     *
     * @param  exemptRoles
     *         The roles that will not be affected.
     *
     * @return The {@link Keyword}.
     */
    @Nullable
    Keyword setExemptRoles(@Nonnull List<Role> exemptRoles);

    /**
     * Used to set the channel that will not be affected by the rule.
     *
     * @param  exemptChannels
     *         The channels that will not be affected.
     *
     * @return The {@link Keyword}.
     */
    @Nullable
    Keyword setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);
}
