package net.dv8tion.jda.api.entities;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class MessageMentions
{
    @NotNull
    public List<Member> getMembers()
    {
        return Collections.emptyList();
    }

    @NotNull
    public List<TextChannel> getTextChannels()
    {
        return Collections.emptyList();
    }

    @NotNull
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.emptyList();
    }

    @NotNull
    public List<Role> getRoles()
    {
        return Collections.emptyList();
    }

    @NotNull
    public List<Emote> getEmotes()
    {
        return Collections.emptyList();
    }

    @NotNull
    public List<Invite> getInvites()
    {
        return Collections.emptyList();
    }

    @NotNull
    public List<MessageSticker> getStickers()
    {
        return Collections.emptyList();
    }

    public boolean mentionsEveryone()
    {
        return false;
    }
}
