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

import javax.annotation.Nonnull

/**
 * The available types for [Command] options.
 */
enum class OptionType @JvmOverloads constructor(
    /**
     * The raw value for this type or -1 for [.UNKNOWN]
     *
     * @return The raw value
     */
    val key: Int, private val supportsChoices: Boolean = false
) {
    /** Placeholder for future option types  */
    UNKNOWN(-1),

    /**
     * Option which is serialized as subcommand, this is only used for internals and should be ignored by users.
     * @see SlashCommandData.addSubcommands
     */
    SUB_COMMAND(1),

    /**
     * Option which is serialized as subcommand groups, this is only used for internals and should be ignored by users.
     * @see SlashCommandData.addSubcommandGroups
     */
    SUB_COMMAND_GROUP(2),

    /**
     * Options which accept text inputs. This also supports role/channel/user mentions.
     * @see OptionMapping.getAsString
     * @see OptionMapping.getMentions
     */
    STRING(3, true),

    /**
     * Options which accept [Long] integer inputs
     * @see OptionMapping.getAsLong
     */
    INTEGER(4, true),

    /**
     * Options which accept boolean true or false inputs
     * @see OptionMapping.getAsBoolean
     */
    BOOLEAN(5),

    /**
     * Options which accept a single [Member][net.dv8tion.jda.api.entities.Member] or [User][net.dv8tion.jda.api.entities.User]
     * @see OptionMapping.getAsUser
     * @see OptionMapping.getAsMember
     */
    USER(6),

    /**
     * Options which accept a single [GuildChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildChannel]
     * @see OptionMapping.getAsChannel
     */
    CHANNEL(7),

    /**
     * Options which accept a single [Role][net.dv8tion.jda.api.entities.Role]
     * @see OptionMapping.getAsRole
     */
    ROLE(8),

    /**
     * Options which accept a single [Role][net.dv8tion.jda.api.entities.Role], [User][net.dv8tion.jda.api.entities.User], or [Member][net.dv8tion.jda.api.entities.Member].
     * @see OptionMapping.getAsMentionable
     */
    MENTIONABLE(9),

    /**
     * Options which accept a [Double] value (also includes [Long])
     * @see OptionMapping.getAsDouble
     * @see OptionMapping.getAsLong
     */
    NUMBER(10, true),

    /**
     * Options which accept a file attachment
     * @see OptionMapping.getAsAttachment
     */
    ATTACHMENT(11);

    /**
     * Whether options of this type support predefined choices.
     *
     * @return True, if you can use choices for this type.
     */
    fun canSupportChoices(): Boolean {
        return supportsChoices
    }

    companion object {
        /**
         * Converts the provided raw type to the enum constant.
         *
         * @param  key
         * The raw type
         *
         * @return The OptionType constant or [.UNKNOWN]
         */
        @JvmStatic
        @Nonnull
        fun fromKey(key: Int): OptionType {
            for (type in entries) {
                if (type.key == key) return type
            }
            return UNKNOWN
        }
    }
}
