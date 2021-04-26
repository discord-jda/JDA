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

package net.dv8tion.jda.api.events.interaction;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.CommandHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandReplyAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandHookImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.CommandReplyActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SlashCommandEvent extends GenericChannelInteractionCreateEvent
{
    private final String name, subcommandName, subcommandGroup;
    private final long commandId;
    private final List<OptionData> options;
    private final CommandHookImpl hook;

    public SlashCommandEvent(@Nonnull JDA api, long responseNumber, @Nonnull String token, long interactionId,
                             @Nullable Guild guild, @Nullable Member member, @Nonnull User user, @Nonnull MessageChannel channel,
                             @Nonnull String name, @Nullable String subcommandName, @Nullable String subcommandGroup,
                             long commandId, @Nonnull List<OptionData> options)
    {
        super(api, responseNumber, InteractionType.SLASH_COMMAND.getKey(), token, interactionId, guild, member, user, channel);
        this.name = name;
        this.subcommandGroup = subcommandGroup;
        this.subcommandName = subcommandName;
        this.commandId = commandId;
        this.options = Collections.unmodifiableList(options);
        this.hook = new CommandHookImpl(this);
    }

    /**
     * The command name.
     * <br>This can be useful for abstractions.
     *
     * <p>Note that commands can have these following structures:
     * <ul>
     *     <li>{@code /name subcommandGroup subcommandName}</li>
     *     <li>{@code /name subcommandName}</li>
     *     <li>{@code /name}</li>
     * </ul>
     *
     * You can use {@link #getCommandPath()} to simplify your checks.
     *
     * @return The command name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The subcommand name.
     * <br>This can be useful for abstractions.
     *
     * <p>Note that commands can have these following structures:
     * <ul>
     *     <li>{@code /name subcommandGroup subcommandName}</li>
     *     <li>{@code /name subcommandName}</li>
     *     <li>{@code /name}</li>
     * </ul>
     *
     * You can use {@link #getCommandPath()} to simplify your checks.
     *
     * @return The subcommand name, or null if this is not a subcommand
     */
    @Nullable
    public String getSubcommandName()
    {
        return subcommandName;
    }

    /**
     * The subcommand group name.
     * <br>This can be useful for abstractions.
     *
     * <p>Note that commands can have these following structures:
     * <ul>
     *     <li>{@code /name subcommandGroup subcommandName}</li>
     *     <li>{@code /name subcommandName}</li>
     *     <li>{@code /name}</li>
     * </ul>
     *
     * You can use {@link #getCommandPath()} to simplify your checks.
     *
     * @return The subcommand group name, or null if this is not a subcommand group
     */
    @Nullable
    public String getSubcommandGroup()
    {
        return subcommandGroup;
    }

    /**
     * Combination of {@link #getName()}, {@link #getSubcommandGroup()}, and {@link #getSubcommandName()}.
     * <br>This will format the command into a path such as {@code mod/mute} where {@code mod} would be the {@link #getName()} and {@code mute} the {@link #getSubcommandName()}.
     *
     * <p>Examples:
     * <ul>
     *     <li>{@code /mod ban -> "mod/ban"}</li>
     *     <li>{@code /admin config owner -> "admin/config/owner"}</li>
     *     <li>{@code /ban -> "ban"}</li>
     * </ul>
     *
     * @return The command path
     */
    @Nonnull
    public String getCommandPath()
    {
        StringBuilder builder = new StringBuilder(name);
        if (subcommandGroup != null)
            builder.append('/').append(subcommandGroup);
        if (subcommandName != null)
            builder.append('/').append(subcommandName);
        return builder.toString();
    }

    /**
     * The command id
     *
     * @return The command id
     */
    public long getCommandIdLong()
    {
        return commandId;
    }

    /**
     * The command id
     * <br>This is not the same as {@link #getName()}!
     *
     * @return The command id
     */
    @Nonnull
    public String getCommandId()
    {
        return Long.toUnsignedString(commandId);
    }

    /**
     * The list of {@link OptionData} for this command.
     * <br>Each option has a name and value.
     *
     * @return The options passed for this command
     */
    @Nonnull
    public List<OptionData> getOptions()
    {
        return options;
    }

    /**
     * Gets all options for the specified name.
     *
     * @param  name
     *         The option name
     *
     * @throws IllegalArgumentException
     *         If the provided name is null
     *
     * @return The list of options
     *
     * @see   #getOption(String)
     */
    @Nonnull
    public List<OptionData> getOptionsByName(@Nonnull String name)
    {
        Checks.notNull(name, "Name");
        return options.stream()
                .filter(opt -> opt.getName().equals(name))
                .collect(Collectors.toList());
    }

    /**
     * Gets all options for the specified type.
     *
     * @param  type
     *         The option type
     *
     * @throws IllegalArgumentException
     *         If the provided type is null
     *
     * @return The list of options
     */
    @Nonnull
    public List<OptionData> getOptionsByType(@Nonnull OptionType type)
    {
        Checks.notNull(type, "Type");
        return options.stream()
                .filter(it -> it.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Finds the first option with the specified name.
     *
     * @param  name
     *         The option name
     *
     * @throws IllegalArgumentException
     *         If the name is null
     *
     * @return The option with the provided name, or null if that option is not provided
     */
    @Nullable
    public OptionData getOption(@Nonnull String name)
    {
        List<OptionData> options = getOptionsByName(name);
        return options.isEmpty() ? null : options.get(0);
    }

    /**
     * Whether this interaction has already been acknowledged.
     * <br>Both {@link #acknowledge()} and {@link #reply(String)} acknowledge an interaction.
     * Each interaction can only be acknowledged once.
     *
     * @return True, if this interaction has already been acknowledged
     */
    public boolean isAcknowledged()
    {
        return hook.isAck();
    }

    @Nonnull
    @CheckReturnValue
    public CommandHook getHook()
    {
        return hook;
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction acknowledge()
    {
        Route.CompiledRoute route = Route.Interactions.CALLBACK.compile(getInteractionId(), getInteractionToken());
        return new CommandReplyActionImpl(api, route, this.hook);
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction acknowledge(boolean ephemeral)
    {
        return acknowledge().setEphemeral(ephemeral);
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction reply(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        CommandReplyActionImpl action = (CommandReplyActionImpl) acknowledge();
        return action.applyMessage(message);
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction reply(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return acknowledge().setContent(content);
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction reply(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        Checks.notNull(embed, "MessageEmbed");
        Checks.noneNull(embeds, "MessageEmbed");
        return acknowledge().addEmbeds(embed).addEmbeds(embeds);
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction replyFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return reply(String.format(format, args));
    }

// Currently not supported, sad face
//    @Nonnull
//    @CheckReturnValue
//    public CommandReplyAction reply(@Nonnull File file, @Nonnull AttachmentOption... options)
//    {
//        return acknowledge().addFile(file, options);
//    }
//
//    @Nonnull
//    @CheckReturnValue
//    public CommandReplyAction reply(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
//    {
//        return acknowledge().addFile(file, name, options);
//    }
//
//    @Nonnull
//    @CheckReturnValue
//    public CommandReplyAction reply(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
//    {
//        return acknowledge().addFile(data, name, options);
//    }
//
//    @Nonnull
//    @CheckReturnValue
//    public CommandReplyAction reply(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
//    {
//        return acknowledge().addFile(data, name, options);
//    }

    public static class OptionData // TODO: Move this somewhere else, not sure yet
    {
        private final DataObject data;
        private final OptionType type;
        private final String name;
        private final TLongObjectMap<Object> resolved;

        public OptionData(DataObject data, TLongObjectMap<Object> resolved)
        {
            this.data = data;
            this.type = OptionType.fromKey(data.getInt("type", -1));;
            this.name = data.getString("name");
            this.resolved = resolved;
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
         * The String representation of this option value.
         * <br>This will automatically convert the value to a string if the type is not {@link OptionType#STRING OptionType.STRING}.
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
            default: throw new IllegalStateException("Cannot convert option of type " + type + " to long");
            case STRING:
            case CHANNEL:
            case ROLE:
            case USER:
            case INTEGER:
                return data.getLong("value");
            }
        }

        /**
         * The resolved {@link Member} for this option value.
         * <br>Note that {@link OptionType#USER OptionType.USER} can also accept users that are not members of a guild, in which case this will be null!
         *
         * @throws IllegalStateException
         *         If this option is not of type {@link OptionType#USER USER}
         *
         * @return The resolved {@link Member}, or null
         */
        @Nullable
        public Member getAsMember()
        {
            if (type != OptionType.USER)
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
         *         If this option is not of type {@link OptionType#USER USER}
         *
         * @return The resolved {@link User}
         */
        @Nonnull
        public User getAsUser()
        {
            if (type != OptionType.USER)
                throw new IllegalStateException("Cannot resolve User for option " + getName() + " of type " + type);
            Object object = resolved.get(getAsLong());
            if (object instanceof Member)
                return ((Member) object).getUser();
            if (object instanceof User)
                return (User) object;
            throw new IllegalStateException("Could not resolve user!");
        }

        /**
         * The resolved {@link Role} for this option value.
         *
         * @throws IllegalStateException
         *         If this option is not of type {@link OptionType#ROLE ROLE}
         *
         * @return The resolved {@link Role}
         */
        @Nonnull
        public Role getAsRole()
        {
            if (type != OptionType.ROLE)
                throw new IllegalStateException("Cannot resolve Role for option " + getName() + " of type " + type);
            Object role = resolved.get(getAsLong());
            if (role instanceof Role)
                return (Role) role;
            throw new IllegalStateException("Could not resolve role!");
        }

        @Nullable
        private AbstractChannel getAsChannel()
        {
            if (type != OptionType.CHANNEL)
                throw new IllegalStateException("Cannot resolve AbstractChannel for option " + getName() + " of type " + type);
            return (AbstractChannel) resolved.get(getAsLong()); // TODO: Handle uncached channels correctly
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
            AbstractChannel value = getAsChannel();
            if (value instanceof GuildChannel)
                return (GuildChannel) value;
            throw new IllegalStateException("Could not resolve GuildChannel!");
        }

//        @Nullable
//        public PrivateChannel getAsPrivateChannel()
//        {
//            AbstractChannel value = getAsChannel();
//            return value instanceof PrivateChannel ? (PrivateChannel) value : null;
//        }

        /**
         * The resolved {@link MessageChannel} for this option value.
         * <br>Note that {@link OptionType#CHANNEL OptionType.CHANNEL} can accept channels of any type!
         *
         * @throws IllegalStateException
         *         If this option is not of type {@link OptionType#CHANNEL CHANNEL}
         *
         * @return The resolved {@link MessageChannel}, or null if this was not a message channel
         */
        @Nullable
        public MessageChannel getAsMessageChannel()
        {
            AbstractChannel value = getAsChannel();
            return value instanceof MessageChannel ? (MessageChannel) value : null;
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
            AbstractChannel channel = getAsChannel();
            return channel == null ? ChannelType.UNKNOWN : channel.getType();
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
            if (!(obj instanceof OptionData))
                return false;
            OptionData data = (OptionData) obj;
            return getType() == data.getType() && getName().equals(data.getName());
        }
    }
}
