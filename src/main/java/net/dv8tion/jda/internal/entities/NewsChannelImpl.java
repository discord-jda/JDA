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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.channel.concrete.NewsChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.BaseGuildMessageChannelMixin;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NewsChannelImpl extends AbstractGuildChannelImpl<NewsChannelImpl> implements NewsChannel, BaseGuildMessageChannelMixin<NewsChannelImpl>
{
    private final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    private String topic;
    private long parentCategoryId;
    private long latestMessageId;
    private int position;
    private boolean nsfw;

    public NewsChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.NEWS;
    }
    
    @Nullable
    @Override
    public String getTopic()
    {
        return topic;
    }

    @Override
    public boolean isNSFW()
    {
        return nsfw;
    }

    @Override
    public long getParentCategoryIdLong()
    {
        return parentCategoryId;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(getGuild().getMembersView().stream()
            .filter(m -> m.hasPermission(this, Permission.VIEW_CHANNEL))
            .collect(Collectors.toList()));
    }

    @Override
    public int getPositionRaw()
    {
        return position;
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return latestMessageId;
    }

    @Nonnull
    @Override
    public RestAction<Webhook.WebhookReference> follow(@Nonnull String targetChannelId)
    {
        Checks.notNull(targetChannelId, "Target Channel ID");

        Route.CompiledRoute route = Route.Channels.FOLLOW_CHANNEL.compile(getId());
        DataObject body = DataObject.empty().put("webhook_channel_id", targetChannelId);
        return new RestActionImpl<>(getJDA(), route, body, (response, request) -> {
            DataObject json = response.getObject();
            return new Webhook.WebhookReference(request.getJDA(), json.getUnsignedLong("webhook_id") , json.getUnsignedLong("channel_id"));
        });
    }

    @Nonnull
    @Override
    public ChannelAction<NewsChannel> createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        ChannelAction<NewsChannel> action = guild.createNewsChannel(name).setNSFW(nsfw).setTopic(topic);
        if (guild.equals(getGuild()))
        {
            Category parent = getParentCategory();
            if (parent != null)
                action.setParent(parent);
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
    public NewsChannelManager getManager()
    {
        return null;
    }

    @Override
    public TLongObjectMap<PermissionOverride> getPermissionOverrideMap()
    {
        return overrides;
    }

    @Override
    public NewsChannelImpl setParentCategory(long parentCategoryId)
    {
        this.parentCategoryId = parentCategoryId;
        return this;
    }

    @Override
    public NewsChannelImpl setPosition(int position)
    {
        getGuild().getNewsChannelView().clearCachedLists();
        this.position = position;
        return this;
    }

    @Override
    public NewsChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    @Override
    public NewsChannelImpl setNSFW(boolean nsfw)
    {
        this.nsfw = nsfw;
        return this;
    }

    @Override
    public NewsChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }

    // -- Object Overrides --
    @Override
    public String toString()
    {
        return "NC:" + getName() + '(' + id + ')';
    }
}
