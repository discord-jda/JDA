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

package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.interactions.command.CommandAutoCompleteInteractionImpl;
import net.dv8tion.jda.internal.interactions.command.MessageContextInteractionImpl;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;
import net.dv8tion.jda.internal.interactions.command.UserContextInteractionImpl;
import net.dv8tion.jda.internal.interactions.component.ButtonInteractionImpl;
import net.dv8tion.jda.internal.interactions.component.EntitySelectInteractionImpl;
import net.dv8tion.jda.internal.interactions.component.StringSelectInteractionImpl;
import net.dv8tion.jda.internal.interactions.modal.ModalInteractionImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class InteractionCreateHandler extends SocketHandler
{
    public InteractionCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        int type = content.getInt("type");
        int version = content.getInt("version", 1);
        if (version != 1)
        {
            WebSocketClient.LOG.debug("Received interaction with version {}. This version is currently unsupported by this version of JDA. Consider updating!", version);
            return null;
        }

        long guildId = content.getUnsignedLong("guild_id", 0);
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;

        // Check channel type
        DataObject channelJson = content.getObject("channel");
        ChannelType channelType = ChannelType.fromId(channelJson.getInt("type"));
        if (!channelType.isMessage())
        {
            WebSocketClient.LOG.debug("Discarding INTERACTION_CREATE event from unexpected channel type. Channel: {}", channelJson);
            return null;
        }

        switch (InteractionType.fromKey(type))
        {
            case COMMAND: // slash commands
                handleCommand(content);
                break;
            case COMPONENT: // buttons/components
                handleAction(content);
                break;
            case COMMAND_AUTOCOMPLETE:
                api.handleEvent(
                    new CommandAutoCompleteInteractionEvent(api, responseNumber,
                        new CommandAutoCompleteInteractionImpl(api, content)));
                break;
            case MODAL_SUBMIT:
                api.handleEvent(
                    new ModalInteractionEvent(api, responseNumber,
                        new ModalInteractionImpl(api, content)));
                break;
            default:
                api.handleEvent(
                    new GenericInteractionCreateEvent(api, responseNumber,
                        new InteractionImpl(api, content)));
        }

        return null;
    }

    private void handleCommand(DataObject content)
    {
        switch (Command.Type.fromId(content.getObject("data").getInt("type")))
        {
        case SLASH:
            api.handleEvent(
                new SlashCommandInteractionEvent(api, responseNumber,
                    new SlashCommandInteractionImpl(api, content)));
            break;
        case MESSAGE:
            api.handleEvent(
                new MessageContextInteractionEvent(api, responseNumber,
                    new MessageContextInteractionImpl(api, content)));
            break;
        case USER:
            api.handleEvent(
                new UserContextInteractionEvent(api, responseNumber,
                    new UserContextInteractionImpl(api, content)));
            break;
        }
    }

    private void handleAction(DataObject content)
    {
        switch (Component.Type.fromKey(content.getObject("data").getInt("component_type")))
        {
        case BUTTON:
            api.handleEvent(
                new ButtonInteractionEvent(api, responseNumber,
                    new ButtonInteractionImpl(api, content)));
            break;
        case STRING_SELECT:
            api.handleEvent(
                new StringSelectInteractionEvent(api, responseNumber,
                    new StringSelectInteractionImpl(api, content)));
            break;
        case USER_SELECT:
        case ROLE_SELECT:
        case MENTIONABLE_SELECT:
        case CHANNEL_SELECT:
            api.handleEvent(
                new EntitySelectInteractionEvent(api, responseNumber,
                    new EntitySelectInteractionImpl(api, content)));
            break;
        }
    }
}
