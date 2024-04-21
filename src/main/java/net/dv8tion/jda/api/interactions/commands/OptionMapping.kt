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
package net.dv8tion.jda.api.interactions.commands

import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.InteractionMentions
import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * Name/Value pair for a [CommandInteraction] option.
 *
 *
 * Since values for command options are a union-type you can use this class to coerce the values to the desired target type.
 * <br></br>You can use [.getType] to do dynamic handling as well. Each getter documents the conditions and coercion rules for the specific types.
 *
 * @see CommandInteraction.getOption
 * @see CommandInteraction.getOptions
 */
class OptionMapping(private val data: DataObject, private val resolved: TLongObjectMap<Any>, jda: JDA?, guild: Guild?) {
    /**
     * The [OptionType] of this option.
     *
     * @return The [OptionType]
     */
    @JvmField
    @get:Nonnull
    val type: OptionType

    /**
     * The name of this option.
     *
     * @return The option name
     */
    @JvmField
    @get:Nonnull
    val name: String

    /**
     * Resolved mentions for a [STRING][OptionType.STRING] option.
     * <br></br>If this option is not of type [STRING][OptionType.STRING], this always returns empty lists.
     * Mentions are sorted by occurrence.
     *
     *
     * Mentioned [members][Member] and [roles][Role] are always of the same guild.
     * If the interaction [user][Interaction.getUser], mentions users from other guilds, they will only be provided by [net.dv8tion.jda.api.entities.Mentions.getUsers].
     *
     *
     * This is not supported for [CommandAutoCompleteInteraction].
     *
     * @return [net.dv8tion.jda.api.entities.Mentions] for this option
     */
    @get:Nonnull
    var mentions: Mentions? = null

    init {
        type = OptionType.Companion.fromKey(data.getInt("type", -1))
        name = data.getString("name")
        mentions = if (type == OptionType.STRING) InteractionMentions(
            asString,
            resolved,
            jda as JDAImpl?,
            guild as GuildImpl?
        ) else InteractionMentions(
            "",
            TLongObjectHashMap(0),
            jda as JDAImpl?,
            guild as GuildImpl?
        )
    }

    @get:Nonnull
    val asAttachment: Message.Attachment
        /**
         * The file uploaded for this option.
         * <br></br>This is represented as an [ephemeral][Message.Attachment.isEphemeral] attachment which will only be hosted for up to 2 weeks.
         * If you want a permanent reference, you must download it.
         *
         * @throws IllegalStateException
         * If this option [type][.getType] is not [OptionType.ATTACHMENT]
         *
         * @return [Attachment][net.dv8tion.jda.api.entities.Message.Attachment]
         */
        get() {
            val obj = resolved[asLong]
            if (obj is Message.Attachment) return obj
            throw IllegalStateException("Cannot resolve option of type $type to Attachment!")
        }

    @get:Nonnull
    val asString: String
        /**
         * The String representation of this option value.
         * <br></br>This will automatically convert the value to a string if the type is not [OptionType.STRING].
         * <br></br>This will be the ID of any resolved entity such as [Role] or [Member].
         *
         * @return The String representation of this option value
         */
        get() = data.getString("value")
    val asBoolean: Boolean
        /**
         * The boolean value.
         *
         * @throws IllegalStateException
         * If this option is not of type [BOOLEAN][OptionType.BOOLEAN]
         *
         * @return The boolean value
         */
        get() {
            check(type == OptionType.BOOLEAN) { "Cannot convert option of type $type to boolean" }
            return data.getBoolean("value")
        }
    val asLong: Long
        /**
         * The long value for this option.
         * <br></br>This will be the ID of any resolved entity such as [Role] or [Member].
         *
         * @throws IllegalStateException
         * If this option [type][.getType] cannot be converted to a long
         * @throws NumberFormatException
         * If this option is of type [STRING][OptionType.STRING] and could not be parsed to a valid long value
         *
         * @return The long value
         */
        get() = when (type) {
            OptionType.STRING, OptionType.MENTIONABLE, OptionType.CHANNEL, OptionType.ROLE, OptionType.USER, OptionType.INTEGER, OptionType.ATTACHMENT -> data.getLong(
                "value"
            )

            else -> throw IllegalStateException("Cannot convert option of type $type to long")
        }
    val asInt: Int
        /**
         * The int value for this option.
         * <br></br>This will be the ID of any resolved entity such as [Role] or [Member].
         *
         *
         * **It is highly recommended to assert int values by using [OptionData.setRequiredRange]**
         *
         * @throws IllegalStateException
         * If this option [type][.getType] cannot be converted to a long
         * @throws NumberFormatException
         * If this option is of type [STRING][OptionType.STRING] and could not be parsed to a valid long value
         * @throws ArithmeticException
         * If the provided integer value cannot fit into a 32bit signed int
         *
         * @return The int value
         */
        get() = Math.toIntExact(asLong)
    val asDouble: Double
        /**
         * The double value for this option.
         *
         * @throws IllegalStateException
         * If this option [type][.getType] cannot be converted to a double
         * @throws NumberFormatException
         * If this option is of type [STRING][OptionType.STRING] and could not be parsed to a valid double value
         *
         * @return The double value
         */
        get() = when (type) {
            OptionType.STRING, OptionType.INTEGER, OptionType.NUMBER -> data.getDouble(
                "value"
            )

            else -> throw IllegalStateException("Cannot convert option of type $type to double")
        }

    @get:Nonnull
    val asMentionable: IMentionable
        /**
         * The resolved [IMentionable] instance for this option value.
         *
         * @throws IllegalStateException
         * If the mentioned entity is not resolvable
         *
         * @return The resolved [IMentionable]
         */
        get() {
            val entity = resolved[asLong]
            if (entity is IMentionable) return entity
            throw IllegalStateException("Cannot resolve option of type $type to IMentionable")
        }
    val asMember: Member?
        /**
         * The resolved [Member] for this option value.
         * <br></br>Note that [OptionType.USER] can also accept users that are not members of a guild, in which case this will be null!
         *
         * @throws IllegalStateException
         * If this option is not of type [USER][OptionType.USER] or [MENTIONABLE][OptionType.MENTIONABLE]
         *
         * @return The resolved [Member], or null
         */
        get() {
            check(!(type != OptionType.USER && type != OptionType.MENTIONABLE)) { "Cannot resolve Member for option " + name + " of type " + type }
            val `object` = resolved[asLong]
            return if (`object` is Member) `object` else null
            // Unresolved
        }

    @get:Nonnull
    val asUser: User
        /**
         * The resolved [User] for this option value.
         *
         * @throws IllegalStateException
         * If this option is not of type [USER][OptionType.USER] or
         * [MENTIONABLE][OptionType.MENTIONABLE] without a resolved user
         *
         * @return The resolved [User]
         */
        get() {
            check(!(type != OptionType.USER && type != OptionType.MENTIONABLE)) { "Cannot resolve User for option " + name + " of type " + type }
            val `object` = resolved[asLong]
            if (`object` is Member) return `object`.user
            if (`object` is User) return `object`
            throw IllegalStateException("Could not resolve User from option type $type")
        }

    @get:Nonnull
    val asRole: Role
        /**
         * The resolved [Role] for this option value.
         *
         * @throws IllegalStateException
         * If this option is not of type [ROLE][OptionType.ROLE] or
         * [MENTIONABLE][OptionType.MENTIONABLE] without a resolved role
         *
         * @return The resolved [Role]
         */
        get() {
            check(!(type != OptionType.ROLE && type != OptionType.MENTIONABLE)) { "Cannot resolve Role for option " + name + " of type " + type }
            val role = resolved[asLong]
            if (role is Role) return role
            throw IllegalStateException("Could not resolve Role from option type $type")
        }

    @get:Nonnull
    val channelType: ChannelType?
        /**
         * The [ChannelType] for the resolved channel.
         *
         * @throws IllegalStateException
         * If this option is not of type [CHANNEL][OptionType.CHANNEL]
         *
         * @return The [ChannelType]
         */
        get() = asChannel.type

    @get:Nonnull
    val asChannel: GuildChannelUnion
        /**
         * The resolved [net.dv8tion.jda.api.entities.channel.middleman.GuildChannel] for this option value.
         * <br></br>Note that [OptionType.CHANNEL] can accept channels of any type!
         *
         * @throws IllegalStateException
         * If this option is not of type [CHANNEL][OptionType.CHANNEL]
         * or could not be resolved for unexpected reasons
         *
         * @return The resolved [net.dv8tion.jda.api.entities.channel.middleman.GuildChannel]
         */
        get() {
            check(type == OptionType.CHANNEL) { "Cannot resolve Channel for option " + name + " of type " + type }
            val entity = resolved[asLong]
            if (entity is GuildChannel) return entity as GuildChannelUnion
            throw IllegalStateException("Could not resolve GuildChannel!")
        }

    override fun toString(): String {
        return EntityString(this)
            .setType(type)
            .addMetadata("name", name)
            .addMetadata("value", asString)
            .toString()
    }

    override fun hashCode(): Int {
        return Objects.hash(type, name)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is OptionMapping) return false
        val data = obj
        return type == data.type && name == data.name
    }
}
