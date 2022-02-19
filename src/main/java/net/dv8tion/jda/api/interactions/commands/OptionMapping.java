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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public OptionMapping(DataObject data, TLongObjectMap<Object> resolved)
    {
        this.data = data;
        this.type = OptionType.fromKey(data.getInt("type", -1));
        this.name = data.getString("name");
        this.resolved = resolved;
    }

    private <T, C extends Collection<T>> C parseMentions(C coll, Pattern pattern, boolean duplicates, Function<Matcher, T> resolver)
    {
        Matcher matcher = pattern.matcher(getAsString());
        while (matcher.find())
        {
            try
            {
                T obj = resolver.apply(matcher);
                if (obj != null && (duplicates || !coll.contains(obj)))
                    coll.add(obj);
            }
            catch (NumberFormatException ignored) {}
        }

        return coll;
    }

    /**
     * Resolved {@link Member} mentions for a {@link OptionType#STRING STRING} option.
     * <br>If this option is not of type {@link OptionType#STRING STRING}, this always returns an empty list.
     * Mentions are sorted by occurrence.
     *
     * <p>This only contains members of the guild.
     * If the user mentions users from other guilds, they will only be provided by {@link #getMentionedUsers()}.
     *
     * <p>This is not supported for {@link CommandAutoCompleteInteraction}.
     *
     * @return {@link List} of {@link Member} the resolved guild user mentions in a string option
     */
    @Nonnull
    public List<Member> getMentionedMembers()
    {
        if (type != OptionType.STRING)
            return Collections.emptyList();

        return parseMentions(new ArrayList<>(), Message.MentionType.USER.getPattern(), false, (matcher) -> {
            long id = Long.parseUnsignedLong(matcher.group(1));
            Object obj = resolved.get(id);
            return obj instanceof Member ? (Member) obj : null;
        });
    }

    /**
     * Resolved {@link User} mentions for a {@link OptionType#STRING STRING} option.
     * <br>If this option is not of type {@link OptionType#STRING STRING}, this always returns an empty list.
     * Mentions are sorted by occurrence.
     *
     * <p>This may also contain users which are not members in the guild!
     *
     * <p>This is not supported for {@link CommandAutoCompleteInteraction}.
     *
     * @return {@link List} of {@link User} the resolved guild user mentions in a string option
     */
    @Nonnull
    public List<User> getMentionedUsers()
    {
        if (type != OptionType.STRING)
            return Collections.emptyList();

        return parseMentions(new ArrayList<>(), Message.MentionType.USER.getPattern(), false, (matcher) -> {
            long id = Long.parseUnsignedLong(matcher.group(1));
            Object obj = resolved.get(id);
            if (obj instanceof User)
                return (User) obj;
            if (obj instanceof Member)
                return ((Member) obj).getUser();
            return null;
        });
    }

    /**
     * Resolved {@link Role} mentions for a {@link OptionType#STRING STRING} option.
     * <br>If this option is not of type {@link OptionType#STRING STRING}, this always returns an empty list.
     * Mentions are sorted by occurrence.
     *
     * <p>This is not supported for {@link CommandAutoCompleteInteraction}.
     *
     * @return {@link List} of {@link Role} the resolved guild role mentions in a string option
     */
    @Nonnull
    public List<Role> getMentionedRoles()
    {
        if (type != OptionType.STRING)
            return Collections.emptyList();

        return parseMentions(new ArrayList<>(), Message.MentionType.ROLE.getPattern(), false, (matcher) -> {
            long id = Long.parseUnsignedLong(matcher.group(1));
            Object obj = resolved.get(id);
            return obj instanceof Role ? (Role) obj : null;
        });
    }

    /**
     * Resolved {@link GuildChannel} mentions for a {@link OptionType#STRING STRING} option.
     * <br>If this option is not of type {@link OptionType#STRING STRING}, this always returns an empty list.
     * Mentions are sorted by occurrence.
     *
     * <p>This is not supported for {@link CommandAutoCompleteInteraction}.
     *
     * @return {@link List} of {@link GuildChannel} the resolved guild channel mentions in a string option
     */
    @Nonnull
    public List<GuildChannel> getMentionedChannels()
    {
        if (type != OptionType.STRING)
            return Collections.emptyList();

        return parseMentions(new ArrayList<>(), Message.MentionType.CHANNEL.getPattern(), false, (matcher) -> {
            long id = Long.parseUnsignedLong(matcher.group(1));
            Object obj = resolved.get(id);
            return obj instanceof GuildChannel ? (GuildChannel) obj : null;
        });
    }

    /**
     * All resolved {@link IMentionable mentions} for a {@link OptionType#STRING STRING} option.
     * <br>If this option is not of type {@link OptionType#STRING STRING}, this always returns an empty list.
     * Mentions are sorted by occurrence.
     *
     * <p>This is not supported for {@link CommandAutoCompleteInteraction}.
     *
     * This merges {@link #getMentionedUsers()}, {@link #getMentionedMembers()}, {@link #getMentionedRoles()}, and {@link #getMentionedChannels()}.
     *
     * @return {@link List} of {@link IMentionable} the resolved mentions in a string option
     */
    @Nonnull
    public List<IMentionable> getMentions()
    {
        if (type != OptionType.STRING)
            return Collections.emptyList();

        List<User> users = getMentionedUsers();
        List<Member> members = getMentionedMembers();
        List<Role> roles = getMentionedRoles();
        List<GuildChannel> channels = getMentionedChannels();
        users.removeIf(user -> members.stream().anyMatch(m -> m.getIdLong() == user.getIdLong()));

        List<IMentionable> mentions = new ArrayList<>(users.size() + members.size() + roles.size() + channels.size());
        mentions.addAll(users);
        mentions.addAll(members);
        mentions.addAll(roles);
        mentions.addAll(channels);
        mentions.sort(Comparator.comparingInt(mention -> getAsString().indexOf(mention.getId())));

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
        Channel channel = getAsChannel();
        return channel == null ? ChannelType.UNKNOWN : channel.getType();
    }

    /**
     * The resolved {@link GuildChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *         or could not be resolved for unexpected reasons
     *
     * @return The resolved {@link GuildChannel}
     */
    @Nonnull
    public GuildChannel getAsGuildChannel()
    {
        Channel value = getAsChannel();
        if (value instanceof GuildChannel)
            return (GuildChannel) value;
        throw new IllegalStateException("Could not resolve GuildChannel!");
    }


    /**
     * The resolved {@link GuildMessageChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *
     * @return The resolved {@link GuildMessageChannel}, or null if this was not a message channel
     */
    @Nullable
    public GuildMessageChannel getAsMessageChannel()
    {
        Channel value = getAsChannel();
        return value instanceof GuildMessageChannel ? (GuildMessageChannel) value : null;
    }

    /**
     * The resolved {@link TextChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *
     * @return The resolved {@link TextChannel}, or null if this was not a text channel
     */
    @Nullable
    public TextChannel getAsTextChannel()
    {
        Channel channel = getAsChannel();
        return channel instanceof TextChannel ? (TextChannel) channel : null;
    }

    /**
     * The resolved {@link NewsChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *
     * @return The resolved {@link NewsChannel}, or null if this was not a news channel
     */
    @Nullable
    public NewsChannel getAsNewsChannel()
    {
        Channel channel = getAsChannel();
        return channel instanceof NewsChannel ? (NewsChannel) channel : null;
    }

    /**
     * The resolved {@link ThreadChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *
     * @return The resolved {@link ThreadChannel}, or null if this was not a thread channel
     */
    @Nullable
    public ThreadChannel getAsThreadChannel()
    {
        Channel channel = getAsChannel();
        return channel instanceof ThreadChannel ? (ThreadChannel) channel : null;
    }


    /**
     * The resolved {@link AudioChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *
     * @return The resolved {@link AudioChannel}, or null if this was not an audio channel
     */
    @Nullable
    public AudioChannel getAsAudioChannel()
    {
        Channel channel = getAsChannel();
        return channel instanceof AudioChannel ? (AudioChannel) channel : null;
    }

    /**
     * The resolved {@link VoiceChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *
     * @return The resolved {@link VoiceChannel}, or null if this was not a voice channel
     */
    @Nullable
    public VoiceChannel getAsVoiceChannel()
    {
        Channel channel = getAsChannel();
        return channel instanceof VoiceChannel ? (VoiceChannel) channel : null;
    }

    /**
     * The resolved {@link StageChannel} for this option value.
     * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
     *
     * @throws IllegalStateException
     *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
     *
     * @return The resolved {@link StageChannel}, or null if this was not a stage channel
     */
    @Nullable
    public StageChannel getAsStageChannel()
    {
        Channel channel = getAsChannel();
        return channel instanceof StageChannel ? (StageChannel) channel : null;
    }


    @Override
    public String toString()
    {
        return "Option[" + getType() + "](" + getName() + "=" + getAsString() + ")";
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

    @Nullable
    private Channel getAsChannel()
    {
        if (type != OptionType.CHANNEL)
            throw new IllegalStateException("Cannot resolve Channel for option " + getName() + " of type " + type);
        return (Channel) resolved.get(getAsLong());
    }
}
