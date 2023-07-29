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

package net.dv8tion.jda.internal.entities.automod;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModEventType;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class AutoModRuleImpl implements AutoModRule
{
    private final long id;
    private Guild guild;
    private long ownerId;
    private String name = "";
    private AutoModEventType eventType = AutoModEventType.UNKNOWN;
    private AutoModTriggerType triggerType = AutoModTriggerType.UNKNOWN;
    private boolean enabled = false;
    private TLongList exemptRoles = new TLongArrayList();
    private TLongList exemptChannels = new TLongArrayList();
    private List<AutoModResponse> actions = Collections.emptyList();
    private List<String> filteredKeywords = Collections.emptyList();
    private List<String> filteredRegex = Collections.emptyList();
    private EnumSet<KeywordPreset> filteredPresets = EnumSet.noneOf(KeywordPreset.class);
    private List<String> allowlist = Collections.emptyList();
    private int mentionLimit = -1;
    private boolean isMentionRaidProtectionEnabled = false;

    public AutoModRuleImpl(Guild guild, long id)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @NotNull
    @Override
    public Guild getGuild()
    {
        Guild realGuild = guild.getJDA().getGuildById(guild.getIdLong());
        if (realGuild != null)
            guild = realGuild;
        return guild;
    }

    @Override
    public long getCreatorIdLong()
    {
        return ownerId;
    }

    @NotNull
    @Override
    public String getName()
    {
        return name;
    }

    @NotNull
    @Override
    public AutoModEventType getEventType()
    {
        return eventType;
    }

    @NotNull
    @Override
    public AutoModTriggerType getTriggerType()
    {
        return triggerType;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @NotNull
    @Override
    public List<Role> getExemptRoles()
    {
        List<Role> roles = new ArrayList<>(exemptRoles.size());
        for (int i = 0; i < exemptRoles.size(); i++)
        {
            long roleId = exemptRoles.get(i);
            Role role = guild.getRoleById(roleId);
            if (role != null)
                roles.add(role);
        }
        return Collections.unmodifiableList(roles);
    }

    @NotNull
    @Override
    public List<GuildChannel> getExemptChannels()
    {
        List<GuildChannel> channels = new ArrayList<>(exemptChannels.size());
        for (int i = 0; i < exemptChannels.size(); i++)
        {
            long channelId = exemptChannels.get(i);
            GuildChannel channel = guild.getGuildChannelById(channelId);
            if (channel != null)
                channels.add(channel);
        }
        return Collections.unmodifiableList(channels);
    }

    @NotNull
    @Override
    public List<AutoModResponse> getActions()
    {
        return actions;
    }

    @NotNull
    @Override
    public List<String> getFilteredKeywords()
    {
        return filteredKeywords;
    }

    @NotNull
    @Override
    public List<String> getFilteredRegex()
    {
        return filteredRegex;
    }

    @NotNull
    @Override
    public EnumSet<KeywordPreset> getFilteredPresets()
    {
        return Helpers.copyEnumSet(KeywordPreset.class, filteredPresets);
    }

    @NotNull
    @Override
    public List<String> getAllowlist()
    {
        return allowlist;
    }

    @Override
    public int getMentionLimit()
    {
        return mentionLimit;
    }

    @Override
    public boolean isMentionRaidProtectionEnabled()
    {
        return isMentionRaidProtectionEnabled;
    }

    public AutoModRuleImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public AutoModRuleImpl setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    public AutoModRuleImpl setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    public AutoModRuleImpl setEventType(AutoModEventType eventType)
    {
        this.eventType = eventType;
        return this;
    }

    public AutoModRuleImpl setTriggerType(AutoModTriggerType triggerType)
    {
        this.triggerType = triggerType;
        return this;
    }

    public AutoModRuleImpl setExemptRoles(TLongList exemptRoles)
    {
        this.exemptRoles = exemptRoles;
        return this;
    }

    public AutoModRuleImpl setExemptChannels(TLongList exemptChannels)
    {
        this.exemptChannels = exemptChannels;
        return this;
    }

    public AutoModRuleImpl setActions(List<AutoModResponse> actions)
    {
        this.actions = actions;
        return this;
    }

    public AutoModRuleImpl setFilteredKeywords(List<String> filteredKeywords)
    {
        this.filteredKeywords = filteredKeywords;
        return this;
    }

    public AutoModRuleImpl setFilteredRegex(List<String> filteredRegex)
    {
        this.filteredRegex = filteredRegex;
        return this;
    }

    public AutoModRuleImpl setFilteredPresets(EnumSet<KeywordPreset> filteredPresets)
    {
        this.filteredPresets = filteredPresets;
        return this;
    }

    public AutoModRuleImpl setAllowlist(List<String> allowlist)
    {
        this.allowlist = allowlist;
        return this;
    }

    public AutoModRuleImpl setMentionLimit(int mentionLimit)
    {
        this.mentionLimit = mentionLimit;
        return this;
    }

    public AutoModRuleImpl setMentionRaidProtectionEnabled(boolean mentionRaidProtectionEnabled)
    {
        isMentionRaidProtectionEnabled = mentionRaidProtectionEnabled;
        return this;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof AutoModRuleImpl))
            return false;
        AutoModRuleImpl oRule = (AutoModRuleImpl) obj;
        return this.id == oRule.id;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(triggerType)
                .setName(name)
                .addMetadata("id", getId())
                .toString();
    }

    public static AutoModRuleImpl fromData(Guild guild, DataObject data)
    {
        long id = data.getUnsignedLong("id");
        AutoModRuleImpl rule = new AutoModRuleImpl(guild, id);

        rule.setName(data.getString("name"))
            .setEnabled(data.getBoolean("enabled", true))
            .setOwnerId(data.getUnsignedLong("creator_id", 0L))
            .setEventType(AutoModEventType.fromKey(data.getInt("event_type", -1)))
            .setTriggerType(AutoModTriggerType.fromKey(data.getInt("trigger_type", -1)));

        data.optArray("exempt_roles").ifPresent(array -> rule.setExemptRoles(parseList(array)));
        data.optArray("exempt_channels").ifPresent(array -> rule.setExemptChannels(parseList(array)));

        data.optArray("actions").ifPresent(array ->
            rule.setActions(array.stream(DataArray::getObject)
                .map(obj -> new AutoModResponseImpl(guild, obj))
                .collect(Helpers.toUnmodifiableList()))
        );

        data.optObject("trigger_metadata").ifPresent(metadata -> {
            // Only for KEYWORD type
            metadata.optArray("keyword_filter").ifPresent(array ->
                rule.setFilteredKeywords(array.stream(DataArray::getString)
                    .collect(Helpers.toUnmodifiableList()))
            );
            metadata.optArray("regex_patterns").ifPresent(array ->
                rule.setFilteredRegex(array.stream(DataArray::getString)
                    .collect(Helpers.toUnmodifiableList()))
            );
            // Both KEYWORD and KEYWORD_PRESET
            metadata.optArray("allow_list").ifPresent(array ->
                    rule.setAllowlist(array.stream(DataArray::getString)
                            .collect(Helpers.toUnmodifiableList()))
            );
            // Only KEYWORD_PRESET
            metadata.optArray("presets").ifPresent(array ->
                rule.setFilteredPresets(array.stream(DataArray::getInt)
                    .map(KeywordPreset::fromKey)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(KeywordPreset.class))))
            );
            // Only for MENTION type
            rule.setMentionLimit(metadata.getInt("mention_total_limit", 0));
            rule.setMentionRaidProtectionEnabled(metadata.getBoolean("mention_raid_protection_enabled"));
        });

        return rule;
    }

    private static TLongList parseList(DataArray array)
    {
        TLongList list = new TLongArrayList(array.length());
        for (int i = 0; i < array.length(); i++)
            list.add(array.getUnsignedLong(i));
        return list;
    }
}
