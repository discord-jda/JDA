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
package net.dv8tion.jda.internal.entities.automod

import gnu.trove.list.TLongList
import gnu.trove.list.array.TLongArrayList
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.automod.AutoModEventType
import net.dv8tion.jda.api.entities.automod.AutoModResponse
import net.dv8tion.jda.api.entities.automod.AutoModRule
import net.dv8tion.jda.api.entities.automod.AutoModRule.KeywordPreset
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.internal.utils.Helpers
import java.util.*
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.annotation.Nonnull

class AutoModRuleImpl(private override var guild: Guild, override val idLong: Long) : AutoModRule {
    override var creatorIdLong: Long = 0
        private set

    @get:Nonnull
    override var name = ""
        private set

    @get:Nonnull
    override var eventType = AutoModEventType.UNKNOWN
        private set

    @get:Nonnull
    override var triggerType = AutoModTriggerType.UNKNOWN
        private set
    override var isEnabled = false
        private set
    private override var exemptRoles: TLongList = TLongArrayList()
    private override var exemptChannels: TLongList = TLongArrayList()

    @get:Nonnull
    override var actions = emptyList<AutoModResponse>()
        private set

    @get:Nonnull
    override var filteredKeywords = emptyList<String>()
        private set

    @get:Nonnull
    override var filteredRegex = emptyList<String>()
        private set
    private override var filteredPresets = EnumSet.noneOf(KeywordPreset::class.java)

    @get:Nonnull
    override var allowlist = emptyList<String>()
        private set
    override var mentionLimit = -1
        private set
    override var isMentionRaidProtectionEnabled = false
        private set

    @Nonnull
    override fun getGuild(): Guild {
        val realGuild: Guild = guild.getJDA().getGuildById(guild.idLong)
        if (realGuild != null) guild = realGuild
        return guild
    }

    @Nonnull
    override fun getExemptRoles(): List<Role> {
        val roles: MutableList<Role> = ArrayList(exemptRoles.size())
        for (i in 0 until exemptRoles.size()) {
            val roleId = exemptRoles[i]
            val role: Role = guild.getRoleById(roleId)
            if (role != null) roles.add(role)
        }
        return Collections.unmodifiableList(roles)
    }

    @Nonnull
    override fun getExemptChannels(): List<GuildChannel> {
        val channels: MutableList<GuildChannel> = ArrayList(exemptChannels.size())
        for (i in 0 until exemptChannels.size()) {
            val channelId = exemptChannels[i]
            val channel = guild.getGuildChannelById(channelId)
            if (channel != null) channels.add(channel)
        }
        return Collections.unmodifiableList(channels)
    }

    @Nonnull
    override fun getFilteredPresets(): EnumSet<KeywordPreset> {
        return Helpers.copyEnumSet(KeywordPreset::class.java, filteredPresets)
    }

    fun setName(name: String): AutoModRuleImpl {
        this.name = name
        return this
    }

    fun setEnabled(enabled: Boolean): AutoModRuleImpl {
        isEnabled = enabled
        return this
    }

    fun setOwnerId(ownerId: Long): AutoModRuleImpl {
        creatorIdLong = ownerId
        return this
    }

    fun setEventType(eventType: AutoModEventType): AutoModRuleImpl {
        this.eventType = eventType
        return this
    }

    fun setTriggerType(triggerType: AutoModTriggerType): AutoModRuleImpl {
        this.triggerType = triggerType
        return this
    }

    fun setExemptRoles(exemptRoles: TLongList): AutoModRuleImpl {
        this.exemptRoles = exemptRoles
        return this
    }

    fun setExemptChannels(exemptChannels: TLongList): AutoModRuleImpl {
        this.exemptChannels = exemptChannels
        return this
    }

    fun setActions(actions: List<AutoModResponse>): AutoModRuleImpl {
        this.actions = actions
        return this
    }

    fun setFilteredKeywords(filteredKeywords: List<String>): AutoModRuleImpl {
        this.filteredKeywords = filteredKeywords
        return this
    }

    fun setFilteredRegex(filteredRegex: List<String>): AutoModRuleImpl {
        this.filteredRegex = filteredRegex
        return this
    }

    fun setFilteredPresets(filteredPresets: EnumSet<KeywordPreset>): AutoModRuleImpl {
        this.filteredPresets = filteredPresets
        return this
    }

    fun setAllowlist(allowlist: List<String>): AutoModRuleImpl {
        this.allowlist = allowlist
        return this
    }

    fun setMentionLimit(mentionLimit: Int): AutoModRuleImpl {
        this.mentionLimit = mentionLimit
        return this
    }

    fun setMentionRaidProtectionEnabled(mentionRaidProtectionEnabled: Boolean): AutoModRuleImpl {
        isMentionRaidProtectionEnabled = mentionRaidProtectionEnabled
        return this
    }

    override fun hashCode(): Int {
        return java.lang.Long.hashCode(idLong)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is AutoModRuleImpl) return false
        return idLong == obj.idLong
    }

    override fun toString(): String {
        return EntityString(this)
            .setType(triggerType)
            .setName(name)
            .addMetadata("id", idLong)
            .toString()
    }

    companion object {
        @JvmStatic
        fun fromData(guild: Guild, data: DataObject): AutoModRuleImpl {
            val id = data.getUnsignedLong("id")
            val rule = AutoModRuleImpl(guild, id)
            rule.setName(data.getString("name"))
                .setEnabled(data.getBoolean("enabled", true))
                .setOwnerId(data.getUnsignedLong("creator_id", 0L))
                .setEventType(AutoModEventType.fromKey(data.getInt("event_type", -1)))
                .setTriggerType(AutoModTriggerType.fromKey(data.getInt("trigger_type", -1)))
            data.optArray("exempt_roles").ifPresent { array: DataArray -> rule.setExemptRoles(parseList(array)) }
            data.optArray("exempt_channels").ifPresent { array: DataArray -> rule.setExemptChannels(parseList(array)) }
            data.optArray("actions").ifPresent { array: DataArray ->
                rule.setActions(array.stream { obj: DataArray?, index: Int? ->
                    obj!!.getObject(
                        index!!
                    )
                }
                    .map { obj: DataObject -> AutoModResponseImpl(guild, obj) }
                    .collect(Helpers.toUnmodifiableList()))
            }
            data.optObject("trigger_metadata").ifPresent { metadata: DataObject ->
                // Only for KEYWORD type
                metadata.optArray("keyword_filter").ifPresent { array: DataArray ->
                    rule.setFilteredKeywords(array.stream { obj: DataArray?, index: Int? ->
                        obj!!.getString(
                            index!!
                        )
                    }
                        .collect(Helpers.toUnmodifiableList()))
                }
                metadata.optArray("regex_patterns").ifPresent { array: DataArray ->
                    rule.setFilteredRegex(array.stream { obj: DataArray?, index: Int? ->
                        obj!!.getString(
                            index!!
                        )
                    }
                        .collect(Helpers.toUnmodifiableList()))
                }
                // Both KEYWORD and KEYWORD_PRESET
                metadata.optArray("allow_list").ifPresent { array: DataArray ->
                    rule.setAllowlist(array.stream { obj: DataArray?, index: Int? ->
                        obj!!.getString(
                            index!!
                        )
                    }
                        .collect(Helpers.toUnmodifiableList()))
                }
                // Only KEYWORD_PRESET
                metadata.optArray("presets").ifPresent { array: DataArray ->
                    rule.setFilteredPresets(
                        array.stream { obj: DataArray?, index: Int? ->
                            obj!!.getInt(
                                index!!
                            )
                        }
                            .map { obj: Int? -> KeywordPreset.fromKey() }
                            .collect(
                                Collectors.toCollection(
                                    Supplier { EnumSet.noneOf(KeywordPreset::class.java) })
                            )
                    )
                }
                // Only for MENTION type
                rule.setMentionLimit(metadata.getInt("mention_total_limit", 0))
                rule.setMentionRaidProtectionEnabled(metadata.getBoolean("mention_raid_protection_enabled"))
            }
            return rule
        }

        private fun parseList(array: DataArray): TLongList {
            val list: TLongList = TLongArrayList(array.length())
            for (i in 0 until array.length()) list.add(array.getUnsignedLong(i))
            return list
        }
    }
}
