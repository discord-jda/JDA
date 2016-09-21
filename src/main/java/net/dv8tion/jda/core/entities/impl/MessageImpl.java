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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.handle.EntityBuilder;
import net.dv8tion.jda.core.requests.*;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageImpl implements Message
{
    private final JDAImpl api;
    private final String id;
    private final MessageType type;
    private final MessageChannel channel;
    private final boolean isPrivate;
    private boolean mentionsEveryone = false;
    private boolean isTTS = false;
    private boolean pinned;
    private String content;
    private String subContent = null;
    private String strippedContent = null;
    private User author;
    private OffsetDateTime time;
    private OffsetDateTime editedTime = null;
    private List<User> mentionedUsers = new LinkedList<>();
    private List<TextChannel> mentionedChannels = new LinkedList<>();
    private List<Role> mentionedRoles = new LinkedList<>();
    private List<Attachment> attachments = new LinkedList<>();
    private List<MessageEmbed> embeds = new LinkedList<>();

    public MessageImpl(String id, MessageChannel channel)
    {
        this(id, channel, MessageType.DEFAULT);
    }


    public MessageImpl(String id, MessageChannel channel, MessageType type)
    {
        this.id = id;
        this.channel = channel;
        this.api = (JDAImpl) channel.getJDA();
        this.type = type;
        this.isPrivate = channel instanceof PrivateChannel;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public boolean isPinned()
    {
        return pinned;
    }

    @Override
    public RestAction<Void> pin()
    {
        return channel.pinMessageById(getId());
    }

    @Override
    public RestAction<Void> unpin()
    {
        return channel.unpinMessageById(getId());
    }

    public MessageImpl setPinned(boolean pinned)
    {
        this.pinned = pinned;
        return this;
    }

    @Override
    public MessageType getType()
    {
        return type;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public List<User> getMentionedUsers()
    {
        return Collections.unmodifiableList(mentionedUsers);
    }

    @Override
    public boolean isMentioned(User user)
    {
        return mentionsEveryone() || mentionedUsers.contains(user);
    }

    @Override
    public List<TextChannel> getMentionedChannels()
    {
        return Collections.unmodifiableList(mentionedChannels);
    }

    @Override
    public List<Role> getMentionedRoles()
    {
        return Collections.unmodifiableList(mentionedRoles);
    }

    @Override
    public boolean mentionsEveryone()
    {
        return mentionsEveryone;
    }

    @Override
    public boolean isEdited()
    {
        return editedTime != null;
    }

    @Override
    public OffsetDateTime getEditedTime()
    {
        return editedTime;
    }

    @Override
    public User getAuthor()
    {
        return author;
    }

    @Override
    public String getContent()
    {
        if (subContent == null)
        {
            String tmp = content;
            for (User user : mentionedUsers)
            {
                if (isPrivate)
                {
                    tmp = tmp.replace("<@" + user.getId() + '>', '@' + user.getName())
                            .replace("<@!" + user.getId() + '>', '@' + user.getName());
                }
                else
                {
                    String name = getGuild().getMember(author).getEffectiveName();
                    tmp = tmp.replace("<@" + user.getId() + '>', '@' + name)
                            .replace("<@!" + user.getId() + '>', '@' + name);
                }
            }
            for (TextChannel mentionedChannel : mentionedChannels)
            {
                tmp = tmp.replace("<#" + mentionedChannel.getId() + '>', '#' + mentionedChannel.getName());
            }
            for (Role mentionedRole : mentionedRoles)
            {
                tmp = tmp.replace("<@&" + mentionedRole.getId() + '>', '@' + mentionedRole.getName());
            }
            subContent = tmp;
        }
        return subContent;
    }

    @Override
    public String getRawContent()
    {
        return content;
    }

    @Override
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        return isPrivate ? (PrivateChannel) channel : null;
    }

    @Override
    public TextChannel getTextChannel()
    {
        return !isPrivate ? (TextChannel) channel : null;
    }

    @Override
    public Guild getGuild()
    {
        return !isPrivate ? getTextChannel().getGuild() : null;
    }

    @Override
    public List<Attachment> getAttachments()
    {
        return Collections.unmodifiableList(attachments);
    }

    @Override
    public List<MessageEmbed> getEmbeds()
    {
        return Collections.unmodifiableList(embeds);
    }

    @Override
    public boolean isPrivate()
    {
        return isPrivate;
    }

    @Override
    public boolean isTTS()
    {
        return isTTS;
    }

    @Override
    public RestAction<Message> editMessage(String newContent)
    {
        return editMessage(new MessageBuilder().appendString(newContent).build());
    }

    @Override
    public RestAction<Message> editMessage(Message newContent)
    {
        if (!api.getSelfInfo().equals(getAuthor()))
            throw new UnsupportedOperationException("Attempted to update message that was not sent by this account. You cannot modify other User's messages!");


        JSONObject json = new JSONObject().put("content", newContent.getRawContent()).put("tts", newContent.isTTS());
        Route.CompiledRoute route = Route.Messages.EDIT_MESSAGE.compile(getChannel().getId(), getId());
        return new RestAction<Message>(api, route, json)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    try
                    {
                        Message m = EntityBuilder.get(api).createMessage(response.getObject());
                        request.onSuccess(m);
                    }
                    catch (IllegalArgumentException e)
                    {
                        request.onFailure(e);
                    }
                }
                else
                {
                    request.onFailure(
                            new ErrorResponseException(
                                    ErrorResponse.fromJSON(response.getObject()), response));
                }
            }
        };
    }

    @Override
    public RestAction<Void> deleteMessage()
    {
        if (!getJDA().getSelfInfo().equals(getAuthor()))
        {
            if (isPrivate)
                throw new PermissionException("Cannot delete another User's messages in a PrivateChannel.");
            else if (!getGuild().getMember(getJDA().getSelfInfo())
                    .hasPermission((TextChannel) getChannel(), Permission.MESSAGE_MANAGE))
                throw new PermissionException(Permission.MESSAGE_MANAGE);
        }
        return channel.deleteMessageById(getId());
    }

    public MessageImpl setMentionedUsers(List<User> mentionedUsers)
    {
        this.mentionedUsers = mentionedUsers;
        return this;
    }

    public MessageImpl setMentionedChannels(List<TextChannel> mentionedChannels)
    {
        this.mentionedChannels = mentionedChannels;
        return this;
    }

    public MessageImpl setMentionedRoles(List<Role> mentionedRoles)
    {
        this.mentionedRoles = mentionedRoles;
        return this;
    }

    public MessageImpl setMentionsEveryone(boolean mentionsEveryone)
    {
        this.mentionsEveryone = mentionsEveryone;
        return this;
    }

    public MessageImpl setTTS(boolean TTS)
    {
        isTTS = TTS;
        return this;
    }

    public MessageImpl setTime(OffsetDateTime time)
    {
        this.time = time;
        return this;
    }

    public MessageImpl setEditedTime(OffsetDateTime editedTime)
    {
        this.editedTime = editedTime;
        return this;
    }

    public MessageImpl setAuthor(User author)
    {
        this.author = author;
        return this;
    }

    public MessageImpl setContent(String content)
    {
        this.content = content;
        return this;
    }

    public MessageImpl setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
        return this;
    }

    public MessageImpl setEmbeds(List<MessageEmbed> embeds)
    {
        this.embeds = embeds;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Message))
            return false;
        Message oMsg = (Message) o;
        return this == oMsg || this.getId().equals(oMsg.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        String content = getContent();
        if (content.length() > 20)
        {
            content = content.substring(0, 17) + "...";
        }
        return "M:" + author.getName() + ':' + content + '(' + getId() + ')';
    }

    @Override
    public String getStrippedContent()
    {
        if (strippedContent == null)
        {
            String tmp = getContent();
            //all the formatting keys to keep track of
            String[] keys = new String[] {"*", "_", "`", "~~"};

            //find all tokens (formatting strings described above)
            TreeSet<FormatToken> tokens = new TreeSet<>((t1, t2) -> Integer.compare(t1.start, t2.start));
            for (String key : keys)
            {
                Matcher matcher = Pattern.compile(Pattern.quote(key)).matcher(tmp);
                while (matcher.find())
                {
                    tokens.add(new FormatToken(key, matcher.start()));
                }
            }

            //iterate over all tokens, find all matching pairs, and add them to the list toRemove
            Stack<FormatToken> stack = new Stack<>();
            List<FormatToken> toRemove = new ArrayList<>();
            boolean inBlock = false;
            for (FormatToken token : tokens)
            {
                if (stack.empty() || !stack.peek().format.equals(token.format) || stack.peek().start + token.format.length() == token.start)
                {
                    //we are at opening tag
                    if (!inBlock)
                    {
                        //we are outside of block -> handle normally
                        if (token.format.equals("`"))
                        {
                            //block start... invalidate all previous tags
                            stack.clear();
                            inBlock = true;
                        }
                        stack.push(token);
                    }
                    else if (token.format.equals("`"))
                    {
                        //we are inside of a block -> handle only block tag
                        stack.push(token);
                    }
                }
                else if (!stack.empty())
                {
                    //we found a matching close-tag
                    toRemove.add(stack.pop());
                    toRemove.add(token);
                    if (token.format.equals("`") && stack.empty())
                    {
                        //close tag closed the block
                        inBlock = false;
                    }
                }
            }

            //sort tags to remove by their start-index and iteratively build the remaining string
            Collections.sort(toRemove, (t1, t2) -> Integer.compare(t1.start, t2.start));
            StringBuilder out = new StringBuilder();
            int currIndex = 0;
            for (FormatToken formatToken : toRemove)
            {
                if (currIndex < formatToken.start)
                {
                    out.append(tmp.substring(currIndex, formatToken.start));
                }
                currIndex = formatToken.start + formatToken.format.length();
            }
            if (currIndex < tmp.length())
            {
                out.append(tmp.substring(currIndex));
            }
            //return the stripped text, escape all remaining formatting characters (did not have matching open/close before or were left/right of block
            strippedContent = out.toString().replace("*", "\\*").replace("_", "\\_").replace("~", "\\~");
        }
        return strippedContent;
    }

    private static class FormatToken {
        public final String format;
        public final int start;

        public FormatToken(String format, int start) {
            this.format = format;
            this.start = start;
        }
    }
}
