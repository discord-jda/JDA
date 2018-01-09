/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.dv8tion.jda.core.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryImpl extends AbstractChannelImpl<CategoryImpl> implements Category
{
    protected final TLongObjectMap<Channel> channels = MiscUtil.newLongMap();

    public CategoryImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public Category getParent()
    {
        return null;
    }

    @Override
    public int compareTo(Category other)
    {
        Checks.notNull(other, "Other Category");
        if (other.equals(this))
            return 0;
        Checks.check(getGuild().equals(other.getGuild()), "Cannot compare categories from different guilds!");
        if (rawPosition == other.getPositionRaw())
            return Long.compare(id, other.getIdLong());
        return Integer.compare(rawPosition, other.getPositionRaw());
    }

    @Override
    public ChannelType getType()
    {
        return ChannelType.CATEGORY;
    }

    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(getChannels().stream()
                    .map(Channel::getMembers)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList()));
    }

    @Override
    public int getPosition()
    {
        //We call getCategories instead of directly accessing the GuildImpl.getCategories because
        // getCategories does the sorting logic.
        List<Category> channels = guild.getCategories();
        for (int i = 0; i < channels.size(); i++)
        {
            if (channels.get(i) == this)
                return i;
        }
        throw new AssertionError("Somehow when determining position we never found the Category in the Guild's channels? wtf?");
    }

    @Override
    public ChannelAction createCopy(Guild guild)
    {
        Checks.notNull(guild, "Guild");
        ChannelAction action = guild.getController().createCategory(name);
        if (guild.equals(getGuild()))
        {
            for (PermissionOverride o : overrides.valueCollection())
            {
                if (o.isMemberOverride())
                    action.addPermissionOverride(o.getMember(), o.getAllowedRaw(), o.getDeniedRaw());
                else
                    action.addPermissionOverride(o.getRole(), o.getAllowedRaw(), o.getDeniedRaw());
            }
        }
        return action;
    }

    @Override
    public InviteAction createInvite()
    {
        throw new UnsupportedOperationException("Cannot create invites for category!");
    }

    @Override
    public RestAction<List<Invite>> getInvites()
    {
        return new RestAction.EmptyRestAction<>(getJDA(), Collections.emptyList());
    }

    @Override
    public List<Channel> getChannels()
    {
        List<Channel> channels = new ArrayList<>();
        channels.addAll(getTextChannels());
        channels.addAll(getVoiceChannels());
        return Collections.unmodifiableList(channels);
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        return Collections.unmodifiableList(getGuild().getTextChannels().stream()
                    .filter(channel -> channel.getParent() != null)
                    .filter(channel -> channel.getParent().equals(this))
                    .collect(Collectors.toList()));
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(getGuild().getVoiceChannels().stream()
                    .filter(channel -> channel.getParent() != null)
                    .filter(channel -> channel.getParent().equals(this))
                    .collect(Collectors.toList()));
    }

    @Override
    public ChannelAction createTextChannel(String name)
    {
        ChannelAction action = guild.getController().createTextChannel(name).setParent(this);
        applyPermission(action);
        return action;
    }

    @Override
    public ChannelAction createVoiceChannel(String name)
    {
        ChannelAction action = guild.getController().createVoiceChannel(name).setParent(this);
        applyPermission(action);
        return action;
    }

    @Override
    public CategoryOrderAction<TextChannel> modifyTextChannelPositions()
    {
        return getGuild().getController().modifyTextChannelPositions(this);
    }

    @Override
    public CategoryOrderAction<VoiceChannel> modifyVoiceChannelPositions()
    {
        return getGuild().getController().modifyVoiceChannelPositions(this);
    }

    @Override
    public String toString()
    {
        return "GC:" + getName() + '(' + id + ')';
    }

    private void applyPermission(ChannelAction a)
    {
        overrides.forEachValue(override ->
        {
            if (override.isMemberOverride())
                a.addPermissionOverride(override.getMember(), override.getAllowedRaw(), override.getDeniedRaw());
            else
                a.addPermissionOverride(override.getRole(), override.getAllowedRaw(), override.getDeniedRaw());
            return true;
        });
    }
}
