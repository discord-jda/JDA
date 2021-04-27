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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.interactions.CommandInteractionImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SlashCommandEvent extends GenericInteractionCreateEvent implements CommandInteraction
{
    private final CommandInteractionImpl commandInteraction;

    public SlashCommandEvent(@Nonnull JDA api, long responseNumber, @Nonnull CommandInteractionImpl interaction)
    {
        super(api, responseNumber, interaction);
        this.commandInteraction = interaction;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannel getChannel()
    {
        return (MessageChannel) super.getChannel();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return commandInteraction.getName();
    }

    @Nullable
    @Override
    public String getSubcommandName()
    {
        return commandInteraction.getSubcommandName();
    }

    @Nullable
    @Override
    public String getSubcommandGroup()
    {
        return commandInteraction.getSubcommandGroup();
    }

    @Override
    public long getCommandIdLong()
    {
        return commandInteraction.getCommandIdLong();
    }

    @Nonnull
    @Override
    public List<OptionMapping> getOptions()
    {
        return commandInteraction.getOptions();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InteractionHook getHook()
    {
        return commandInteraction.getHook();
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
}
