/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.MultipartBody;
import net.dv8tion.jda.client.exceptions.VerificationLevelException;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.requests.*;
import net.dv8tion.jda.core.utils.IOUtil;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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
        return Collections.unmodifiableList(
        ((GuildImpl) getGuild()).getMembersMap().values().stream()
                .filter(m -> m.getPermissions(this).contains(Permission.MESSAGE_READ))
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
    public RestAction<Message> sendMessage(String text)
    {
        return sendMessage(new MessageBuilder().append(text).build());
    }

    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed)
    {
        return sendMessage(new MessageBuilder().setEmbed(embed).build());
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

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(getId());
        JSONObject json = ((MessageImpl) msg).toJSONObject();
        return new RestAction<Message>(getJDA(), route, json)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    Message m = EntityBuilder.get(getJDA()).createMessage(response.getObject());
                    request.onSuccess(m);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<Message> sendFile(File file, Message message) throws IOException
    {
        checkNull(file, "file");

        return sendFile(file, file.getName(), message);
    }

    @Override
    public RestAction<Message> sendFile(File file, String fileName, Message message) throws IOException
    {
        checkNull(file, "file");

        if(file == null || !file.exists() || !file.canRead())
            throw new IllegalArgumentException("Provided file is either null, doesn't exist or is not readable!");
        if (file.length() > 8<<20)   //8MB
            throw new IllegalArgumentException("File is to big! Max file-size is 8MB");

        return sendFile(IOUtil.readFully(file), fileName, message);
    }

    @Override
    public RestAction<Message> sendFile(InputStream data, String fileName, Message message)
    {
        checkVerification();
        checkPermission(Permission.MESSAGE_WRITE);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);
        checkNull(data, "data InputStream");
        checkNull(fileName, "fileName");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(id);
        MultipartBody body = Unirest.post(Requester.DISCORD_API_PREFIX + route.getCompiledRoute())
                .fields(null); //We use this to change from an HttpRequest to a MultipartBody

        body.field("file", data, fileName);

        if (message != null)
        {
            body.field("content", message.getRawContent());
            body.field("tts", message.isTTS());
        }

        return new RestAction<Message>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(EntityBuilder.get(api).createMessage(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Message> sendFile(byte[] data, String fileName, Message message)
    {
        checkVerification();
        checkPermission(Permission.MESSAGE_WRITE);
        checkPermission(Permission.MESSAGE_ATTACH_FILES);
        checkNull(fileName, "fileName");

        if (data.length > 8<<20)   //8MB
            throw new IllegalArgumentException("Provided data is too large! Max file-size is 8MB");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(id);
        MultipartBody body = Unirest.post(Requester.DISCORD_API_PREFIX + route.getCompiledRoute())
                .fields(null); //We use this to change from an HttpRequest to a MultipartBody

        body.field("file", data, fileName);

        if (message != null)
        {
            body.field("content", message.getRawContent());
            body.field("tts", message.isTTS());
        }

        return new RestAction<Message>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(EntityBuilder.get(api).createMessage(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Message> getMessageById(String messageId)
    {
        if (getJDA().getAccountType() != AccountType.BOT)
            throw new AccountTypeException(AccountType.BOT);
        checkNull(messageId, "messageId");
        checkPermission(Permission.MESSAGE_READ);
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getId(), messageId);
        return new RestAction<Message>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    Message m = EntityBuilder.get(getJDA()).createMessage(response.getObject());
                    request.onSuccess(m);
                }
                else
                    request.onFailure(response);

            }
        };
    }

    @Override
    public RestAction<Void> deleteMessageById(String messageId)
    {
        checkNull(messageId, "messageId");
        checkPermission(Permission.MESSAGE_READ);

        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGE.compile(getId(), messageId);
        return new RestAction<Void>(getJDA(), route, null) {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                {
                    ErrorResponse error = ErrorResponse.fromJSON(response.getObject());
                    if (error == ErrorResponse.MISSING_PERMISSIONS)
                    {
                        //Double check to make sure we still have permission to read.
                        if (!guild.getSelfMember().hasPermission(Permission.MESSAGE_READ))
                            request.onFailure(new PermissionException(Permission.MESSAGE_READ));
                        else
                            request.onFailure(new PermissionException(Permission.MESSAGE_MANAGE,
                                    "You need MESSAGE_MANAGE permission to delete another users Messages"));
                    }
                    else
                    {
                        request.onFailure(response);
                    }
                }
            }
        };
    }

    @Override
    public MessageHistory getHistory()
    {
        return new MessageHistory(this);
    }

    @Override
    public RestAction<MessageHistory> getHistoryAround(Message markerMessage, int limit)
    {
        return MessageHistory.getHistoryAround(this, markerMessage, limit);
    }

    @Override
    public RestAction<MessageHistory> getHistoryAround(String markedMessageId, int limit)
    {
        return MessageHistory.getHistoryAround(this, markedMessageId, limit);
    }

    @Override
    public RestAction<Void> sendTyping()
    {
        Route.CompiledRoute route = Route.Channels.SEND_TYPING.compile(id);
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
    public RestAction<Void> pinMessageById(String messageId)
    {
        checkNull(messageId, "messageId");
        checkPermission(Permission.MESSAGE_READ, "You cannot pin a message in a channel you can't access. (MESSAGE_READ)");
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");

        Route.CompiledRoute route = Route.Messages.ADD_PINNED_MESSAGE.compile(getId(), messageId);
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
    public RestAction<Void> unpinMessageById(String messageId)
    {
        checkNull(messageId, "messageId");
        checkPermission(Permission.MESSAGE_READ, "You cannot unpin a message in a channel you can't access. (MESSAGE_READ)");
        checkPermission(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");

        Route.CompiledRoute route = Route.Messages.REMOVE_PINNED_MESSAGE.compile(getId(), messageId);
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
    public RestAction<List<Message>> getPinnedMessages()
    {
        checkPermission(Permission.MESSAGE_READ, "Cannot get the pinned message of a channel without MESSAGE_READ access.");

        Route.CompiledRoute route = Route.Messages.GET_PINNED_MESSAGES.compile(getId());
        return new RestAction<List<Message>>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    LinkedList<Message> pinnedMessages = new LinkedList<>();
                    EntityBuilder builder = EntityBuilder.get(getJDA());
                    JSONArray pins = response.getArray();

                    for (int i = 0; i < pins.length(); i++)
                    {
                        pinnedMessages.add(builder.createMessage(pins.getJSONObject(i)));
                    }

                    request.onSuccess(Collections.unmodifiableList(pinnedMessages));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
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
    public RestAction<PermissionOverride> createPermissionOverride(Member member)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Args.notNull(member, "member");
        if (!guild.equals(member.getGuild()))
            throw new IllegalArgumentException("Provided member is not from the same guild as this channel!");
        if (getMemberOverrideMap().containsKey(member))
            throw new IllegalStateException("Provided member already has a PermissionOverride in this channel!");

        final PermissionOverride override = new PermissionOverrideImpl(this, member, null);

        JSONObject body = new JSONObject()
                .put("id", member.getUser().getId())
                .put("type", "member")
                .put("allow", 0)
                .put("deny", 0);

        Route.CompiledRoute route = Route.Channels.CREATE_PERM_OVERRIDE.compile(id, member.getUser().getId());
        return new RestAction<PermissionOverride>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                getMemberOverrideMap().put(member, override);
                request.onSuccess(override);
            }
        };
    }

    @Override
    public RestAction<PermissionOverride> createPermissionOverride(Role role)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Args.notNull(role, "role");
        if (!guild.equals(role.getGuild()))
            throw new IllegalArgumentException("Provided role is not from the same guild as this channel!");
        if (getRoleOverrideMap().containsKey(role))
            throw new IllegalStateException("Provided role already has a PermissionOverride in this channel!");

        final PermissionOverride override = new PermissionOverrideImpl(this, null, role);

        JSONObject body = new JSONObject()
                .put("id", role.getId())
                .put("type", "role")
                .put("allow", 0)
                .put("deny", 0);

        Route.CompiledRoute route = Route.Channels.CREATE_PERM_OVERRIDE.compile(id, role.getId());
        return new RestAction<PermissionOverride>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                getRoleOverrideMap().put(role, override);
                request.onSuccess(override);
            }
        };
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

    private void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
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
}
