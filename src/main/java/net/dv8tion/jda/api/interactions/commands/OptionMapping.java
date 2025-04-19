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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.InteractionMentions;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Name/Value pair for a {@link CommandInteraction} option.
 *
 * <p>Since values for command options are a union-type you can use this class to coerce the values to the desired target type.
 * <br>You can use {@link #getType()} to do dynamic handling as well. Each getter documents the conditions and coercion rules for the specific types.
 *
 * @see CommandInteraction#getOption(String)
 * @see CommandInteraction#getOptions()
 */
public class OptionMapping
{
    private final DataObject data;
    private final OptionType type;
    private final String name;
    private final TLongObjectMap<Object> resolved;
    private final Mentions mentions;

    public OptionMapping(DataObject data, TLongObjectMap<Object> resolved, JDA jda, Guild guild)
    {
        this.data = data;
        this.type = OptionType.fromKey(data.getInt("type", -1));
        this.name = data.getString("name");
        this.resolved = resolved;
        if (type == OptionType.STRING)
            mentions = new InteractionMentions(getAsString(), resolved, (JDAImpl) jda, guild);
        else
            mentions = new InteractionMentions("", new TLongObjectHashMap<>(0), (JDAImpl) jda, guild);
    }

    /**
     * Resolved mentions for a {@link OptionType#STRING STRING} option.
     * <br>If this option is not of type {@link OptionType#STRING STRING}, this always returns empty lists.
     * Mentions are sorted by occurrence.
     *
     * <p>Mentioned {@link Member members} and {@link Role roles} are always of the same guild.
     * If the interaction {@link Interaction#getUser() user}, mentions users from other guilds, they will only be provided by {@link net.dv8tion.jda.api.entities.Mentions#getUsers()}.
     *
     * <p>This is not supported for {@link CommandAutoCompleteInteraction}.
     *
     * @return {@link net.dv8tion.jda.api.entities.Mentions} for this option
     */
    @Nonnull
    public Mentions getMentions()
    {
        return mentions;
    }

    /**
     * The {@link OptionType OptionType} of this option.
     *
     * @return The {@link OptionType OptionType}
     */
    @Nonnull
    public OptionType getType()
    {
        return type;
    }

    /**
     * The name of this option.
     *
     * @return The option name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The file uploaded for this option.
     * <br>This is represented as an {@link Message.Attachment#isEphemeral() ephemeral} attachment which will only be hosted for up to 2 weeks.
     * If you want a permanent reference, you must download it.
     *
     * @throws IllegalStateException
     *         If this option {@link #getType() type} is not {@link OptionType#ATTACHMENT}
     *
     * @return {@link net.dv8tion.jda.api.entities.Message.Attachment Attachment}
     */
    @Nonnull
    public Message.Attachment getAsAttachment()
    {
        Object obj = resolved.get(getAsLong());
        if (obj instanceof Message.Attachment)
            return (Message.Attachment) obj;
        throw new IllegalStateException("Cannot resolve option of type " + type + " to Attachment!");
    }

    /**
     * The String representation of this option value.
     * <br>This will automatically convert the value to a string if the type is not {@link OptionType#STRING OptionType.STRING}.
     * <br>This will be the ID of any resolved entity such as {@link Role} or {@link Member}.
     *
     * @return The String representation of this option value
     */
    @Nonnull
    public String getAsString()
    {
        return data.getString("value");
    }

    /**
     * The boolean value.
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#BOOLEAN BOOLEAN}
     *
     * @return The boolean value
     */
    public boolean getAsBoolean()
    {
        if (type != OptionType.BOOLEAN)
            throw new IllegalStateException("Cannot convert option of type " + type + " to boolean");
        return data.getBoolean("value");
    }

    /**
     * The long value for this option.
     * <br>This will be the ID of any resolved entity such as {@link Role} or {@link Member}.
     *
     * @throws IllegalStateException
     *         If this option {@link #getType() type} cannot be converted to a long
     * @throws NumberFormatException
     *         If this option is of type {@link OptionType#STRING STRING} and could not be parsed to a valid long value
     *
     * @return The long value
     */
    public long getAsLong()
    {
        switch (type)
        {
            default:
                throw new IllegalStateException("Cannot convert option of type " + type + " to long");
            case STRING:
            case MENTIONABLE:
            case CHANNEL:
            case ROLE:
            case USER:
            case INTEGER:
            case ATTACHMENT:
                return data.getLong("value");
        }
    }

    /**
     * The int value for this option.
     * <br>This will be the ID of any resolved entity such as {@link Role} or {@link Member}.
     *
     * <p><b>It is highly recommended to assert int values by using {@link OptionData#setRequiredRange(long, long)}</b>
     *
     * @throws IllegalStateException
     *         If this option {@link #getType() type} cannot be converted to a long
     * @throws NumberFormatException
     *         If this option is of type {@link OptionType#STRING STRING} and could not be parsed to a valid long value
     * @throws ArithmeticException
     *         If the provided integer value cannot fit into a 32bit signed int
     *
     * @return The int value
     */
    public int getAsInt()
    {
        return Math.toIntExact(getAsLong());
    }

    /**
     * The double value for this option.
     * 
     * @throws IllegalStateException
     *         If this option {@link #getType() type} cannot be converted to a double
     * @throws NumberFormatException
     *         If this option is of type {@link OptionType#STRING STRING} and could not be parsed to a valid double value
     * 
     * @return The double value
     */
    public double getAsDouble()
    {
        switch (type)
        {
            default:
                throw new IllegalStateException("Cannot convert option of type " + type + " to double");
            case STRING:
            case INTEGER:
            case NUMBER:
                return data.getDouble("value");
        }
    }

    /**
     * The resolved {@link IMentionable} instance for this option value.
     *
     * @throws IllegalStateException
     *         If the mentioned entity is not resolvable
     *
     * @return The resolved {@link IMentionable}
     */
    @Nonnull
    public IMentionable getAsMentionable()
    {
        Object entity = resolved.get(getAsLong());
        if (entity instanceof IMentionable)
            return (IMentionable) entity;
        throw new IllegalStateException("Cannot resolve option of type " + type + " to IMentionable");
    }

    /**
     * The resolved {@link Member} for this option value.
     * <br>Note that {@link OptionType#USER OptionType.USER} can also accept users that are not members of a guild, in which case this will be null!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#USER USER} or {@link OptionType#MENTIONABLE MENTIONABLE}
     *
     * @return The resolved {@link Member}, or null
     */
    @Nullable
    public Member getAsMember()
    {
        if (type != OptionType.USER && type != OptionType.MENTIONABLE)
            throw new IllegalStateException("Cannot resolve Member for option " + getName() + " of type " + type);
        Object object = resolved.get(getAsLong());
        if (object instanceof Member)
            return (Member) object;
        return null; // Unresolved
    }

    /**
     * The resolved {@link User} for this option value.
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#USER USER} or
     *         {@link OptionType#MENTIONABLE MENTIONABLE} without a resolved user
     *
     * @return The resolved {@link User}
     */
    @Nonnull
    public User getAsUser()
    {
        if (type != OptionType.USER && type != OptionType.MENTIONABLE)
            throw new IllegalStateException("Cannot resolve User for option " + getName() + " of type " + type);
        Object object = resolved.get(getAsLong());
        if (object instanceof Member)
            return ((Member) object).getUser();
        if (object instanceof User)
            return (User) object;
        throw new IllegalStateException("Could not resolve User from option type " + type);
    }

    /**
     * The resolved {@link Role} for this option value.
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#ROLE ROLE} or
     *         {@link OptionType#MENTIONABLE MENTIONABLE} without a resolved role
     *
     * @return The resolved {@link Role}
     */
    @Nonnull
    public Role getAsRole()
    {
        if (type != OptionType.ROLE && type != OptionType.MENTIONABLE)
            throw new IllegalStateException("Cannot resolve Role for option " + getName() + " of type " + type);
        Object role = resolved.get(getAsLong());
        if (role instanceof Role)
            return (Role) role;
        throw new IllegalStateException("Could not resolve Role from option type " + type);
    }

    /**
     * The {@link ChannelType} for the resolved channel.
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *
     * @return The {@link ChannelType}
     */
    @Nonnull
    public ChannelType getChannelType()
    {
        return getAsChannel().getType();
    }

    /**
     * The resolved {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *         or could not be resolved for unexpected reasons
     *
     * @return The resolved {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel}
     */
    @Nonnull
    public GuildChannelUnion getAsChannel()
    {
        if (type != OptionType.CHANNEL)
            throw new IllegalStateException("Cannot resolve Channel for option " + getName() + " of type " + type);

        Object entity = resolved.get(getAsLong());
        if (entity instanceof GuildChannel)
            return (GuildChannelUnion) entity;

        throw new IllegalStateException("Could not resolve GuildChannel!");
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(getType())
                .addMetadata("name", name)
                .addMetadata("value", getAsString())
                .toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getType(), getName());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof OptionMapping))
            return false;
        OptionMapping data = (OptionMapping) obj;
        return getType() == data.getType() && getName().equals(data.getName());
    }
}
