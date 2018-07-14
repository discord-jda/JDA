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

import net.dv8tion.jda.client.exceptions.VerificationLevelException;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import net.dv8tion.jda.core.requests.restaction.WebhookAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TextChannelImpl extends AbstractChannelImpl<TextChannelImpl> implements TextChannel
{
    private String topic;
    private long lastMessageId;
    private boolean nsfw;

    public TextChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public String getAsMention()
    {
        return "<#" + id + '>';
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks()
    {
        checkPermission(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Channels.GET_WEBHOOKS.compile(getId());
        return new RestAction<List<Webhook>>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<Webhook>> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                JSONArray array = response.getArray();
                List<Webhook> webhooks = new ArrayList<>(array.length());
                EntityBuilder builder = api.getEntityBuilder();

                for (Object object : array)
                {
                    try
                    {
                        webhooks.add(builder.createWebhook((JSONObject) object));
                    }
                    catch (JSONException | NullPointerException e)
                    {
                        JDAImpl.LOG.error("Error while creating websocket from json", e);
                    }
                }

                request.onSuccess(Collections.unmodifiableList(webhooks));
            }
        };
    }

    @Override
    public WebhookAction createWebhook(String name)
    {
        Checks.notBlank(name, "Webhook name");
        name = name.trim();
        checkPermission(Permission.MANAGE_WEBHOOKS);

        Checks.check(name.length() >= 2 && name.length() <= 100, "Name must be 2-100 characters in length!");

        Route.CompiledRoute route = Route.Channels.CREATE_WEBHOOK.compile(getId());
        return new WebhookAction(getJDA(), route, name);
    }

    @Override
    public RestAction<Void> deleteMessages(Collection<Message> messages)
    {
        Checks.notEmpty(messages, "Messages collection");

        return deleteMessagesByIds(messages.stream()
                .map(ISnowflake::getId)
                .collect(Collectors.toList()));
    }

    @Override
    public RestAction<Void> deleteMessagesByIds(Collection<String> messageIds)
    {
        checkPermission(Permission.MESSAGE_MANAGE, "Must have MESSAGE_MANAGE in order to bulk delete messages in this channel regardless of author.");
        if (messageIds.size() < 2 || messageIds.size() > 100)
            throw new IllegalArgumentException("Must provide at least 2 or at most 100 messages to be deleted.");

        long twoWeeksAgo = ((System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)) - MiscUtil.DISCORD_EPOCH) << MiscUtil.TIMESTAMP_OFFSET;
        for (String id : messageIds)
        {
            Checks.check(MiscUtil.parseSnowflake(id) > twoWeeksAgo, "Message Id provided was older than 2 weeks. Id: " + id);
        }

        JSONObject body = new JSONObject().put("messages", messageIds);
        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGES.compile(getId());
        return new RestAction<Void>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public AuditableRestAction<Void> deleteWebhookById(String id)
    {
        Checks.isSnowflake(id, "Webhook ID");

        if (!guild.getSelfMember().hasPermission(this, Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Webhooks.DELETE_WEBHOOK.compile(id);
        return new AuditableRestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public boolean canTalk()
    {
        return canTalk(guild.getSelfMember());
    }

    @Override
    public boolean canTalk(Member member)
    {
        if (!guild.equals(member.getGuild()))
            throw new IllegalArgumentException("Provided Member is not from the Guild that this TextChannel is part of.");

        return member.hasPermission(this, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE);
    }

    @Override
    public long getLatestMessageIdLong()
    {
        final long messageId = lastMessageId;
        if (messageId == 0)
            throw new IllegalStateException("No last message id found.");
        return messageId;
    }

    @Override
    public boolean hasLatestMessage()
    {
        return lastMessageId != 0;
    }

    @Override
    public ChannelType getType()
    {
        return ChannelType.TEXT;
    }

    @Override
    public String getTopic()
    {
        return topic;
    }

    @Override
    public boolean isNSFW() {
        return nsfw || name.equals("nsfw") || name.startsWith("nsfw-");
    }

    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(guild.getMembersMap().valueCollection().stream()
                .filter(m -> m.hasPermission(this, Permission.MESSAGE_READ))
                .collect(Collectors.toList()));
    }

    @Override
    public int getPosition()
    {
        //We call getTextChannels instead of directly accessing the GuildImpl.getTextChannelMap because
        // getTextChannels does the sorting logic.
        List<TextChannel> channels = guild.getTextChannels();
        for (int i = 0; i < channels.size(); i++)
        {
            if (channels.get(i) == this)
                return i;
        }
        throw new AssertionError("Somehow when determining position we never found the TextChannel in the Guild's channels? wtf?");
    }

    @Override
    public ChannelAction createCopy(Guild guild)
    {
        Checks.notNull(guild, "Guild");
        ChannelAction action = guild.getController().createTextChannel(name).setNSFW(nsfw).setTopic(topic);
        if (guild.equals(getGuild()))
        {
            Category parent = getParent();
            if (parent != null)
                action.setParent(parent);
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
    public MessageAction sendMessage(CharSequence text)
    {
        checkVerification();
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        return TextChannel.super.sendMessage(text);
    }

    @Override
    public MessageAction sendMessage(MessageEmbed embed)
    {
        checkVerification();
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        // this is checked because you cannot send an empty message
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
        return TextChannel.super.sendMessage(embed);
    }

    @Override
    public MessageAction sendMessage(Message msg)
    {
        Checks.notNull(msg, "Message");

        checkVerification();
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        if (msg.getContentRaw().isEmpty() && !msg.getEmbeds().isEmpty())
            checkPermission(Permission.MESSAGE_EMBED_LINKS);

        //Call MessageChannel's default
        return TextChannel.super.sendMessage(msg);
    }

    @Override
    public MessageAction sendFile(InputStream data, String fileName, Message message)
    {
        checkVerification();
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);

        //Call MessageChannel's default method
        return TextChannel.super.sendFile(data, fileName, message);
    }

    @Override
    public RestAction<Message> getMessageById(String messageId)
    {
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return TextChannel.super.getMessageById(messageId);
    }

    @Override
    public AuditableRestAction<Void> deleteMessageById(String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");
        checkPermission(Permission.MESSAGE_READ);

        //Call MessageChannel's default method
        return TextChannel.super.deleteMessageById(messageId);
    }

    @Override
    public RestAction<Void> pinMessageById(String messageId)
    {
        checkPermission(Permission.MESSAGE_READ, "You cannot pin a message in a channel you can't access. (MESSAGE_READ)");
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");

        //Call MessageChannel's default method
        return TextChannel.super.pinMessageById(messageId);
    }

    @Override
    public RestAction<Void> unpinMessageById(String messageId)
    {
        checkPermission(Permission.MESSAGE_READ, "You cannot unpin a message in a channel you can't access. (MESSAGE_READ)");
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");

        //Call MessageChannel's default method
        return TextChannel.super.unpinMessageById(messageId);
    }

    @Override
    public RestAction<List<Message>> getPinnedMessages()
    {
        checkPermission(Permission.MESSAGE_READ, "Cannot get the pinned message of a channel without MESSAGE_READ access.");

        //Call MessageChannel's default method
        return TextChannel.super.getPinnedMessages();
    }

    @Override
    public RestAction<Void> addReactionById(String messageId, String unicode)
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return TextChannel.super.addReactionById(messageId, unicode);
    }

    @Override
    public RestAction<Void> addReactionById(String messageId, Emote emote)
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return TextChannel.super.addReactionById(messageId, emote);
    }

    @Override
    public RestAction<Void> clearReactionsById(String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        checkPermission(Permission.MESSAGE_MANAGE);
        final Route.CompiledRoute route = Route.Messages.REMOVE_ALL_REACTIONS.compile(getId(), messageId);
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Void> removeReactionById(String messageId, String unicode, User user)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.noWhitespace(unicode, "Unicode emoji");
        Checks.notNull(user, "User");
        if (!getJDA().getSelfUser().equals(user))
            checkPermission(Permission.MESSAGE_MANAGE);
        final String code = MiscUtil.encodeUTF8(unicode);
        Route.CompiledRoute route;
        if (user.equals(getJDA().getSelfUser()))
            route = Route.Messages.REMOVE_OWN_REACTION.compile(getId(), messageId, code);
        else
            route = Route.Messages.REMOVE_REACTION.compile(getId(), messageId, code, user.getId());
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (!response.isOk())
                    request.onFailure(response);
                else
                    request.onSuccess(null);
            }
        };
    }

    @Override
    public MessageAction editMessageById(String messageId, CharSequence newContent)
    {
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        return TextChannel.super.editMessageById(messageId, newContent);
    }

    @Override
    public MessageAction editMessageById(String messageId, MessageEmbed newEmbed)
    {
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
        return TextChannel.super.editMessageById(messageId, newEmbed);
    }

    @Override
    public MessageAction editMessageById(String id, Message newContent)
    {
        Checks.notNull(newContent, "Message");

        //checkVerification(); no verification needed to edit a message
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        if (newContent.getContentRaw().isEmpty() && !newContent.getEmbeds().isEmpty())
            checkPermission(Permission.MESSAGE_EMBED_LINKS);

        //Call MessageChannel's default
        return TextChannel.super.editMessageById(id, newContent);
    }

    @Override
    public String toString()
    {
        return "TC:" + getName() + '(' + id + ')';
    }

    @Override
    public int compareTo(TextChannel chan)
    {
        Checks.notNull(chan, "Other TextChannel");
        if (this == chan)
            return 0;
        Checks.check(getGuild().equals(chan.getGuild()), "Cannot compare TextChannels that aren't from the same guild!");
        if (this.getPositionRaw() == chan.getPositionRaw())
            return Long.compare(id, chan.getIdLong());
        return Integer.compare(rawPosition, chan.getPositionRaw());
    }

    // -- Setters --

    public TextChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    public TextChannelImpl setLastMessageId(long id)
    {
        this.lastMessageId = id;
        return this;
    }

    public TextChannelImpl setNSFW(boolean nsfw)
    {
        this.nsfw = nsfw;
        return this;
    }

    // -- internal --

    private void checkVerification()
    {
        if (!guild.checkVerification())
            throw new VerificationLevelException(guild.getVerificationLevel());
    }
}
