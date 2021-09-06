package net.dv8tion.jda.api.entities;

import gnu.trove.set.TLongSet;

import java.util.*;

import java.util.stream.Collectors;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EmoteImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Matcher;

public class MessageMentions
{
    private final String content;
    private final boolean mentionsEveryone;
    private final TLongSet userIds;
    private final TLongSet roleIds;
    private final JDAImpl jda;
    private final Guild guild;

    private final List<User> users;
    private final List<Member> members;
    private final List<Role> roles;
    private final List<TextChannel> textChannels;
    private final List<VoiceChannel> voiceChannels;

    private final List<Emote> emotes;
    private final List<String> invites;
    private final List<MessageSticker> stickers;

    public MessageMentions(
            String content, boolean mentionsEveryone, TLongSet userIds, TLongSet roleIds, List<MessageSticker> stickers,
            Guild guild, JDAImpl jda
    ) {
        this.content = content;
        this.mentionsEveryone = mentionsEveryone;
        this.userIds = userIds;
        this.roleIds = roleIds;
        this.jda = jda;
        this.guild = guild;

        this.users = Collections.unmodifiableList(processMentions(Message.MentionType.USER, new ArrayList<>(), true, this::matchUser));

        if (guild == null)
        {
            this.members = new ArrayList<>();
        }
        else
        {
            this.members = Collections.unmodifiableList(
                    getUsers()
                        .stream()
                        .map(guild::getMember)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
            );
        }

        this.roles = Collections.unmodifiableList(processMentions(Message.MentionType.ROLE, new ArrayList<>(), true, this::matchRole));
        this.textChannels = Collections.unmodifiableList(processMentions(Message.MentionType.CHANNEL, new ArrayList<>(), true, it -> matchChannel(it, ChannelType.TEXT, TextChannel.class)));
        this.voiceChannels = Collections.unmodifiableList(processMentions(Message.MentionType.CHANNEL, new ArrayList<>(), true, it -> matchChannel(it, ChannelType.VOICE, VoiceChannel.class)));
        this.emotes = Collections.unmodifiableList(processMentions(Message.MentionType.EMOTE, new ArrayList<>(), true, this::matchEmote));
        this.invites = matchInvites();
        this.stickers = stickers;
    }

    @NotNull
    public List<Member> getMembers()
    {
        return members;
    }

    @NotNull
    public List<User> getUsers()
    {
        return users;
    }

    @NotNull
    public List<TextChannel> getTextChannels()
    {
        return textChannels;
    }

    @NotNull
    public List<VoiceChannel> getVoiceChannels()
    {
        return voiceChannels;
    }

    @NotNull
    public List<Role> getRoles()
    {
        return roles;
    }

    @NotNull
    public List<Emote> getEmotes()
    {
        return emotes;
    }

    @NotNull
    public List<String> getInvites()
    {
        return invites;
    }

    @NotNull
    public RestAction<List<Invite>> resolveInvites()
    {
        return RestAction.allOf(
            getInvites()
                    .stream()
                    .map(it -> Invite.resolve(jda, it))
                    .collect(Collectors.toList())
        );
    }

    @NotNull
    public List<MessageSticker> getStickers()
    {
        return stickers;
    }

    public boolean mentionsEveryone()
    {
        return mentionsEveryone;
    }

    @NotNull
    public JDA getJDA()
    {
        return jda;
    }

    private <T, C extends Collection<T>> C processMentions(Message.MentionType type, C collection, boolean distinct, Function<Matcher, T> map)
    {
        Matcher matcher = type.getPattern().matcher(content);
        while (matcher.find())
        {
            try
            {
                T elem = map.apply(matcher);
                if (elem == null || (distinct && collection.contains(elem)))
                    continue;
                collection.add(elem);
            }
            catch (NumberFormatException ignored) {}
        }
        return collection;
    }

    private User matchUser(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!userIds.contains(userId))
            return null;
        User user = getJDA().getUserById(userId);
        if (user == null)
            user = users.stream().filter(it -> it.getIdLong() == userId).findFirst().orElse(null);
        return user;
    }

    private Role matchRole(Matcher matcher)
    {
        long roleId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!roleIds.contains(roleId))
            return null;
        if (guild != null)
            return guild.getRoleById(roleId);
        else
            return getJDA().getRoleById(roleId);
    }

    private Emote matchEmote(Matcher matcher)
    {
        long emoteId = MiscUtil.parseSnowflake(matcher.group(2));
        String name = matcher.group(1);
        boolean animated = matcher.group(0).startsWith("<a:");
        Emote emote = getJDA().getEmoteById(emoteId);
        if (emote == null)
            emote = new EmoteImpl(emoteId, jda).setName(name).setAnimated(animated);
        return emote;
    }

    private List<String> matchInvites()
    {
        List<String> invites = new ArrayList<>();
        Matcher m = Message.INVITE_PATTERN.matcher(content);
        while (m.find())
            invites.add(m.group(1));
        return invites;
    }

    private <T extends GuildChannel> T matchChannel(Matcher matcher, ChannelType type, Class<T> clazz)
    {
        long channelId = MiscUtil.parseSnowflake(matcher.group(1));

        switch (type)
        {
        case TEXT:
            return clazz.cast(getJDA().getTextChannelById(channelId));

        case VOICE:
            return clazz.cast(getJDA().getVoiceChannelById(channelId));

        default:
            return null;
        }
    }
}
