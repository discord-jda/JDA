/**
 * Created by Michael Ritter on 15.12.2015.
 */
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.requests.RequestBuilder;
import net.dv8tion.jda.requests.RequestType;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MessageImpl implements Message
{
    private final String id;
    private final JDA api;
    private List<User> mentionedUsers = new LinkedList<>();
    private boolean mentionsEveryone;
    private boolean isTTS;
    private OffsetDateTime time;
    private OffsetDateTime editedTime = null;
    private User author;
    private TextChannel channel;
    private String content;

    public MessageImpl(String id, JDA api)
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
        return content;
    }

    @Override
    public TextChannel getChannel()
    {
        return channel;
    }

    @Override
    public boolean isTTS()
    {
        return isTTS;
    }

    @Override
    public Message updateMessage(String new_content)
    {
        RequestBuilder rb = new RequestBuilder(api);
        rb.setType(RequestType.PATCH);
        rb.setUrl("https://discordapp.com/api/channels/" + channel.getId() + "/messages/" + getId());
        rb.setData(new JSONObject().put("content", new_content).toString());

        try
        {
            return new EntityBuilder(api).createMessage(new JSONObject(rb.makeRequest()));
        }
        catch (JSONException ex)
        {
            return null;
        }
    }

    @Override
    public void deleteMessage()
    {
        RequestBuilder rb = new RequestBuilder(api);
        rb.setType(RequestType.DELETE);
        rb.setGetResponse(false);
        rb.setUrl("https://discordapp.com/api/channels/" + channel.getId() + "/messages/" + getId());
        rb.makeRequest();
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

    public MessageImpl setChannel(TextChannel channel)
    {
        this.channel = channel;
        return this;
    }

    public MessageImpl setContent(String content)
    {
        this.content = content;
        return this;
    }
}
