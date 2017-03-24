/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TextChannelImpl implements TextChannel
{
    private final String id;
    private final GuildImpl guild;
    private final HashMap<Member, PermissionOverride> memberOverrides = new HashMap<>();
    private final HashMap<Role, PermissionOverride> roleOverrides = new HashMap<>();

    private volatile ChannelManager manager;
    private volatile ChannelManagerUpdatable managerUpdatable;
    private final Object mngLock = new Object();

    private String name;
    private String topic;
    private String lastMessageId;
    private int rawPosition;

    public TextChannelImpl(String id, Guild guild)
    {
        this.id = id;
        this.guild = (GuildImpl) guild;
    }

    @Override
    public String getAsMention()
    {
        return "<#" + getId() + '>';
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public RestAction<Void> deleteMessages(Collection<Message> messages)
    {
        Args.notEmpty(messages, "Messages collection");

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
            Args.notEmpty(id, "Message id in messageIds");
            Args.check(Long.parseLong(id) > twoWeeksAgo, "Message Id provided was older than 2 weeks. Id: " + id);
        }

        JSONObject body = new JSONObject().put("messages", messageIds);
        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGES.compile(id);
        return new RestAction<Void>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks()
    {
        checkPermission(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Channels.GET_WEBHOOKS.compile(id);
        return new RestAction<List<Webhook>>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                List<Webhook> webhooks = new LinkedList<>();
                JSONArray array = response.getArray();
                EntityBuilder builder = EntityBuilder.get(getJDA());

                for (Object object : array)
                {
                    try
                    {
                        webhooks.add(builder.createWebhook((JSONObject) object));
                    }
                    catch (JSONException | NullPointerException e)
                    {
                        JDAImpl.LOG.log(e);
                    }
                }

                request.onSuccess(webhooks);
            }
        };
    }

    @Override
    public RestAction<Void> deleteWebhookById(String id)
    {
        Args.notEmpty(id, "webhook id");

        if (!guild.getSelfMember().hasPermission(this, Permission.MANAGE_WEBHOOKS))
            throw new PermissionException(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Webhooks.DELETE_WEBHOOK.compile(id);
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
    public String getLatestMessageId()
    {
        String messageId = lastMessageId;
        if (messageId == null)
            throw new IllegalStateException("No last message id found.");
        return messageId;
    }

    @Override
    public boolean hasLatestMessage()
    {
        return lastMessageId != null;
    }

    @Override
    public String getName()
    {
        return name;
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
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(guild.getMembersMap().values().stream()
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
        throw new RuntimeException("Somehow when determining position we never found the TextChannel in the Guild's channels? wtf?");
    }

    @Override
    public int getPositionRaw()
    {
        return rawPosition;
    }

    @Override
    public JDA getJDA()
    {
        return guild.getJDA();
    }
    
    @Override
    public RestAction<Message> sendMessage(Message msg)
    {
        Args.notNull(msg, "Message");

        checkVerification();
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        if (msg.getRawContent().isEmpty() && !msg.getEmbeds().isEmpty())
            checkPermission(Permission.MESSAGE_EMBED_LINKS);

        //Call MessageChannel's default
        return TextChannel.super.sendMessage(msg);
    }

    @Override
    public RestAction<Message> sendFile(InputStream data, String fileName, Message message)
    {
        checkVerification();
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);

        //Call MessageChannel's default method
        return TextChannel.super.sendFile(data, fileName, message);
    }

    @Override
    public RestAction<Message> sendFile(byte[] data, String fileName, Message message)
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
    public RestAction<Void> deleteMessageById(String messageId)
    {
        Args.notEmpty(messageId, "messageId");
        checkPermission(Permission.MESSAGE_READ);

        //Call MessageChannel's default method
        return TextChannel.super.deleteMessageById(messageId);
    }

    @Override
    public RestAction<MessageHistory> getHistoryAround(String messageId, int limit)
    {
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return TextChannel.super.getHistoryAround(messageId, limit);
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
        checkPermission(Permission.MESSAGE_ADD_REACTION);
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return TextChannel.super.addReactionById(messageId, unicode);
    }

    @Override
    public RestAction<Void> addReactionById(String messageId, Emote emote)
    {
        checkPermission(Permission.MESSAGE_ADD_REACTION);
        checkPermission(Permission.MESSAGE_HISTORY);

        //Call MessageChannel's default method
        return TextChannel.super.addReactionById(messageId, emote);
    }

    @Override
    public PermissionOverride getPermissionOverride(Member member)
    {
        return memberOverrides.get(member);
    }

    @Override
    public PermissionOverride getPermissionOverride(Role role)
    {
        return roleOverrides.get(role);
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides()
    {
        List<PermissionOverride> overrides = new ArrayList<>(memberOverrides.size() + roleOverrides.size());
        overrides.addAll(memberOverrides.values());
        overrides.addAll(roleOverrides.values());
        return Collections.unmodifiableList(overrides);
    }

    @Override
    public List<PermissionOverride> getMemberPermissionOverrides()
    {
        return Collections.unmodifiableList(new ArrayList<>(memberOverrides.values()));
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides()
    {
        return Collections.unmodifiableList(new ArrayList<>(roleOverrides.values()));
    }

    @Override
    public ChannelManager getManager()
    {
        ChannelManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new ChannelManager(this);
            }
        }
        return mng;
    }

    @Override
    public ChannelManagerUpdatable getManagerUpdatable()
    {
        ChannelManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new ChannelManagerUpdatable(this);
            }
        }
        return mng;
    }

    @Override
    public RestAction<Void> delete()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(id);
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Message> editMessageById(String id, Message newContent)
    {
        Args.notNull(newContent, "Message");

        //checkVerification(); no verification needed to edit a message
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_WRITE);
        if (newContent.getRawContent().isEmpty() && !newContent.getEmbeds().isEmpty())
            checkPermission(Permission.MESSAGE_EMBED_LINKS);

        //Call MessageChannel's default
        return TextChannel.super.editMessageById(id, newContent);
    }

    @Override
    public PermissionOverrideAction createPermissionOverride(Member member)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Args.notNull(member, "member");
        if (!guild.equals(member.getGuild()))
            throw new IllegalArgumentException("Provided member is not from the same guild as this channel!");
        if (getMemberOverrideMap().containsKey(member))
            throw new IllegalStateException("Provided member already has a PermissionOverride in this channel!");

        Route.CompiledRoute route = Route.Channels.CREATE_PERM_OVERRIDE.compile(id, member.getUser().getId());
        return new PermissionOverrideAction(getJDA(), route, this, member);
    }

    @Override
    public PermissionOverrideAction createPermissionOverride(Role role)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Args.notNull(role, "role");
        if (!guild.equals(role.getGuild()))
            throw new IllegalArgumentException("Provided role is not from the same guild as this channel!");
        if (getRoleOverrideMap().containsKey(role))
            throw new IllegalStateException("Provided role already has a PermissionOverride in this channel!");

        Route.CompiledRoute route = Route.Channels.CREATE_PERM_OVERRIDE.compile(id, role.getId());
        return new PermissionOverrideAction(getJDA(), route, this, role);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof TextChannel))
            return false;
        TextChannel oTChannel = (TextChannel) o;
        return this == oTChannel || this.getId().equals(oTChannel.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        return "TC:" + getName() + '(' + getId() + ')';
    }

    @Override
    public int compareTo(TextChannel chan)
    {
        if (this == chan)
            return 0;

        if (this.getGuild() != chan.getGuild())
            throw new IllegalArgumentException("Cannot compare TextChannels that aren't from the same guild!");

        if (this.getPositionRaw() != chan.getPositionRaw())
            return chan.getPositionRaw() - this.getPositionRaw();

        OffsetDateTime thisTime = this.getCreationTime();
        OffsetDateTime chanTime = chan.getCreationTime();

        //We compare the provided channel's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a channel was created, the lower its hierarchy ranking when
        // it shares the same position as another channel.
        return chanTime.compareTo(thisTime);
    }

    // -- Setters --

    public TextChannelImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public TextChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    public TextChannelImpl setRawPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
        return this;
    }

    public TextChannelImpl setLastMessageId(String id)
    {
        this.lastMessageId = id;
        return this;
    }

    // -- Map Getters --

    public HashMap<Member, PermissionOverride> getMemberOverrideMap()
    {
        return memberOverrides;
    }

    public HashMap<Role, PermissionOverride> getRoleOverrideMap()
    {
        return roleOverrides;
    }

    // -- internal --

    private void checkVerification()
    {
        if (!guild.checkVerification())
            throw new VerificationLevelException(guild.getVerificationLevel());
    }

    private void checkPermission(Permission permission) {checkPermission(permission, null);}
    private void checkPermission(Permission permission, String message)
    {
        if (!guild.getSelfMember().hasPermission(this, permission))
        {
            if (message != null)
                throw new PermissionException(permission, message);
            else
                throw new PermissionException(permission);
        }
    }

    @Override
    public RestAction<List<Invite>> getInvites()
    {
        if (!this.guild.getSelfMember().hasPermission(this, Permission.MANAGE_CHANNEL))
            throw new PermissionException(Permission.MANAGE_CHANNEL);

        final Route.CompiledRoute route = Route.Invites.GET_CHANNEL_INVITES.compile(getId());

        return new RestAction<List<Invite>>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                {
                    EntityBuilder entityBuilder = EntityBuilder.get(this.api);
                    JSONArray array = response.getArray();
                    List<Invite> invites = new ArrayList<>(array.length());
                    for (int i = 0; i < array.length(); i++)
                    {
                        invites.add(entityBuilder.createInvite(array.getJSONObject(i)));
                    }
                    request.onSuccess(invites);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public InviteAction createInvite()
    {
        if (!this.guild.getSelfMember().hasPermission(this, Permission.CREATE_INSTANT_INVITE))
            throw new PermissionException(Permission.CREATE_INSTANT_INVITE);

        return new InviteAction(this.getJDA(), this.getId());
    }
}
