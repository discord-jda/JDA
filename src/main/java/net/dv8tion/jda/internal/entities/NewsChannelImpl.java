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
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public class NewsChannelImpl extends BaseGuildMessageChannelImpl<NewsChannel, NewsChannelImpl> implements NewsChannel
{
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

    // -- Setters --

    @Override
    public NewsChannelImpl setPosition(int rawPosition)
    {
        getGuild().getNewsChannelView().clearCachedLists();
        return super.setPosition(rawPosition);
    }

    // -- Object Overrides --
    @Override
    public String toString()
    {
        return "NC:" + getName() + '(' + id + ')';
    }
}
