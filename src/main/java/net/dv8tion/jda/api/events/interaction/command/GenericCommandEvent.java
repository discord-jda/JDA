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

package net.dv8tion.jda.api.events.interaction.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class GenericCommandEvent extends GenericInteractionCreateEvent implements CommandPayload
{
    public GenericCommandEvent(@Nonnull JDA api, long responseNumber, @Nonnull CommandInteraction interaction)
    {
        super(api, responseNumber, interaction);
    }

    @Nonnull
    @Override
    public CommandInteraction getInteraction()
    {
        return (CommandInteraction) super.getInteraction();
    }

    @NotNull
    @Override
    public String getName()
    {
        return getInteraction().getName();
    }

    @Nullable
    @Override
    public String getSubcommandName()
    {
        return getInteraction().getSubcommandName();
    }

    @Nullable
    @Override
    public String getSubcommandGroup()
    {
        return getInteraction().getSubcommandGroup();
    }

    @Override
    public long getCommandIdLong()
    {
        return getInteraction().getCommandIdLong();
    }

    @NotNull
    @Override
    public List<OptionMapping> getOptions()
    {
        return getInteraction().getOptions();
    }
}
