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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UserContextEvent extends GenericCommandEvent implements UserContextInteraction
{
    private final UserContextInteraction interaction;

    public UserContextEvent(@Nonnull JDA api, long responseNumber, @Nonnull UserContextInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @Nonnull
    @Override
    public User getTarget()
    {
        return interaction.getTarget();
    }

    @Nullable
    @Override
    public Member getTargetMember()
    {
        return interaction.getTargetMember();
    }

    @Nonnull
    @Override
    public InteractionHook getHook()
    {
        return interaction.getHook();
    }

    @Nonnull
    @Override
    public ReplyCallbackAction deferReply()
    {
        return interaction.deferReply();
    }
}
