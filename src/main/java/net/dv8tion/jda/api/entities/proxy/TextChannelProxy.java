/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.entities.proxy;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class TextChannelProxy extends GuildChannelProxy implements TextChannel
{
    public TextChannelProxy(TextChannel channel)
    {
        super(channel);
    }

    @Override
    public TextChannel getSubject()
    {
        TextChannel channel = api.getTextChannelById(id);
        if (channel == null)
            throw new ProxyResolutionException("TextChannel(" + getId() + ")");
        return channel;
    }

    @Override
    public TextChannelProxy getProxy()
    {
        return this;
    }

    @Override
    public ChannelAction<TextChannel> createCopy()
    {
        return getSubject().createCopy();
    }

    @Override
    public ChannelAction<TextChannel> createCopy(Guild guild)
    {
        return getSubject().createCopy(guild);
    }

    @Override
    public String getTopic()
    {
        return getSubject().getTopic();
    }

    @Override
    public boolean isNSFW()
    {
        return getSubject().isNSFW();
    }

    @Override
    public int getSlowmode()
    {
        return getSubject().getSlowmode();
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return getSubject().getLatestMessageIdLong();
    }

    @Override
    public boolean hasLatestMessage()
    {
        return getSubject().hasLatestMessage();
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks()
    {
        return getSubject().getWebhooks();
    }

    @Override
    public WebhookAction createWebhook(String name)
    {
        return getSubject().createWebhook(name);
    }

    @Override
    public RestAction<Void> deleteMessages(Collection<Message> messages)
    {
        return getSubject().deleteMessages(messages);
    }

    @Override
    public RestAction<Void> deleteMessagesByIds(Collection<String> messageIds)
    {
        return getSubject().deleteMessagesByIds(messageIds);
    }

    @Override
    public AuditableRestAction<Void> deleteWebhookById(String id)
    {
        return getSubject().deleteWebhookById(id);
    }

    @Override
    public RestAction<Void> clearReactionsById(String messageId)
    {
        return getSubject().clearReactionsById(messageId);
    }

    @Override
    public RestAction<Void> removeReactionById(String messageId, String unicode, User user)
    {
        return getSubject().removeReactionById(messageId, unicode, user);
    }

    @Override
    public boolean canTalk()
    {
        return getSubject().canTalk();
    }

    @Override
    public boolean canTalk(Member member)
    {
        return getSubject().canTalk(member);
    }

    @Override
    public int compareTo(@NotNull TextChannel o)
    {
        return getSubject().compareTo(o);
    }

    @Override
    public String getAsMention()
    {
        return getSubject().getAsMention();
    }
}
