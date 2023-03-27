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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.build.TriggerConfig;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

public interface AutoModRuleManager extends Manager<AutoModRuleManager>
{
    long NAME             = 1;
    long ENABLED          = 1 << 1;
    long RESPONSE         = 1 << 2;
    long EXEMPT_ROLES     = 1 << 3;
    long EXEMPT_CHANNELS  = 1 << 4;
    long TRIGGER_METADATA = 1 << 5;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(AutoModRuleManager.NAME | AutoModRuleManager.RESPONSE);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ENABLED}</li>
     *     <li>{@link #RESPONSE}</li>
     *     <li>{@link #EXEMPT_ROLES}</li>
     *     <li>{@link #EXEMPT_CHANNELS}</li>
     *     <li>{@link #TRIGGER_METADATA}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @Override
    AutoModRuleManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(AutoModRuleManager.NAME, AutoModRuleManager.RESPONSE);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ENABLED}</li>
     *     <li>{@link #RESPONSE}</li>
     *     <li>{@link #EXEMPT_ROLES}</li>
     *     <li>{@link #EXEMPT_CHANNELS}</li>
     *     <li>{@link #TRIGGER_METADATA}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @Override
    AutoModRuleManager reset(long... fields);

    @Nonnull
    @CheckReturnValue
    AutoModRuleManager setName(@Nonnull String name);

    @Nonnull
    @CheckReturnValue
    AutoModRuleManager setEnabled(boolean enabled);

//    @Nonnull
//    @CheckReturnValue
//    AutoModRuleManager setEventType(@Nonnull AutoModEventType type);

    @Nonnull
    @CheckReturnValue
    AutoModRuleManager setResponses(@Nonnull Collection<? extends AutoModResponse> responses);

    @Nonnull
    @CheckReturnValue
    default AutoModRuleManager setResponses(@Nonnull AutoModResponse... responses)
    {
        Checks.noneNull(responses, "Responses");
        return setResponses(Arrays.asList(responses));
    }

    @Nonnull
    @CheckReturnValue
    AutoModRuleManager setExemptRoles(@Nonnull Collection<Role> roles);

    @Nonnull
    @CheckReturnValue
    default AutoModRuleManager setExemptRoles(@Nonnull Role... roles)
    {
        Checks.noneNull(roles, "Roles");
        return setExemptRoles(Arrays.asList(roles));
    }

    @Nonnull
    @CheckReturnValue
    AutoModRuleManager setExemptChannels(@Nonnull Collection<? extends GuildChannel> channels);

    @Nonnull
    @CheckReturnValue
    default AutoModRuleManager setExemptChannels(@Nonnull GuildChannel... channels)
    {
        Checks.noneNull(channels, "Channels");
        return setExemptChannels(Arrays.asList(channels));
    }

    @Nonnull
    @CheckReturnValue
    AutoModRuleManager setTriggerConfig(@Nonnull TriggerConfig config);
}
