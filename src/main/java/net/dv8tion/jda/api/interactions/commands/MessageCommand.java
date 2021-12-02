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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;

import javax.annotation.Nonnull;

public class MessageCommand extends Command
{
    public MessageCommand(JDAImpl api, Guild guild, DataObject json)
    {
        super(api, guild, json);
    }

    @Nonnull
    @Override
    public CommandEditAction editCommand() {
        if (applicationId != api.getSelfUser().getApplicationIdLong())
            throw new IllegalStateException("Cannot edit a command from another bot!");
        return guild == null ?
                new CommandEditActionImpl(api, getId(), CommandType.MESSAGE_CONTEXT) :
                new CommandEditActionImpl(guild, getId(), CommandType.MESSAGE_CONTEXT);
    }

    @Override
    public String toString()
    {
        return "MC:" + getName() + "(" + getId() + ")";
    }
}
