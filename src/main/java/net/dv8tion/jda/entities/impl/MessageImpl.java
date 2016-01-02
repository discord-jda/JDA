/**
 * Created by Michael Ritter on 15.12.2015.
 */
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.handle.EntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MessageImpl implements Message
{
    private final String id;
    private final JDAImpl api;
    private List<User> mentionedUsers = new LinkedList<>();
    private boolean mentionsEveryone = false;
    private boolean isTTS = false;
    private OffsetDateTime time;
    private OffsetDateTime editedTime = null;
    private User author;
    private String channelId;
    private boolean isPrivate;
    private String content;
    private String subContent = null;

    public MessageImpl(String id, JDAImpl api)
    {
        this.id = id;
        this.api = api;
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
    public boolean mentionsEveryone()
    {
        return mentionsEveryone;
    }

    @Override
    public OffsetDateTime getTime()
    {
        return time.plusSeconds(0L);
    }

    @Override
    public boolean isEdited()
    {
        return editedTime != null;
    }

    @Override
    public OffsetDateTime getEditedTimestamp()
    {
        return editedTime.plusSeconds(0L);
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
                tmp = tmp.replace("<@" + user.getId() + ">", "@" + user.getUsername());
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
    public String getChannelId()
    {
        return channelId;
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
    public Message updateMessage(String newContent)
    {
        try
        {
            JSONObject response = api.getRequester().patch("https://discordapp.com/api/channels/" + channelId + "/messages/" + getId(), new JSONObject().put("content", newContent));
            return new EntityBuilder(api).createMessage(response);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteMessage()
    {
        api.getRequester().delete("https://discordapp.com/api/channels/" + channelId + "/messages/" + getId());
    }

    public MessageImpl setMentionedUsers(List<User> mentionedUsers)
    {
        this.mentionedUsers = mentionedUsers;
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

    public MessageImpl setIsPrivate(boolean isPrivate)
    {
        this.isPrivate = isPrivate;
        return this;
    }

    public MessageImpl setChannelId(String channelId)
    {
        this.channelId = channelId;
        return this;
    }

    public MessageImpl setContent(String content)
    {
        this.content = content;
        return this;
    }

    public void acknowledge()
    {
        api.getRequester().post("https://discordapp.com/api/channels/"+channelId+"/messages/"+id+"/ack", new JSONObject());
    }
}
