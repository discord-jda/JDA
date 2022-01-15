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
import net.dv8tion.jda.api.events.interaction.GenericAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Indicates that a user is typing in a {@link net.dv8tion.jda.api.interactions.commands.build.OptionData option} which
 * supports {@link net.dv8tion.jda.api.interactions.commands.build.OptionData#setAutoComplete(boolean) auto-complete}.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 *
 * @see CommandAutoCompleteInteraction
 * @see IAutoCompleteCallback
 */
public class CommandAutoCompleteInteractionEvent extends GenericAutoCompleteInteractionEvent implements CommandAutoCompleteInteraction
{
    private final CommandAutoCompleteInteraction interaction;

    public CommandAutoCompleteInteractionEvent(@Nonnull JDA api, long responseNumber, @Nonnull CommandAutoCompleteInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @Nonnull
    @Override
    public CommandAutoCompleteInteraction getInteraction()
    {
        return interaction;
    }

    @Nonnull
    @Override
    public AutoCompleteQuery getFocusedOption()
    {
        return interaction.getFocusedOption();
    }

    @Nonnull
    @Override
    public Command.Type getCommandType()
    {
        return interaction.getCommandType();
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

    @Nonnull
    @Override
    public AutoCompleteCallbackAction replyChoices(@Nonnull Collection<Command.Choice> choices)
    {
        return interaction.replyChoices(choices);
    }
}
