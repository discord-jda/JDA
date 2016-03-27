/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities.impl;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.exceptions.RateLimitedException;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.PermissionOverrideManager;
import net.dv8tion.jda.requests.Requester;
import net.dv8tion.jda.utils.InviteUtil;
import net.dv8tion.jda.utils.PermissionUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TextChannelImpl implements TextChannel
{
    private final String id;
    private final Guild guild;
    private String name;
    private String topic;
    private int position;
    private final Map<User, PermissionOverride> userPermissionOverrides = new HashMap<>();
    private final Map<Role, PermissionOverride> rolePermissionOverrides = new HashMap<>();

    private ChannelManager manager = null;

    public TextChannelImpl(String id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    @Override
    public PermissionOverride getOverrideForUser(User user)
    {
        return userPermissionOverrides.get(user);
    }

    @Override
    public PermissionOverride getOverrideForRole(Role role)
    {
        return rolePermissionOverrides.get(role);
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides()
    {
        List<PermissionOverride> overrides = new LinkedList<>();
        overrides.addAll(userPermissionOverrides.values());
        overrides.addAll(rolePermissionOverrides.values());
        return Collections.unmodifiableList(overrides);
    }

    @Override
    public List<PermissionOverride> getUserPermissionOverrides()
    {
        return Collections.unmodifiableList(new LinkedList<PermissionOverride>(userPermissionOverrides.values()));
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides()
    {
        return Collections.unmodifiableList(new LinkedList<PermissionOverride>(rolePermissionOverrides.values()));
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
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
    public List<User> getUsers()
    {
        List<User> users = getGuild().getUsers().stream().filter(user -> checkPermission(user, Permission.MESSAGE_READ)).collect(Collectors.toList());
        return Collections.unmodifiableList(users);
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public Message sendMessage(String text)
    {
        return sendMessage(new MessageBuilder().appendString(text).build());
    }

    @Override
    public Message sendMessage(Message msg)
    {
        SelfInfo self = getJDA().getSelfInfo();
        if (!checkPermission(self, Permission.MESSAGE_WRITE))
            throw new PermissionException(Permission.MESSAGE_WRITE);

        JDAImpl api = (JDAImpl) getJDA();
        if (api.getMessageLimit() != null)
        {
            throw new RateLimitedException(api.getMessageLimit() - System.currentTimeMillis());
        }
        try
        {
            JSONObject response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/messages",
                    new JSONObject().put("content", msg.getRawContent()).put("tts", msg.isTTS()));
            if (response.has("retry_after"))
            {
                long retry_after = response.getLong("retry_after");
                api.setMessageTimeout(retry_after);
                throw new RateLimitedException(retry_after);
            }
            if(!response.has("id")) //sending failed (Verification-level?)
                return null;
            return new EntityBuilder(api).createMessage(response);
        }
        catch (JSONException ex)
        {
            JDAImpl.LOG.log(ex);
            //sending failed
            return null;
        }
    }

    @Override
    public void sendMessageAsync(String text, Consumer<Message> callback)
    {
        sendMessageAsync(new MessageBuilder().appendString(text).build(), callback);
    }

    @Override
    public void sendMessageAsync(Message msg, Consumer<Message> callback)
    {
        SelfInfo self = getJDA().getSelfInfo();
        if (!checkPermission(self, Permission.MESSAGE_WRITE))
            throw new PermissionException(Permission.MESSAGE_WRITE);

        ((MessageImpl) msg).setChannelId(getId());
        AsyncMessageSender.getInstance(getJDA()).enqueue(msg, callback);
    }

    @Override
    @Deprecated
    public Message sendFile(File file)
    {
        return sendFile(file, null);
    }

    @Override
    @Deprecated
    public void sendFileAsync(File file, Consumer<Message> callback)
    {
        sendFileAsync(file, null, callback);
    }

    @Override
    public Message sendFile(File file, Message message)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_WRITE))
            throw new PermissionException(Permission.MESSAGE_WRITE);
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_ATTACH_FILES))
            throw new PermissionException(Permission.MESSAGE_ATTACH_FILES);
        if(file == null || !file.exists() || !file.canRead())
            throw new IllegalArgumentException("Provided file is either null, doesn't exist or is not readable!");
        if (file.length() > 8<<20)   //8MB
            throw new IllegalArgumentException("File is to big! Max file-size is 8MB");

        JDAImpl api = (JDAImpl) getJDA();
        try
        {
            MultipartBody body = Unirest.post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/messages")
                    .header("authorization", getJDA().getAuthToken())
                    .header("user-agent", Requester.USER_AGENT)
                    .field("file", file);
            if (message != null)
                body.field("content", message.getRawContent()).field("tts", message.isTTS());

            String dbg = String.format("Requesting %s -> %s\n\tPayload: file: %s, message: %s, tts: %s\n\tResponse: ",
                    body.getHttpRequest().getHttpMethod().name(), body.getHttpRequest().getUrl(),
                    file.getAbsolutePath(), message == null ? "null" : message.getRawContent(), message == null ? "N/A" : message.isTTS());
            String requestBody = body.asString().getBody();
            Requester.LOG.trace(dbg + body);

            try
            {
                JSONObject messageJson = new JSONObject(requestBody);
                return new EntityBuilder(api).createMessage(messageJson);
            }
            catch (JSONException e)
            {
                Requester.LOG.fatal("Following json caused an exception: " + requestBody);
                Requester.LOG.log(e);
            }
        }
        catch (UnirestException e)
        {
            Requester.LOG.log(e);
        }
        return null;
    }

    @Override
    public void sendFileAsync(File file, Message message, Consumer<Message> callback)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_WRITE))
            throw new PermissionException(Permission.MESSAGE_WRITE);
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_ATTACH_FILES))
            throw new PermissionException(Permission.MESSAGE_ATTACH_FILES);

        Thread thread = new Thread(() ->
        {
            Message messageReturn = sendFile(file, message);
            if (callback != null)
                callback.accept(message);
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendTyping()
    {
        ((JDAImpl) getJDA()).getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/typing", new JSONObject());
    }

    @Override
    public boolean checkPermission(User user, Permission perm)
    {
        return PermissionUtil.checkPermission(user, perm, this);
    }

    @Override
    public synchronized ChannelManager getManager()
    {
        if (manager == null)
            manager = new ChannelManager(this);
        return manager;
    }

    @Override
    public PermissionOverrideManager createPermissionOverride(User user)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MANAGE_PERMISSIONS))
        {
            throw new PermissionException(Permission.MANAGE_PERMISSIONS);
        }
        if (!getGuild().getUsers().contains(user))
        {
            throw new IllegalArgumentException("Given user is not member of this Guild");
        }
        PermissionOverrideImpl override = new PermissionOverrideImpl(this, user, null);
        //hacky way of putting entity to server without using requester here
        override.setAllow(1 << Permission.MANAGE_PERMISSIONS.getOffset()).setDeny(0);
        PermissionOverrideManager manager = new PermissionOverrideManager(override);
        manager.reset(Permission.MANAGE_PERMISSIONS).update();
        return manager;
    }

    @Override
    public PermissionOverrideManager createPermissionOverride(Role role)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MANAGE_PERMISSIONS))
        {
            throw new PermissionException(Permission.MANAGE_PERMISSIONS);
        }
        if (!getGuild().getRoles().contains(role))
        {
            throw new IllegalArgumentException("Given role does not exist in this Guild");
        }
        PermissionOverrideImpl override = new PermissionOverrideImpl(this, null, role);
        //hacky way of putting entity to server without using requester here
        override.setAllow(1 << Permission.MANAGE_PERMISSIONS.getOffset()).setDeny(0);
        PermissionOverrideManager manager = new PermissionOverrideManager(override);
        manager.reset(Permission.MANAGE_PERMISSIONS).update();
        return manager;
    }

    @Override
    public List<InviteUtil.AdvancedInvite> getInvites()
    {
        return InviteUtil.getInvites(this);
    }


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

    public TextChannelImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    public Map<User, PermissionOverride> getUserPermissionOverridesMap()
    {
        return userPermissionOverrides;
    }

    public Map<Role, PermissionOverride> getRolePermissionOverridesMap()
    {
        return rolePermissionOverrides;
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

    public static class AsyncMessageSender
    {
        private static final Map<JDA, AsyncMessageSender> instances = new HashMap<>();
        private final JDAImpl api;
        private Runner runner = null;

        private AsyncMessageSender(JDAImpl api)
        {
            this.api = api;
        }

        public static AsyncMessageSender getInstance(JDA api)
        {
            if (!instances.containsKey(api))
            {
                instances.put(api, new AsyncMessageSender(((JDAImpl) api)));
            }
            return instances.get(api);
        }

        private final Queue<AbstractMap.SimpleImmutableEntry<Message, Consumer<Message>>> queue = new LinkedList<>();

        public synchronized void enqueue(Message msg, Consumer<Message> callback)
        {
            queue.add(new AbstractMap.SimpleImmutableEntry<>(msg, callback));
            if (runner == null || !runner.isAlive())
            {
                runner = new Runner(this);
                runner.start();
            }
        }

        private synchronized Queue<AbstractMap.SimpleImmutableEntry<Message, Consumer<Message>>> getQueue()
        {
            LinkedList<AbstractMap.SimpleImmutableEntry<Message, Consumer<Message>>> copy = new LinkedList<>(queue);
            queue.clear();
            return copy;
        }

        private static class Runner extends Thread
        {
            private final AsyncMessageSender sender;

            public Runner(AsyncMessageSender sender)
            {
                this.sender = sender;
            }

            @Override
            public void run()
            {
                Queue<AbstractMap.SimpleImmutableEntry<Message, Consumer<Message>>> queue = sender.getQueue();
                while (!queue.isEmpty())
                {
                    Long messageLimit = sender.api.getMessageLimit();
                    if (messageLimit != null)
                    {
                        try
                        {
                            Thread.sleep(messageLimit - System.currentTimeMillis());
                        }
                        catch (InterruptedException e)
                        {
                            JDAImpl.LOG.log(e);
                        }
                    }
                    AbstractMap.SimpleImmutableEntry<Message, Consumer<Message>> peek = queue.peek();
                    JSONObject response = sender.api.getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + peek.getKey().getChannelId() + "/messages",
                            new JSONObject().put("content", peek.getKey().getRawContent()).put("tts", peek.getKey().isTTS()));
                    if (response == null)
                    {
                        JDAImpl.LOG.debug("Error sending async-message (returned null-json)... Retrying after 1s");
                        sender.api.setMessageTimeout(1000);
                    }
                    else if (!response.has("retry_after"))   //success
                    {
                        queue.poll();//remove from queue
                        if (peek.getValue() != null)
                        {
                            try
                            {
                                //if response didn't have id, sending failed (due to permission/blocked pm,...
                                peek.getValue().accept(
                                        response.has("id") ? new EntityBuilder(sender.api).createMessage(response) : null);
                            }
                            catch (JSONException ex)
                            {
                                //could not generate message from json
                                JDAImpl.LOG.log(ex);
                            }
                        }
                    }
                    else
                    {
                        sender.api.setMessageTimeout(response.getLong("retry_after"));
                    }
                    if (queue.isEmpty())
                    {
                        queue = sender.getQueue();
                    }
                }
            }

        }
    }
}
