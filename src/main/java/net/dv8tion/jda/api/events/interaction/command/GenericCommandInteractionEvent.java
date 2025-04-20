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
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Indicates that a {@link CommandInteraction} was used.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public class GenericCommandInteractionEvent extends GenericInteractionCreateEvent implements CommandInteraction
{
    public GenericCommandInteractionEvent(@Nonnull JDA api, long responseNumber, @Nonnull CommandInteraction interaction)
    {
        super(api, responseNumber, interaction);
    }

    @Nonnull
    @Override
    public CommandInteraction getInteraction()
    {
        return (CommandInteraction) super.getInteraction();
    }

    @Nonnull
    @Override
    public Command.Type getCommandType()
    {
        return getInteraction().getCommandType();
    }

    @Nonnull
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

    @Override
    public boolean isGuildCommand()
    {
        return getInteraction().isGuildCommand();
    }

    @Nonnull
    @Override
    public List<OptionMapping> getOptions()
    {
        return getInteraction().getOptions();
    }

    @Nonnull
    @Override
    public InteractionHook getHook()
    {
        return getInteraction().getHook();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ReplyCallbackAction deferReply()
    {
        return getInteraction().deferReply();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ModalCallbackAction replyModal(@Nonnull Modal modal)
    {
        return getInteraction().replyModal(modal);
    }

    /**
     * @deprecated Replaced with {@link net.dv8tion.jda.api.components.button.Button#premium(net.dv8tion.jda.api.entities.SkuSnowflake) Button.premium(SkuSnowflake)},
     * see the <a href="https://discord.com/developers/docs/change-log#premium-apps-new-premium-button-style-deep-linking-url-schemes" target="_blank">Discord change logs</a> for more details.
     */
    @Nonnull
    @Override
    @Deprecated
    @CheckReturnValue
    public PremiumRequiredCallbackAction replyWithPremiumRequired()
    {
        return getInteraction().replyWithPremiumRequired();
    }
}
