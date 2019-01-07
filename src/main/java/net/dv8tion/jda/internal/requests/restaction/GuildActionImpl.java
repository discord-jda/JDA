/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.requests.restaction.GuildAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class GuildActionImpl extends RestActionImpl<Void> implements GuildAction
{
    protected String name;
    protected Region region;
    protected Icon icon;
    protected Guild.VerificationLevel verificationLevel;
    protected Guild.NotificationLevel notificationLevel;
    protected Guild.ExplicitContentLevel explicitContentLevel;

    protected final List<RoleData> roles;
    protected final List<ChannelData> channels;

    public GuildActionImpl(JDA api, String name)
    {
        super(api, Route.Guilds.CREATE_GUILD.compile());
        this.setName(name);

        this.roles = new LinkedList<>();
        this.channels = new LinkedList<>();
        // public role is the first element
        this.roles.add(new RoleData(0));
    }

    @Override
    public GuildActionImpl setCheck(BooleanSupplier checks)
    {
        return (GuildActionImpl) super.setCheck(checks);
    }

    @Override
    @CheckReturnValue
    public GuildActionImpl setRegion(Region region)
    {
        Checks.check(region == null || !region.isVip(), "Cannot create a Guild with a VIP voice region!");
        this.region = region;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildActionImpl setIcon(Icon icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildActionImpl setName(String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.check(name.length() >= 2 && name.length() <= 100, "Name must have 2-100 characters in length!");
        this.name = name;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildActionImpl setVerificationLevel(Guild.VerificationLevel level)
    {
        this.verificationLevel = level;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildActionImpl setNotificationLevel(Guild.NotificationLevel level)
    {
        this.notificationLevel = level;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildActionImpl setExplicitContentLevel(Guild.ExplicitContentLevel level)
    {
        this.explicitContentLevel = level;
        return this;
    }

    // Channels

    @Override
    @CheckReturnValue
    public GuildActionImpl addChannel(ChannelData channel)
    {
        Checks.notNull(channel, "Channel");
        this.channels.add(channel);
        return this;
    }

    @Override
    @CheckReturnValue
    public ChannelData getChannel(int index)
    {
        return this.channels.get(index);
    }

    @Override
    @CheckReturnValue
    public ChannelData removeChannel(int index)
    {
        return this.channels.remove(index);
    }

    @Override
    @CheckReturnValue
    public GuildActionImpl removeChannel(ChannelData data)
    {
        this.channels.remove(data);
        return this;
    }

    @Override
    @CheckReturnValue
    public ChannelData newChannel(ChannelType type, String name)
    {
        ChannelData data = new ChannelData(type, name);
        addChannel(data);
        return data;
    }

    // Roles

    @Override
    @CheckReturnValue
    public RoleData getPublicRole()
    {
        return this.roles.get(0);
    }

    @Override
    @CheckReturnValue
    public RoleData getRole(int index)
    {
        return this.roles.get(index);
    }

    @Override
    @CheckReturnValue
    public RoleData newRole()
    {
        final RoleData role = new RoleData(roles.size());
        this.roles.add(role);
        return role;
    }

    @Override
    protected RequestBody finalizeData()
    {
        final JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("roles", new JSONArray(roles));
        if (!channels.isEmpty())
            object.put("channels", new JSONArray(channels));
        if (icon != null)
            object.put("icon", icon.getEncoding());
        if (verificationLevel != null)
            object.put("verification_level", verificationLevel.getKey());
        if (notificationLevel != null)
            object.put("default_message_notifications", notificationLevel.getKey());
        if (explicitContentLevel != null)
            object.put("explicit_content_filter", explicitContentLevel.getKey());
        if (region != null)
            object.put("region", region.getKey());
        return getRequestBody(object);
    }
}
