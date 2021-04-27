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

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Incubating
public class GenericInteractionCreateEvent extends Event implements Interaction
{
    private final Interaction interaction;

    public GenericInteractionCreateEvent(@Nonnull JDA api, long responseNumber, @Nonnull Interaction interaction)
    {
        super(api, responseNumber);
        this.interaction = interaction;
    }

    @Nonnull
    public Interaction getInteraction()
    {
        return interaction;
    }

    @Nonnull
    @Override
    public String getToken()
    {
        return interaction.getToken();
    }

    public int getTypeRaw()
    {
        return interaction.getTypeRaw();
    }

    @Nullable
    public Guild getGuild()
    {
        return interaction.getGuild();
    }

    public boolean isFromGuild()
    {
        return getGuild() != null;
    }

    @Nullable
    @Override
    public AbstractChannel getChannel()
    {
        return interaction.getChannel();
    }

    @Nullable
    public Member getMember()
    {
        return interaction.getMember();
    }

    @Nonnull
    public User getUser()
    {
        return interaction.getUser();
    }

    @Override
    public long getIdLong()
    {
        return interaction.getIdLong();
    }

    @Override
    public boolean isAcknowledged()
    {
        return interaction.isAcknowledged();
    }

    @Nonnull
    @Override
    public RestAction<InteractionHook> acknowledge()
    {
        return interaction.acknowledge();
    }
}
