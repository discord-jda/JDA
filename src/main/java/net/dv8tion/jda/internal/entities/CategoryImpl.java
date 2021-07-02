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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.InviteAction;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryImpl extends AbstractChannelImpl<Category, CategoryImpl> implements Category
{
    public CategoryImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public CategoryImpl setPosition(int rawPosition)
    {
        getGuild().getCategoriesView().clearCachedLists();
        return super.setPosition(rawPosition);
    }

    @Override
    public Category getParent()
    {
        return null;
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.CATEGORY;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(getChannels().stream()
                    .map(GuildChannel::getMembers)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList()));
    }

    @Override
    public int getPosition()
    {
        //We call getCategories instead of directly accessing the GuildImpl.getCategories because
        // getCategories does the sorting logic.
        List<Category> channels = getGuild().getCategories();
        for (int i = 0; i < channels.size(); i++)
        {
            if (equals(channels.get(i)))
                return i;
        }
        throw new IllegalStateException("Somehow when determining position we never found the Category in the Guild's channels? wtf?");
    }

    @Nonnull
    @Override
    public ChannelAction<Category> createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        ChannelAction<Category> action = guild.createCategory(name);
        if (guild.equals(getGuild()))
        {
            for (PermissionOverride o : overrides.valueCollection())
            {
                if (o.isMemberOverride())
                    action.addMemberPermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                else
                    action.addRolePermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
            }
        }
        return action;
    }

    @Nonnull
    @Override
    public InviteAction createInvite()
    {
        throw new UnsupportedOperationException("Cannot create invites for category!");
    }

    @Nonnull
    @Override
    public RestAction<List<Invite>> retrieveInvites()
    {
        return new CompletedRestAction<>(getJDA(), Collections.emptyList());
    }

    @Nonnull
    @Override
    public List<GuildChannel> getChannels()
    {
        List<GuildChannel> channels = new ArrayList<>();
        channels.addAll(getStoreChannels());
        channels.addAll(getTextChannels());
        channels.addAll(getVoiceChannels());
        Collections.sort(channels);
        return Collections.unmodifiableList(channels);
    }

    @Nonnull
    @Override
    public List<StoreChannel> getStoreChannels()
    {
        return Collections.unmodifiableList(getGuild().getStoreChannelCache().stream()
                    .filter(channel -> equals(channel.getParent()))
                    .sorted().collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public List<TextChannel> getTextChannels()
    {
        return Collections.unmodifiableList(getGuild().getTextChannels().stream()
                    .filter(channel -> equals(channel.getParent()))
                    .sorted().collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(getGuild().getVoiceChannels().stream()
                    .filter(channel -> equals(channel.getParent()))
                    .sorted().collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public ChannelAction<TextChannel> createTextChannel(@Nonnull String name)
    {
        ChannelAction<TextChannel> action = getGuild().createTextChannel(name, this);
        return trySync(action);
    }

    @Nonnull
    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name)
    {
        ChannelAction<VoiceChannel> action = getGuild().createVoiceChannel(name, this);
        return trySync(action);
    }

    @Nonnull
    @Override
    public ChannelAction<StageChannel> createStageChannel(@Nonnull String name)
    {
        ChannelAction<StageChannel> action = getGuild().createStageChannel(name, this);
        return trySync(action);
    }

    private <T extends GuildChannel> ChannelAction<T> trySync(ChannelAction<T> action)
    {
        Member selfMember = getGuild().getSelfMember();
        if (!selfMember.canSync(this))
        {
            long botPerms = PermissionUtil.getEffectivePermission(this, selfMember);
            for (PermissionOverride override : getPermissionOverrides())
            {
                long perms = override.getDeniedRaw() | override.getAllowedRaw();
                if ((perms & ~botPerms) != 0)
                    return action;
            }
        }
        return action.syncPermissionOverrides();
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyTextChannelPositions()
    {
        return getGuild().modifyTextChannelPositions(this);
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyVoiceChannelPositions()
    {
        return getGuild().modifyVoiceChannelPositions(this);
    }

    @Override
    public String toString()
    {
        return "GC:" + getName() + '(' + id + ')';
    }
}
