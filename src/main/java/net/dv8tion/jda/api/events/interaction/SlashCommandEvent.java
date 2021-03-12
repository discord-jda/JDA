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
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.commands.CommandThread;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.CommandReplyAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.commands.CommandThreadImpl;
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

public class SlashCommandEvent extends GenericInteractionEvent
{
    private final String name;
    private final long commandId;
    private final List<OptionData> options;
    private final CommandThreadImpl thread;

    public SlashCommandEvent(@Nonnull JDA api, long responseNumber, @Nonnull String token, long interactionId,
                             @Nullable Guild guild, @Nullable Member member, @Nonnull User user, @Nonnull MessageChannel channel,
                             @Nonnull String name, long commandId, @Nonnull List<OptionData> options)
    {
        super(api, responseNumber, token, interactionId, guild, member, user, channel);
        this.name = name;
        this.commandId = commandId;
        this.options = Collections.unmodifiableList(options);
        this.thread = new CommandThreadImpl(this);
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

    public long getCommandIdLong()
    {
        return commandId;
    }

    @Nonnull
    public String getCommandId()
    {
        return Long.toUnsignedString(commandId);
    }

    @Nonnull
    public List<OptionData> getOptions()
    {
        return options;
    }

    @Nonnull
    public List<OptionData> getOptionsByName(@Nonnull String name)
    {
        Checks.notNull(name, "Name");
        return options.stream()
                .filter(opt -> opt.getName().equals(name))
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<OptionData> getOptionsByType(@Nonnull Command.OptionType type)
    {
        Checks.notNull(type, "Type");
        return options.stream()
                .filter(it -> it.getType() == type)
                .collect(Collectors.toList());
    }

    @Nullable
    public OptionData getOption(@Nonnull String name)
    {
        List<OptionData> options = getOptionsByName(name);
        return options.isEmpty() ? null : options.get(0);
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannel getChannel() // slash commands always happen in channels but interactions don't
    {
        return super.getChannel();
    }

    // You can only reply ONCE so maybe we should throw when trying again?

    @Nonnull
    @CheckReturnValue
    public CommandThread getThread()
    {
        return thread;
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction acknowledge()
    {
        Route.CompiledRoute route = Route.Interactions.CALLBACK.compile(getInteractionId(), getInteractionToken());
        return new CommandReplyActionImpl(api, route, this.thread);
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
        Message message = new MessageBuilder()
                .appendFormat(format, args)
                .build();
        return reply(message);
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
        private final Command.OptionType type;
        private final TLongObjectMap<Object> resolved;

        public OptionData(DataObject data, TLongObjectMap<Object> resolved)
        {
            this.data = data;
            this.type = Command.OptionType.fromKey(data.getInt("type", -1));;
            this.resolved = resolved;
        }

        public Command.OptionType getType()
        {
            return type;
        }

        public String getName()
        {
            return data.getString("name");
        }

        public String getAsString()
        {
            return data.getString("value");
        }

        public boolean getAsBoolean()
        {
            return data.getBoolean("value");
        }

        public long getAsLong()
        {
            return data.getLong("value");
        }

        @Nullable
        public Member getAsMember()
        {
            if (type != Command.OptionType.USER)
                throw new IllegalStateException("Cannot resolve Member for option " + getName() + " of type " + type);
            Object object = resolved.get(getAsLong());
            if (object instanceof Member)
                return (Member) object;
            return null; // Unresolved
        }

        @Nullable
        public User getAsUser()
        {
            if (type != Command.OptionType.USER)
                throw new IllegalStateException("Cannot resolve User for option " + getName() + " of type " + type);
            Object object = resolved.get(getAsLong());
            if (object instanceof Member)
                return ((Member) object).getUser();
            if (object instanceof User)
                return (User) object;
            return null; // Unresolved
        }

        @Nullable
        public Role getAsRole()
        {
            if (type != Command.OptionType.ROLE)
                throw new IllegalStateException("Cannot resolve Role for option " + getName() + " of type " + type);
            return (Role) resolved.get(getAsLong());
        }

        // TODO: Handle channels (new type?)
        // TODO: How to handle subcommands? What does it look like?

        @Nonnull
        public List<OptionData> getOptions() // this is for subcommands, i think
        {
            return data.optArray("options")
                    .orElseGet(DataArray::empty)
                    .stream(DataArray::getObject)
                    .map(d -> new OptionData(d, resolved))
                    .collect(Collectors.toList());
        }

        @Nonnull
        public List<OptionData> getOptionsByName(@Nonnull String name)
        {
            Checks.notNull(name, "Name");
            return getOptions()
                .stream()
                .filter(option -> option.getName().equals(name))
                .collect(Collectors.toList());
        }

        @Nonnull
        public List<OptionData> getOptionsByType(@Nonnull Command.OptionType type)
        {
            Checks.notNull(type, "Type");
            return getOptions()
                .stream()
                .filter(it -> it.getType() == type)
                .collect(Collectors.toList());
        }

        @Nullable
        public OptionData getOption(@Nonnull String name)
        {
            List<OptionData> options = getOptionsByName(name);
            return options.isEmpty() ? null : options.get(0);
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
