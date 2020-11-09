/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.CommandReplyAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.commands.CommandThreadImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.CommandReplyActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class SlashCommandEvent extends GenericInteractionEvent
{
    private final String name;
    private final long commandId;
    private final List<OptionData> options;

    public SlashCommandEvent(@Nonnull JDA api, long responseNumber, @Nonnull String token, long interactionId,
                             @Nullable Guild guild, @Nullable Member member, @Nullable User user, @Nonnull MessageChannel channel,
                             @Nonnull String name, long commandId, @Nonnull List<DataObject> options)
    {
        super(api, responseNumber, token, interactionId, guild, member, user, channel);
        this.name = name;
        this.commandId = commandId;
        this.options = options.stream()
                .map(OptionData::new)
                .collect(Collectors.toList());
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

    // You can only reply ONCE so maybe we should throw when trying again?

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction acknowledge()
    {
        Route.CompiledRoute route = Route.Interactions.CALLBACK.compile(getInteractionId(), getInteractionToken());
        return new CommandReplyActionImpl(api, route, new CommandThreadImpl(this));
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

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction reply(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        return acknowledge().addFile(file, options);
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction reply(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return acknowledge().addFile(file, name, options);
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction reply(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return acknowledge().addFile(data, name, options);
    }

    @Nonnull
    @CheckReturnValue
    public CommandReplyAction reply(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return acknowledge().addFile(data, name, options);
    }

    public static class OptionData // TODO: Move this somewhere else, not sure yet
    {
        private final DataObject data;

        private OptionData(DataObject data)
        {
            this.data = data;
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

        // TODO: Role/user/channel when it comes around - currently they are just ids... which is impossible to figure out
        // TODO: How to handle subcommands? What does it look like?

        public List<OptionData> getOptions() // this is for subcommands, i think
        {
            return data.optArray("options")
                    .orElseGet(DataArray::empty)
                    .stream(DataArray::getObject)
                    .map(OptionData::new)
                    .collect(Collectors.toList());
        }

        public List<OptionData> getOptionsByName(@Nonnull String name)
        {
            Checks.notNull(name, "Name");
            return data.optArray("options")
                .orElseGet(DataArray::empty)
                .stream(DataArray::getObject)
                .map(OptionData::new)
                .filter(option -> option.getName().equals(name))
                .collect(Collectors.toList());
        }

        @Override
        public String toString()
        {
            return "Option(" + getName() + "=" + getAsString() + ")";
        }

        @Override
        public int hashCode()
        {
            return data.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof OptionData))
                return false;
            return data.equals(((OptionData) obj).data);
        }
    }
}
