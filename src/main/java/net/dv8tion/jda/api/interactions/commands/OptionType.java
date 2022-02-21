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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import javax.annotation.Nonnull;

/**
 * The available types for {@link Command} options.
 */
public enum OptionType
{
    /** Placeholder for future option types */
    UNKNOWN(-1),
    /**
     * Option which is serialized as subcommand, this is only used for internals and should be ignored by users.
     * @see SlashCommandData#addSubcommands(SubcommandData...)
     */
    SUB_COMMAND(1),
    /**
     * Option which is serialized as subcommand groups, this is only used for internals and should be ignored by users.
     * @see SlashCommandData#addSubcommandGroups(SubcommandGroupData...)
     */
    SUB_COMMAND_GROUP(2),
    /**
     * Options which accept text inputs. This also supports role/channel/user mentions.
     * @see OptionMapping#getAsString()
     * @see OptionMapping#getMentions()
     */
    STRING(3, true),
    /**
     * Options which accept {@link Long} integer inputs
     * @see OptionMapping#getAsLong()
     */
    INTEGER(4, true),
    /**
     * Options which accept boolean true or false inputs
     * @see OptionMapping#getAsBoolean()
     */
    BOOLEAN(5),
    /**
     * Options which accept a single {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.User User}
     * @see OptionMapping#getAsUser()
     * @see OptionMapping#getAsMember()
     */
    USER(6),
    /**
     * Options which accept a single {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}
     * @see OptionMapping#getAsGuildChannel()
     */
    CHANNEL(7),
    /**
     * Options which accept a single {@link net.dv8tion.jda.api.entities.Role Role}
     * @see OptionMapping#getAsRole()
     */
    ROLE(8),
    /**
     * Options which accept a single {@link net.dv8tion.jda.api.entities.Role Role}, {@link net.dv8tion.jda.api.entities.User User}, or {@link net.dv8tion.jda.api.entities.Member Member}.
     * @see OptionMapping#getAsMentionable()
     */
    MENTIONABLE(9),
    /**
     * Options which accept a {@link Double} value (also includes {@link Long})
     * @see OptionMapping#getAsDouble()
     * @see OptionMapping#getAsLong()
     */
    NUMBER(10, true),
    /**
     * Options which accept a file attachment
     * @see OptionMapping#getAsAttachment()
     */
    ATTACHMENT(11),
    ;

    private final int raw;
    private final boolean supportsChoices;

    OptionType(int raw)
    {
        this(raw, false);
    }

    OptionType(int raw, boolean supportsChoices)
    {
        this.raw = raw;
        this.supportsChoices = supportsChoices;
    }

    /**
     * The raw value for this type or -1 for {@link #UNKNOWN}
     *
     * @return The raw value
     */
    public int getKey()
    {
        return raw;
    }

    /**
     * Whether options of this type support predefined choices.
     *
     * @return True, if you can use choices for this type.
     */
    public boolean canSupportChoices()
    {
        return supportsChoices;
    }

    /**
     * Converts the provided raw type to the enum constant.
     *
     * @param  key
     *         The raw type
     *
     * @return The OptionType constant or {@link #UNKNOWN}
     */
    @Nonnull
    public static OptionType fromKey(int key)
    {
        for (OptionType type : values())
        {
            if (type.raw == key)
                return type;
        }
        return UNKNOWN;
    }
}
