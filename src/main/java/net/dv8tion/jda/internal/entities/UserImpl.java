/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.DeferredRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.EnumSet;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

public class UserImpl extends UserById implements User
{
    protected final JDAImpl api;

    protected short discriminator;
    protected String name;
    protected String avatarId;
    protected String bannerId;
    protected int accentColor;
    protected long privateChannel = 0L;
    protected boolean bot;
    protected boolean system;
    protected boolean fake = false;
    protected int flags;

    public UserImpl(long id, JDAImpl api)
    {
        super(id);
        this.api = api;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public String getDiscriminator()
    {
        return Helpers.format("%04d", discriminator);
    }

    @Override
    public String getAvatarId()
    {
        return avatarId;
    }

    @Nullable
    @Override
    public String getBannerId()
    {
        return bannerId;
    }

    @Nullable
    @Override
    public Color getAccentColor()
    {
        return accentColor == User.DEFAULT_ACCENT_COLOR_RAW ? null : new Color(accentColor);
    }

    @Override
    public int getAccentColorRaw()
    {
        return accentColor;
    }

    @Nonnull
    @Override
    public RestAction<Profile> retrieveProfile()
    {
        Route.CompiledRoute route = Route.Users.GET_USER.compile(getId());
        return new RestActionImpl<Profile>(getJDA(), route, (response, request) -> {
            DataObject json = response.getObject();

            String bannerId = json.getString("banner");
            int accentColor = json.getInt("accent_color", User.DEFAULT_ACCENT_COLOR_RAW);

            return new Profile(getIdLong(), bannerId, accentColor);
        });
    }

    @Nonnull
    @Override
    public String getDefaultAvatarId()
    {
        return String.valueOf(discriminator % 5);
    }

    @Nonnull
    @Override
    public String getAsTag()
    {
        return getName() + '#' + getDiscriminator();
    }

    @Override
    public boolean hasPrivateChannel()
    {
        return privateChannel != 0;
    }

    @Nonnull
    @Override
    public RestAction<PrivateChannel> openPrivateChannel()
    {
        return new DeferredRestAction<>(getJDA(), PrivateChannel.class, this::getPrivateChannel, () -> {
            Route.CompiledRoute route = Route.Self.CREATE_PRIVATE_CHANNEL.compile();
            DataObject body = DataObject.empty().put("recipient_id", getId());
            return new RestActionImpl<>(getJDA(), route, body, (response, request) ->
            {
                PrivateChannel priv = api.getEntityBuilder().createPrivateChannel(response.getObject(), this);
                UserImpl.this.privateChannel = priv.getIdLong();
                return priv;
            });
        });
    }

    public PrivateChannel getPrivateChannel()
    {
        if (!hasPrivateChannel())
            return null;
        PrivateChannel channel = getJDA().getPrivateChannelById(privateChannel);
        return channel != null ? channel : new PrivateChannelImpl(privateChannel, this);
    }

    @Nonnull
    @Override
    public List<Guild> getMutualGuilds()
    {
        return getJDA().getMutualGuilds(this);
    }

    @Override
    public boolean isBot()
    {
        return bot;
    }

    @Override
    public boolean isSystem()
    {
        return system;
    }

    @Nonnull
    @Override
    public JDAImpl getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public EnumSet<UserFlag> getFlags()
    {
        return UserFlag.getFlags(flags);
    }
    
    @Override
    public int getFlagsRaw()
    {
        return flags;
    }

    @Override
    public String toString()
    {
        return "U:" + getName() + '(' + getId() + ')';
    }

    // -- Setters --

    public UserImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public UserImpl setDiscriminator(String discriminator)
    {
        this.discriminator = Short.parseShort(discriminator);
        return this;
    }

    public UserImpl setAvatarId(String avatarId)
    {
        this.avatarId = avatarId;
        return this;
    }

    public UserImpl setBannerId(String bannerId)
    {
        this.bannerId = bannerId;
        return this;
    }

    public UserImpl setAccentColor(int accentColor)
    {
        this.accentColor = accentColor;
        return this;
    }

    public UserImpl setPrivateChannel(PrivateChannel privateChannel)
    {
        if (privateChannel != null)
            this.privateChannel = privateChannel.getIdLong();
        return this;
    }

    public UserImpl setBot(boolean bot)
    {
        this.bot = bot;
        return this;
    }

    public UserImpl setSystem(boolean system)
    {
        this.system = system;
        return this;
    }

    public UserImpl setFake(boolean fake)
    {
        this.fake = fake;
        return this;
    }
    
    public UserImpl setFlags(int flags)
    {
        this.flags = flags;
        return this;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;

        String out;
        if (!alt)
            out = getAsMention();
        else if (upper)
            out = getAsTag().toUpperCase();
        else
            out = getAsTag();

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}
