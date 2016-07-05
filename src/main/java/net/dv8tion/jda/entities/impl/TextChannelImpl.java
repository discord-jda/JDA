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
import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.exceptions.RateLimitedException;
import net.dv8tion.jda.exceptions.VerificationLevelException;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.PermissionOverrideManager;
import net.dv8tion.jda.requests.Requester;
import net.dv8tion.jda.utils.InviteUtil;
import net.dv8tion.jda.utils.MiscUtil;
import net.dv8tion.jda.utils.PermissionUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.time.OffsetDateTime;
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
    public String getAsMention()
    {
        return "<#" + getId() + '>';
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
        checkVerification();
        SelfInfo self = getJDA().getSelfInfo();
        if (!checkPermission(self, Permission.MESSAGE_WRITE))
            throw new PermissionException(Permission.MESSAGE_WRITE);

        JDAImpl api = (JDAImpl) getJDA();
        if (api.getMessageLimit(guild.getId()) != null)
        {
            throw new RateLimitedException(api.getMessageLimit(guild.getId()) - System.currentTimeMillis());
        }
        try
        {
            Requester.Response response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/messages",
                    new JSONObject().put("content", msg.getRawContent()).put("tts", msg.isTTS()));
            if (response.isRateLimit())
            {
                long retry_after = response.getObject().getLong("retry_after");
                api.setMessageTimeout(guild.getId(), retry_after);
                throw new RateLimitedException(retry_after);
            }
            if(!response.isOk()) //sending failed (Verification-level?)
                return null;
            return new EntityBuilder(api).createMessage(response.getObject());
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
        checkVerification();
        SelfInfo self = getJDA().getSelfInfo();
        if (!checkPermission(self, Permission.MESSAGE_WRITE))
            throw new PermissionException(Permission.MESSAGE_WRITE);

        ((MessageImpl) msg).setChannelId(id);
        AsyncMessageSender.getInstance(getJDA(), guild.getId()).enqueue(msg, false, callback);
    }

    @Override
    public Message sendFile(File file, Message message)
    {
        checkVerification();
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
        checkVerification();
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_WRITE))
            throw new PermissionException(Permission.MESSAGE_WRITE);
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_ATTACH_FILES))
            throw new PermissionException(Permission.MESSAGE_ATTACH_FILES);

        Thread thread = new Thread(() ->
        {
            Message messageReturn = sendFile(file, message);
            if (callback != null)
                callback.accept(messageReturn);
        });
        thread.setName("TextChannelImpl sendFileAsync Channel: " + id);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public Message getMessageById(String messageId)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_READ))
            throw new PermissionException(Permission.MESSAGE_READ);
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_HISTORY))
            throw new PermissionException(Permission.MESSAGE_HISTORY);

        Requester.Response response = ((JDAImpl) getJDA()).getRequester().get(Requester.DISCORD_API_PREFIX + "channels/" + id + "/messages/" + messageId);

        if (response.isOk())
            return new EntityBuilder((JDAImpl) getJDA()).createMessage(response.getObject());

        //Doesn't exist.
        return null;
    }

    @Override
    public boolean deleteMessageById(String messageId)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_READ))
            throw new PermissionException(Permission.MESSAGE_READ);

        Requester.Response response = ((JDAImpl) getJDA()).getRequester().delete(Requester.DISCORD_API_PREFIX + "channels/" + id + "/messages/" + messageId);

        if (response.isOk())
            return true;
        else if (response.code == 403)  //This block is needed because we cant check who owns the message before attempting to delete.
        {
            //We double check to make sure the permission didn't change.
            if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_READ))
                throw new PermissionException(Permission.MESSAGE_READ);
            else
                throw new PermissionException(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE permission to delete another users Messages");
        }

        //Doesn't exist. Either never existed, bad id, was deleted already, or not in this channel.
        return false;
    }

    @Override
    public MessageHistory getHistory()
    {
        return new MessageHistory(this);
    }

    public void sendTyping()
    {
        ((JDAImpl) getJDA()).getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/typing", new JSONObject());
    }

    @Override
    public boolean pinMessageById(String messageId)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_READ))
            throw new PermissionException(Permission.MESSAGE_READ, "You cannot pin a message in a channel you can't access. (MESSAGE_READ)");
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_MANAGE))
            throw new PermissionException(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().put(
                Requester.DISCORD_API_PREFIX + "/channels/" + id + "/pins/" + messageId, new JSONObject());
        if (response.isRateLimit())
            throw new RateLimitedException(response.getObject().getInt("retry_after"));
        return response.isOk();
    }

    @Override
    public boolean unpinMessageById(String messageId)
    {        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_READ))
        throw new PermissionException(Permission.MESSAGE_READ, "You cannot unpin a message in a channel you can't access. (MESSAGE_READ)");
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_MANAGE))
            throw new PermissionException(Permission.MESSAGE_MANAGE, "You need MESSAGE_MANAGE to pin or unpin messages.");
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().delete(
                Requester.DISCORD_API_PREFIX + "/channels/" + id + "/pins/" + messageId);
        if (response.isRateLimit())
            throw new RateLimitedException(response.getObject().getInt("retry_after"));
        return response.isOk();
    }

    @Override
    public List<Message> getPinnedMessages()
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_READ))
            new PermissionException(Permission.MESSAGE_READ, "Cannot get the pinned message of a channel without MESSAGE_READ access.");

        List<Message> pinnedMessages = new LinkedList<>();
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().get(
                Requester.DISCORD_API_PREFIX + "/channels/" + id + "/pins");
        if (response.isOk())
        {
            JSONArray pins = response.getArray();
            for (int i = 0; i < pins.length(); i++)
            {
                pinnedMessages.add(new EntityBuilder((JDAImpl) getJDA()).createMessage(pins.getJSONObject(i)));
            }
            return Collections.unmodifiableList(pinnedMessages);
        }
        else if (response.isRateLimit())
            throw new RateLimitedException(response.getObject().getInt("retry_after"));
        else
            throw new RuntimeException("An unknown error occured attempting to get pinned messages. Ask devs for help.\n" + response);
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
        PermissionOverrideManager manager = override.getManager();
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
        PermissionOverrideManager manager = override.getManager();
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
    public void deleteMessages(Collection<Message> messages)
    {
        deleteMessagesByIds(messages.stream()
                .map(msg -> msg.getId())
                .collect(Collectors.toList()));
    }

    @Override
    public void deleteMessagesByIds(Collection<String> messageIds)
    {
        if (messageIds.size() < 2 || messageIds.size() > 100)
        {
            throw new IllegalArgumentException("Must provide at least 2 or at most 100 messages to be deleted.");
        }
        else if (!PermissionUtil.checkPermission(getJDA().getSelfInfo(), Permission.MESSAGE_MANAGE, this))
        {
            throw new PermissionException(Permission.MESSAGE_MANAGE, "Must have MESSAGE_MANAGE in order to bulk delete messages in this channel regardless of author.");
        }

        JSONObject body = new JSONObject().put("messages", messageIds);
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + id + "/messages/bulk_delete", body);
        if (response.isRateLimit())
            throw new RateLimitedException(response.getObject().getInt("retry_after"));
    }

    private void checkVerification()
    {
        if (!guild.checkVerification())
        {
            throw new VerificationLevelException(guild.getVerificationLevel());
        }
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

        OffsetDateTime thisTime = MiscUtil.getCreationTime(this);
        OffsetDateTime chanTime = MiscUtil.getCreationTime(chan);

        //We compare the provided channel's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a channel was created, the lower its hierarchy ranking when
        // it shares the same position as another channel.
        return chanTime.compareTo(thisTime);
    }

    public static class AsyncMessageSender
    {
        private static final Map<JDA, Map<String, AsyncMessageSender>> instances = new HashMap<>();
        private final JDAImpl api;
        private final String ratelimitIdentifier; //GuildId or GlobalPrivateChannel
        private Runner runner = null;
        private boolean runnerRunning = false;
        private boolean alive = true;
        private final Queue<Task> queue = new LinkedList<>();

        private AsyncMessageSender(JDAImpl api, String ratelimitIdentifier)
        {
            this.api = api;
            this.ratelimitIdentifier = ratelimitIdentifier;
        }

        public static AsyncMessageSender getInstance(JDA api, String ratelimitIdentifier)
        {
            Map<String, AsyncMessageSender> senders = instances.get(api);
            if (senders == null)
            {
                senders = new HashMap<>();
                instances.put(api, senders);
            }

            AsyncMessageSender sender = senders.get(ratelimitIdentifier);
            if (sender == null)
            {
                sender = new AsyncMessageSender(((JDAImpl) api), ratelimitIdentifier);
                senders.put(ratelimitIdentifier, sender);
            }
            return sender;
        }

        public synchronized static void stop(JDA api, String ratelimitIdentifier)
        {
            Map<String, AsyncMessageSender> senders = instances.get(api);
            if (senders != null && !senders.isEmpty())
            {
                AsyncMessageSender sender = senders.get(ratelimitIdentifier);
                if (sender != null)
                {
                    sender.kill();
                    senders.remove(ratelimitIdentifier);
                }
            }
        }

        public synchronized static void stopAll(JDA api)
        {
            Map<String, AsyncMessageSender> senders = instances.get(api);
            if (senders != null && !senders.isEmpty())
            {
                senders.values().forEach(sender ->
                {
                    sender.kill();
                });
                senders.clear();
            }
        }

        public synchronized void enqueue(Message msg, boolean isEdit, Consumer<Message> callback)
        {
            enqueue(new Task(msg, isEdit, callback));
        }

        public synchronized void enqueue(Task task)
        {
            queue.add(task);
            if (runner == null)
            {
                runnerRunning = true;
                runner = new Runner(this);
                runner.setDaemon(true);
                runner.start();
            }
            else if (!runnerRunning)
            {
                runnerRunning = true;
                notifyAll();
            }
        }

        public synchronized void kill()
        {
            alive = false;
            notifyAll();
        }

        private synchronized void waitNew()
        {
            if (!queue.isEmpty())
                return;
            runnerRunning = false;
            while(!runnerRunning) {
                try {
                    wait();
                } catch(InterruptedException ignored) {}
            }
        }

        private synchronized Queue<Task> getQueue()
        {
            Queue<Task> copy = new LinkedList<>(queue);
            queue.clear();
            return copy;
        }

        public static class Task
        {
            public final Message message;
            public final boolean isEdit;
            public final Consumer<Message> callback;

            public Task(Message message, boolean isEdit, Consumer<Message> callback)
            {
                this.message = message;
                this.isEdit = isEdit;
                this.callback = callback;
            }
        }

        private static class Runner extends Thread
        {
            private final AsyncMessageSender sender;

            public Runner(AsyncMessageSender sender)
            {
                this.sender = sender;
                this.setName("AsyncMessageSender Runner. Identifier: " + sender.ratelimitIdentifier);
            }

            @Override
            public void run()
            {
                sending:    //Label so that, if needed, we can completely kill the while loop from inside the nested loop.
                    while (sender.alive)
                    {
                        Queue<Task> queue = sender.getQueue();
                        while (sender.alive && !queue.isEmpty())
                        {
                            Long messageLimit = sender.api.getMessageLimit(sender.ratelimitIdentifier);
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
                            Task task = queue.peek();
                            Message msg = task.message;
                            Requester.Response response;
                            if (sender.api.getTextChannelById(msg.getChannelId()) == null
                                    && sender.api.getPrivateChannelById(msg.getChannelId()) == null)
                            {
                                //We no longer have access to the MessageChannel that this message is queued to
                                // send to. This is most likely because it was deleted.
                                AsyncMessageSender.stop(sender.api, sender.ratelimitIdentifier);
                                break sending;
                            }
                            if(task.isEdit)
                            {
                                response = sender.api.getRequester().patch(Requester.DISCORD_API_PREFIX + "channels/" + msg.getChannelId() + "/messages/" + msg.getId(),
                                        new JSONObject().put("content", msg.getRawContent()));
                            }
                            else
                            {
                                response = sender.api.getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + msg.getChannelId() + "/messages",
                                        new JSONObject().put("content", msg.getRawContent()).put("tts", msg.isTTS()));
                            }
                            if (response.responseText == null)
                            {
                                JDAImpl.LOG.debug("Error sending async-message (returned null-text)... Retrying after 1s");
                                sender.api.setMessageTimeout(sender.ratelimitIdentifier, 1000);
                            }
                            else if (!response.isRateLimit())   //success/unrecoverable error
                            {
                                queue.poll();//remove from queue
                                try
                                {
                                    if (response.isOk())
                                    {
                                        if (task.callback != null)
                                            task.callback.accept(new EntityBuilder(sender.api).createMessage(response.getObject()));
                                    }
                                    else
                                    {
                                        //if response didn't have id, sending failed (due to permission/blocked pm,...
                                        JDAImpl.LOG.fatal("Could not send/update async message to channel: " + msg.getChannelId() + ". Discord-response: " + response.toString());
                                        if (task.callback != null)
                                            task.callback.accept(null);
                                    }
                                }
                                catch (JSONException ex)
                                {
                                    //could not generate message from json
                                    JDAImpl.LOG.log(ex);
                                }
                                catch (IllegalArgumentException ex)
                                {
                                    JDAImpl.LOG.log(ex);
                                }
                            }
                            else
                            {
                                sender.api.setMessageTimeout(sender.ratelimitIdentifier, response.getObject().getLong("retry_after"));
                            }
                            if (queue.isEmpty())
                            {
                                queue = sender.getQueue();
                            }
                        }
                        sender.waitNew();
                    }
            }
        }
    }
}
