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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @since  3.0
 * @author Florian Spieß
 */
public class ChannelAction extends RestAction<Channel> //todo documentation
{
    public static final int ROLE_TYPE = 0;
    public static final int USER_TYPE = 1;

    protected final Set<PromisePermissionOverride> overrides = new HashSet<>();
    protected final Guild guild;
    protected final boolean voice;
    protected String name;
    protected String topic = null;

    // --voice only--
    protected Integer bitrate = null;
    protected Integer userlimit = null;

    public ChannelAction(JDA api, Route.CompiledRoute route, String name, Guild guild, boolean voice)
    {
        super(api, route, null);
        this.guild = guild;
        this.voice = voice;
        this.name = name;
    }

    public ChannelAction setName(String name)
    {
        Args.notNull(name, "Channel name");
        if (name.length() < 2 || name.length() > 100)
            throw new IllegalArgumentException("Provided channel name must be 2 to 100 characters in length");

        this.name = name;
        return this;
    }

    public ChannelAction setTopic(String topic)
    {
        if (voice)
            throw new UnsupportedOperationException("Cannot set the topic for a VoiceChannel!");
        if (topic != null && topic.length() > 1024)
            throw new IllegalArgumentException("Channel Topic must not be greater than 1024 in length!");
        this.topic = topic;
        return this;
    }

    public ChannelAction addPermissionOverride(Role role, Collection<Permission> allow, Collection<Permission> deny)
    {
        final long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        final long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addPermissionOverride(role, allowRaw, denyRaw);
    }

    public ChannelAction addPermissionOverride(Member member, Collection<Permission> allow, Collection<Permission> deny)
    {
        final long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        final long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addPermissionOverride(member, allowRaw, denyRaw);
    }

    public ChannelAction addPermissionOverride(Role role, long allow, long deny)
    {
        Args.notNull(role, "Override Role");
        if (!role.getGuild().equals(guild))
            throw new IllegalArgumentException("Specified Role is not in the same Guild!");

        String id = role.getId();
        overrides.add(new PromisePermissionOverride(ROLE_TYPE, id, allow, deny));
        return this;
    }

    public ChannelAction addPermissionOverride(Member member, long allow, long deny)
    {
        Args.notNull(member, "Override Member");
        if (!member.getGuild().equals(guild))
            throw new IllegalArgumentException("Specified Role is not in the same Guild!");

        String id = member.getUser().getId();
        overrides.add(new PromisePermissionOverride(USER_TYPE, id, allow, deny));
        return this;
    }

    // --voice only--
    public ChannelAction setBitrate(Integer bitrate)
    {
        if (!voice)
            throw new UnsupportedOperationException("Cannot set the bitrate for a TextChannel!");
        if (bitrate != null)
        {
            if (bitrate < 8000)
                throw new IllegalArgumentException("Bitrate must be greater than 8000.");
            if (bitrate > 128000) // todo: checking whether guild is VIP or not (96000 max no vip)
                throw new IllegalArgumentException("Bitrate must be less than 128000.");
        }

        this.bitrate = bitrate;
        return this;
    }

    public ChannelAction setUserlimit(Integer userlimit)
    {
        if (!voice)
            throw new UnsupportedOperationException("Cannot set the userlimit for a TextChannel!");
        if (userlimit != null && (userlimit < 0 || userlimit > 99))
            throw new IllegalArgumentException("Userlimit must be between 0-99!");
        this.userlimit = userlimit;
        return this;
    }

    @Override
    protected void finalizeData()
    {
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("type", voice ? 2 : 0);
        data.put("permission_overwrites", new JSONArray(overrides));
        if (voice)
        {
            if (bitrate != null)
                data.put("bitrate", bitrate.intValue());
            if (userlimit != null)
                data.put("user_limit", userlimit.intValue());
        }

        this.data = data;
        super.finalizeData();
    }

    @Override
    protected void handleResponse(Response response, Request request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        EntityBuilder builder = EntityBuilder.get(api);
        Channel channel = voice
                ? builder.createVoiceChannel(response.getObject(), guild.getId())
                : builder.createTextChannel(response.getObject(),  guild.getId());

        request.onSuccess(channel);
    }

    protected final class PromisePermissionOverride implements JSONString
    {
        protected final String id;
        protected final long deny;
        protected final long allow;
        protected final int type;

        public PromisePermissionOverride(int type, String id, long allow, long deny)
        {
            this.type = type;
            this.id = id;
            this.deny  = deny;
            this.allow = allow;
        }

        @Override
        public String toJSONString()
        {
            JSONObject object = new JSONObject();
            object.put("id",    id);
            object.put("type",  type);
            object.put("deny",  deny);
            object.put("allow", allow);

            return object.toString();
        }
    }
}
