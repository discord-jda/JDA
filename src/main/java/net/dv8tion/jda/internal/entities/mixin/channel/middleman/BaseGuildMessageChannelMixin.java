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

package net.dv8tion.jda.internal.entities.mixin.channel.middleman;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.*;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface BaseGuildMessageChannelMixin<T extends BaseGuildMessageChannelMixin<T>> extends
        BaseGuildMessageChannel,
        GuildMessageChannelMixin<T>,
        IThreadContainerMixin<T>,
        ICategorizableChannelMixin<T>,
        IPositionableChannelMixin<T>,
        IPermissionContainerMixin<T>,
        IInviteContainerMixin<T>
{
    // ---- Default implementations of interface ----
    @Override
    default boolean canTalk(@Nonnull Member member)
    {
        if (!getGuild().equals(member.getGuild()))
            throw new IllegalArgumentException("Provided Member is not from the Guild that this NewsChannel is part of.");

        return member.hasPermission(this, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    }

    @Nonnull
    @Override
    default RestAction<List<Webhook>> retrieveWebhooks()
    {
        checkPermission(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Channels.GET_WEBHOOKS.compile(getId());
        JDAImpl jda = (JDAImpl) getJDA();
        return new RestActionImpl<>(jda, route, (response, request) ->
        {
            DataArray array = response.getArray();
            List<Webhook> webhooks = new ArrayList<>(array.length());
            EntityBuilder builder = jda.getEntityBuilder();

            for (int i = 0; i < array.length(); i++)
            {
                try
                {
                    webhooks.add(builder.createWebhook(array.getObject(i)));
                }
                catch (UncheckedIOException | NullPointerException e)
                {
                    JDAImpl.LOG.error("Error while creating websocket from json", e);
                }
            }

            return Collections.unmodifiableList(webhooks);
        });
    }

    @Nonnull
    @Override
    default WebhookAction createWebhook(@Nonnull String name)
    {
        Checks.notBlank(name, "Webhook name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");

        checkPermission(Permission.MANAGE_WEBHOOKS);

        return new WebhookActionImpl(getJDA(), this, name);
    }

    @Nonnull
    @Override
    default AuditableRestAction<Void> deleteWebhookById(@Nonnull String id)
    {
        Checks.isSnowflake(id, "Webhook ID");

        checkPermission(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Webhooks.DELETE_WEBHOOK.compile(id);
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    // ---- State Accessors ----
    T setTopic(String topic);

    T setNSFW(boolean nsfw);
}
