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
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationOwners;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Indicates that an {@link Interaction} was created.
 * <br>Every interaction event is derived from this event.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 *
 * @see Interaction
 */
public class GenericInteractionCreateEvent extends Event implements Interaction
{
    private final Interaction interaction;

    public GenericInteractionCreateEvent(@Nonnull JDA api, long responseNumber, @Nonnull Interaction interaction)
    {
        super(api, responseNumber);
        this.interaction = interaction;
    }

    /**
     * The {@link Interaction} instance.
     * <br>Note that this event is a delegate which implements the same interface.
     *
     * @return The {@link Interaction}
     */
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

    @Override
    public int getTypeRaw()
    {
        return interaction.getTypeRaw();
    }

    @Nullable
    @Override
    public Guild getGuild()
    {
        return interaction.getGuild();
    }

    @Nullable
    @Override
    public Channel getChannel()
    {
        return interaction.getChannel();
    }

    @Override
    public long getChannelIdLong()
    {
        return interaction.getChannelIdLong();
    }

    @Nonnull
    @Override
    public DiscordLocale getUserLocale()
    {
        return interaction.getUserLocale();
    }

    @Nonnull
    @Override
    public DiscordLocale getGuildLocale()
    {
        return interaction.getGuildLocale();
    }

    @Nonnull
    @Override
    public InteractionContextType getContext()
    {
        return interaction.getContext();
    }

    @Nullable
    @Override
    public IntegrationOwners getIntegrationOwners()
    {
        return interaction.getIntegrationOwners();
    }

    @Nullable
    @Override
    public Member getMember()
    {
        return interaction.getMember();
    }

    @Nonnull
    @Override
    public User getUser()
    {
        return interaction.getUser();
    }

    @Nonnull
    @Override
    public List<Entitlement> getEntitlements()
    {
        return interaction.getEntitlements();
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
}
