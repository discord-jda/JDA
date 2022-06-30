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

package net.dv8tion.jda.api.events.automod;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.automod.*;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Indicates that a {@link AutoModerationRule} was created/removed/updated.
 *
 * <h2>Requirements</h2>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.requests.GatewayIntent#AUTO_MODERATION_CONFIGURATION AUTO_MODERATION_CONFIGURATION} intent.
 */
public class GenericAutoModerationEvent extends Event
{
    protected final AutoModerationRule rule;

    public GenericAutoModerationEvent(@Nonnull JDA api, long responseNumber, AutoModerationRule rule)
    {
        super(api, responseNumber);
        this.rule = rule;
    }

    /**
     * Returns the {@link AutoModerationRule} that was created/removed/updated.
     *
     * @return The rule.
     */
    @Nonnull
    public AutoModerationRule getRule()
    {
        return rule;
    }

    /**
     * Returns the {@link Guild} that owns the affected auto moderation rule.
     *
     * @return The origin Guild.
     */
    public Guild getGuild()
    {
        return rule.getGuild();
    }

    /**
     * Returns the name of this rule that was created/removed/updated.
     *
     * @return The name of the rule.
     */
    @Nonnull
    public String getName()
    {
        return rule.getName();
    }

    /**
     * Returns the {@link User} that created/removed/updated this rule.
     *
     * @return The creator/updater/remover of the rule.
     */
    @Nonnull
    public User getUser()
    {
        return rule.getUser();
    }

    /**
     * Returns the {@link EventType} for this rule.
     *
     * @return The event type.
     */
    @Nonnull
    public EventType getEventType()
    {
        return rule.getEventType();
    }

    /**
     * Returns the {@link TriggerType} for this rule.
     *
     * @return The trigger type.
     */
    @Nonnull
    public TriggerType getTriggerType()
    {
        return rule.getTriggerType();
    }

    /**
     * Returns the {@link TriggerMetadata} for this rule.
     *
     * @return The trigger metadata.
     */
    @Nonnull
    public TriggerMetadata getTriggerMetadata()
    {
        return rule.getTriggerMetadata();
    }

    /**
     * Returns the {@link AutoModerationAction} that will be executed when this rule is triggered.
     *
     * @return The action.
     */
    @Nonnull
    public List<AutoModerationAction> getActions()
    {
        return rule.getActions();
    }

    /**
     * Returns weather this rule is enabled or not.
     *
     * @return True, if the rule is enabled prior to the update. False, if it is disabled.
     */
    public boolean isEnabled()
    {
        return rule.isEnabled();
    }

    /**
     * Returns the {@link Role roles} that will be exempted from this rule.
     *
     * @return The exempt roles.
     */
    public List<Role> getExemptRoles()
    {
        return rule.getExemptRoles();
    }

    /**
     * Returns the {@link GuildChannel channels} that will be exempted from this rule.
     *
     * @return The exempt channels.
     */
    public List<GuildChannel> getExemptChannels()
    {
        return rule.getExemptChannels();
    }
}
