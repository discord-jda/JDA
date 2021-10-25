package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionImpl implements Interaction
{
    protected final long id;
    protected final int type;
    protected final String token;
    protected final Guild guild;
    protected final Member member;
    protected final User user;
    protected final JDAImpl api;
    protected final InteractionHookImpl hook;

    public InteractionImpl(JDAImpl jda, DataObject data) {
        this.api = jda;
        this.id = data.getUnsignedLong("id");
        this.token = data.getString("token");
        this.type = data.getInt("type");
        this.guild = jda.getGuildById(data.getUnsignedLong("guild_id", 0L));
        this.hook = new InteractionHookImpl(this, jda);
        if (guild != null)
        {
            member = jda.getEntityBuilder().createMember((GuildImpl) guild, data.getObject("member"));
            jda.getEntityBuilder().updateMemberCache((MemberImpl) member);
            user = member.getUser();
        }
        else
        {
            member = null;
            long channelId = data.getUnsignedLong("channel_id");
            PrivateChannel channel = jda.getPrivateChannelById(channelId);
            if (channel == null)
            {
                channel = jda.getEntityBuilder().createPrivateChannel(
                        DataObject.empty()
                                .put("id", channelId)
                                .put("recipient", data.getObject("user"))
                );
            }
            user = channel.getUser();
        }
    }

    public InteractionImpl(long id, int type, String token, Guild guild, Member member, User user)
    {
        this.id = id;
        this.type = type;
        this.token = token;
        this.guild = guild;
        this.member = member;
        this.user = user;
        this.api = (JDAImpl) user.getJDA();
        this.hook = new InteractionHookImpl(this, api);
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public int getTypeRaw()
    {
        return type;
    }

    @Nonnull
    @Override
    public String getToken()
    {
        return token;
    }

    @Nullable
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        return user;
    }

    @Nullable
    @Override
    public Member getMember()
    {
        return member;
    }

    @NotNull
    @Override
    public InteractionHook getHook()
    {
        return this.hook;
    }

    @Override
    public boolean isAcknowledged()
    {
        return this.hook.isAck();
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }
}
