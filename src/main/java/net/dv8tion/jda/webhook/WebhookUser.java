/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
 *
 * <h2>Important notice:</h2>
 * Any JDA related methods such as {@link net.dv8tion.jda.core.entities.User#getMutualGuilds() #getMutualGuilds()} and
 * {@link net.dv8tion.jda.core.entities.User#openPrivateChannel() #openPrivateChannel()} are not available from this class
 * and will throw an {@link java.lang.UnsupportedOperationException UnsupportedOperationException}.
 */
public class WebhookUser implements User
{
    protected final long id;

    protected final short discriminator;
    protected final String name;
    protected final String avatarId;
    protected final boolean bot;

    public WebhookUser(long id, short discriminator, String name, String avatarId, boolean bot)
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
        return "<@" + getId() + '>';
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof WebhookUser))
            return false;
        WebhookUser oUser = (WebhookUser) o;
        return this.id == oUser.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "WU:" + getName() + '(' + getId() + ')';
    }

    private void unsupported()
    {
        throw new UnsupportedOperationException("This operation is not supported for webhook users!");
    }
}
