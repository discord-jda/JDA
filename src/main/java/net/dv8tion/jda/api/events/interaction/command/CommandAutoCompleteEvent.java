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
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CommandAutoCompleteEvent extends GenericInteractionCreateEvent implements CommandAutoCompleteInteraction
{
    private final CommandAutoCompleteInteraction interaction;

    public CommandAutoCompleteEvent(@Nonnull JDA api, long responseNumber, @Nonnull CommandAutoCompleteInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @Nonnull
    @Override
    public OptionMapping getFocusedOption()
    {
        return interaction.getFocusedOption();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return interaction.getName();
    }

    @Nullable
    @Override
    public String getSubcommandName()
    {
        return interaction.getSubcommandName();
    }

    @Nullable
    @Override
    public String getSubcommandGroup()
    {
        return interaction.getSubcommandGroup();
    }

    @Override
    public long getCommandIdLong()
    {
        return interaction.getCommandIdLong();
    }

    @Nonnull
    @Override
    public List<OptionMapping> getOptions()
    {
        return interaction.getOptions();
    }
}
