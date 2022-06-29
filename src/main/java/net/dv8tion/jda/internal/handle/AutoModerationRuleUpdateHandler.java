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

package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.automod.*;
import net.dv8tion.jda.api.events.automod.update.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.AutoModerationRuleImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoModerationRuleUpdateHandler extends SocketHandler
{

    public AutoModerationRuleUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        long guildId = content.getLong("guild_id");
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);

        long ruleId = content.getLong("id");
        AutoModerationRuleImpl rule = (AutoModerationRuleImpl) guild.getAutoModerationRuleById(ruleId);

        if (rule == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.AUTO_MODERATION, ruleId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Caching AUTO_MODERATION_RULE_UPDATE event for rule that is not currently cached. RuleID: {}", ruleId);
        }

        String name = content.getString("name");
        final User user = api.getUserById(content.getString("user_id"));
        EventType eventType = EventType.fromValue(content.getInt("event_type"));
        TriggerType triggerType = TriggerType.fromValue(content.getInt("trigger_type"));
        TriggerMetadata triggerMetadata = api.getEntityBuilder().createTriggerMetadata(content.getObject("trigger_metadata"));
        DataArray actionArray = content.getArray("actions");
        boolean isEnabled = content.getBoolean("enabled");
        DataArray exemptRoleArray = content.getArray("exempt_roles");
        DataArray exemptChannelArray = content.getArray("exempt_channels");

        final List<Role> exemptRoles = new ArrayList<>();
        for (int i = 0; i < exemptRoleArray.length(); i++)
        {
            final long roleId = exemptRoleArray.getLong(i);
            final Role role = guild.getRoleById(roleId);
            if (role == null)
                continue;
            exemptRoles.add(role);
        }

        final List<GuildChannel> exemptChannels = new ArrayList<>();
        for (int i = 0; i < exemptChannelArray.length(); i++)
        {
            final long channelId = exemptChannelArray.getLong(i);
            final GuildChannel channel = guild.getGuildChannelById(channelId);
            if (channel == null)
                continue;
            exemptChannels.add(channel);
        }

        final List<AutoModerationAction> actions = new ArrayList<>();
        for (int i = 0; i < actionArray.length(); i++)
            actions.add(api.getEntityBuilder().createAutoModerationAction(guild, actionArray.getObject(i)));

        if (!Objects.equals(rule.getGuild(), guild))
        {
            Guild oldGuild = rule.getGuild();
            rule.setGuild(guild);
            getJDA().handleEvent(new AutoModerationRuleUpdateGuildEvent(getJDA(), responseNumber, rule, AutoModerationField.GUILD, oldGuild, guild));
        }

        if (!Objects.equals(name, rule.getName()))
        {
            String oldName = rule.getName();
            rule.setName(name);
            getJDA().handleEvent(new AutoModerationRuleUpdateNameEvent(getJDA(), responseNumber, rule, AutoModerationField.NAME, oldName, name));
        }

        if (!Objects.equals(user, rule.getUser()))
        {
            User oldUser = rule.getUser();
            rule.setUser(user);
            getJDA().handleEvent(new AutoModerationRuleUpdateUserEvent(getJDA(), responseNumber, rule, AutoModerationField.USER, oldUser, user));
        }

        if (!Objects.equals(eventType, rule.getEventType()))
        {
            EventType oldEventType = rule.getEventType();
            rule.setEventType(eventType);
            getJDA().handleEvent(new AutoModerationRuleUpdateEventTypeEvent(getJDA(), responseNumber, rule, AutoModerationField.EVENT_TYPE, oldEventType, eventType));
        }

        if (!Objects.equals(triggerType, rule.getTriggerType()))
        {
            TriggerType oldTriggerType = rule.getTriggerType();
            rule.setTriggerType(triggerType);
            getJDA().handleEvent(new AutoModerationRuleUpdateTriggerTypeEvent(getJDA(), responseNumber, rule, AutoModerationField.TRIGGER_TYPE, oldTriggerType, triggerType));
        }

        if (!Objects.equals(triggerMetadata, rule.getTriggerMetadata()))
        {
            TriggerMetadata oldTriggerMetadata = rule.getTriggerMetadata();
            rule.setTriggerMetadata(triggerMetadata);
            getJDA().handleEvent(new AutoModerationRuleUpdateTriggerMetadataEvent(getJDA(), responseNumber, rule, AutoModerationField.TRIGGER_METADATA, oldTriggerMetadata, triggerMetadata));
        }

        if (!Objects.equals(actions, rule.getActions()))
        {
            List<AutoModerationAction> oldActions = rule.getActions();
            rule.setActions(actions);
            getJDA().handleEvent(new AutoModerationRuleUpdateActionsEvent(getJDA(), responseNumber, rule, AutoModerationField.ACTIONS, oldActions, actions));
        }

        if (!Objects.equals(isEnabled, rule.isEnabled()))
        {
            boolean oldIsEnabled = rule.isEnabled();
            rule.setEnabled(isEnabled);
            getJDA().handleEvent(new AutoModerationRuleUpdateEnabledEvent(getJDA(), responseNumber, rule, AutoModerationField.ENABLED, oldIsEnabled, isEnabled));
        }

        if (!Objects.equals(exemptRoles, rule.getExemptRoles()))
        {
            List<Role> oldExemptRoles = rule.getExemptRoles();
            rule.setExemptRoles(exemptRoles);
            getJDA().handleEvent(new AutoModerationRuleUpdateExemptRolesEvent(getJDA(), responseNumber, rule, AutoModerationField.EXEMPT_ROLES, oldExemptRoles, exemptRoles));
        }

        if (!Objects.equals(exemptChannels, rule.getExemptChannels()))
        {
            List<GuildChannel> oldExemptChannels = rule.getExemptChannels();
            rule.setExemptChannels(exemptChannels);
            getJDA().handleEvent(new AutoModerationRuleUpdateExemptChannelsEvent(getJDA(), responseNumber, rule, AutoModerationField.EXEMPT_CHANNELS, oldExemptChannels, exemptChannels));
        }

        return null;
    }
}
