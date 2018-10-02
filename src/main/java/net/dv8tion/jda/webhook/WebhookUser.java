package net.dv8tion.jda.webhook;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.UserImpl.DefaultAvatar;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.List;

/**
 * Represents a user from a webhook, this can be a user that was mentioned or the webhook user itself
 */
public class WebhookUser implements User
{
    protected final long id;

    protected final short discriminator;
    protected final String name;
    protected final String avatarId;
    protected final boolean bot;

    protected WebhookUser(long id, short discriminator, String name, String avatarId, boolean bot)
    {
        this.id = id;
        this.discriminator = discriminator;
        this.name = name;
        this.avatarId = avatarId;
        this.bot = bot;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDiscriminator()
    {
        return String.format("%04d", discriminator);
    }

    @Override
    public String getAvatarId()
    {
        return avatarId;
    }

    @Override
    public String getAvatarUrl()
    {
        return getAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + getId() + "/" + getAvatarId()
            + (getAvatarId().startsWith("a_") ? ".gif" : ".png");
    }

    @Override
    public String getDefaultAvatarId()
    {
        return DefaultAvatar.values()[Integer.parseInt(getDiscriminator()) % DefaultAvatar.values().length].toString();
    }

    @Override
    public String getDefaultAvatarUrl()
    {
        return "https://discordapp.com/assets/" + getDefaultAvatarId() + ".png";
    }

    @Override
    public String getEffectiveAvatarUrl()
    {
        return getAvatarUrl() == null ? getDefaultAvatarUrl() : getAvatarUrl();
    }

    @Override
    public boolean hasPrivateChannel()
    {
        return false;
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel()
    {
        unsupported();
        return null;
    }

    @Override
    public List<Guild> getMutualGuilds()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isBot()
    {
        return bot;
    }

    @Override
    public JDA getJDA()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isFake()
    {
        return true;
    }

    @Override
    public String getAsMention()
    {
        return "<@" + id + '>';
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof WebhookUser))
            return false;
        WebhookUser oUser = (WebhookUser) o;
        return this == oUser || this.id == oUser.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "WU:" + getName() + '(' + id + ')';
    }

    private void unsupported()
    {
        throw new UnsupportedOperationException("This operation is not supported for webhook users!");
    }
}
