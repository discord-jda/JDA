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

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.commandEvents.MessageCommandEvent;
import net.dv8tion.jda.api.events.interaction.commandEvents.SlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.commandEvents.UserCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionType;
import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.ButtonInteractionImpl;
import net.dv8tion.jda.internal.interactions.commandInteractionImpls.MessageCommandInteractionImpl;
import net.dv8tion.jda.internal.interactions.commandInteractionImpls.SlashCommandInteractionImpl;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuInteractionImpl;
import net.dv8tion.jda.internal.interactions.commandInteractionImpls.UserCommandInteractionImpl;
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
        if (content.getInt("version", 1) != 1)
        {
            WebSocketClient.LOG.debug("Received interaction with version {}. This version is currently unsupported by this version of JDA. Consider updating!", content.getInt("version", 1));
            return null;
        }

        long guildId = content.getUnsignedLong("guild_id", 0);
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;
        if (guildId != 0 && api.getGuildById(guildId) == null)
            return null; // discard event if its not from a guild we are currently in

        switch (InteractionType.fromKey(type))
        {
            case COMMAND: // commands
                handleCommand(content);
                break;
            case COMPONENT: // buttons/components
                handleAction(content);
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
        switch (CommandType.fromKey(content.getObject("data").getInt("type")))
        {
        case SLASH_COMMAND:
            api.handleEvent(
                    new SlashCommandEvent(api, responseNumber,
                            new SlashCommandInteractionImpl(api, content)));
            break;
        case USER_COMMAND:
            api.handleEvent(
                    new UserCommandEvent(api, responseNumber,
                            new UserCommandInteractionImpl(api, content)));
            break;
        case MESSAGE_COMMAND:
            api.handleEvent(
                    new MessageCommandEvent(api, responseNumber,
                            new MessageCommandInteractionImpl(api, content)));
            break;
        }
    }

    private void handleAction(DataObject content)
    {
        switch (Component.Type.fromKey(content.getObject("data").getInt("component_type")))
        {
        case BUTTON:
            api.handleEvent(
                new ButtonClickEvent(api, responseNumber,
                    new ButtonInteractionImpl(api, content)));
            break;
        case SELECTION_MENU:
            api.handleEvent(
                new SelectionMenuEvent(api, responseNumber,
                    new SelectionMenuInteractionImpl(api, content)));
            break;
        }
    }
}
