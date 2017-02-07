/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
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
 * Extension of {@link net.dv8tion.jda.core.requests.RestAction RestAction} specifically
 * designed to create a {@link net.dv8tion.jda.core.entities.Channel Channel}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 * @author Florian Spieß
 */
public class ChannelAction extends RestAction<Channel>
{
    public static final int ROLE_TYPE = 0;
    public static final int MEMBER_TYPE = 1;

    protected final Set<PromisePermissionOverride> overrides = new HashSet<>();
    protected final Guild guild;
    protected final boolean voice;
    protected String name;
    protected String topic = null;

    // --voice only--
    protected Integer bitrate = null;
    protected Integer userlimit = null;

    /**
     * Creates a new ChannelAction instance
     *
     * @param  route
     *         The {@link net.dv8tion.jda.core.requests.Route.CompiledRoute CompileRoute}
     *         to use for this action
     * @param  name
     *         The name for the new {@link net.dv8tion.jda.core.entities.Channel Channel}
     * @param  guild
     *         The {@link net.dv8tion.jda.core.entities.Guild Guild} the channel should be created in
     * @param  voice
     *         Whether the new channel should be a VoiceChannel (false {@literal ->} TextChannel)
     */
    public ChannelAction(Route.CompiledRoute route, String name, Guild guild, boolean voice)
    {
        super(guild.getJDA(), route, null);
        this.guild = guild;
        this.voice = voice;
        this.name = name;
    }

    /**
     * Sets the name for the new Channel
     *
     * @param  name
     *         The not-null name for the new Channel (2-100 chars long)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is null or not between 2-100 chars long
     *
     * @return The current ChannelAction, for chaining convenience
     */
    public ChannelAction setName(String name)
    {
        Args.notNull(name, "Channel name");
        if (name.length() < 2 || name.length() > 100)
            throw new IllegalArgumentException("Provided channel name must be 2 to 100 characters in length");

        this.name = name;
        return this;
    }

    /**
     * Sets the topic for the new TextChannel
     *
     * @param  topic
     *         The topic for the new Channel (max 1024 chars)
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is for a VoiceChannel
     * @throws IllegalArgumentException
     *         If the provided topic is longer than 1024 chars
     *
     * @return The current ChannelAction, for chaining convenience
     */
    public ChannelAction setTopic(String topic)
    {
        if (voice)
            throw new UnsupportedOperationException("Cannot set the topic for a VoiceChannel!");
        if (topic != null && topic.length() > 1024)
            throw new IllegalArgumentException("Channel Topic must not be greater than 1024 in length!");
        this.topic = topic;
        return this;
    }

    /**
     * Adds a new Role-{@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the new Channel.
     *
     * @param  role
     *         The not-null {@link net.dv8tion.jda.core.entities.Role Role} for the override
     * @param  allow
     *         The granted {@link net.dv8tion.jda.core.Permission Permissions} for the override or null
     * @param  deny
     *         The denied {@link net.dv8tion.jda.core.Permission Permissions} for the override or null
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified {@link net.dv8tion.jda.core.entities.Role Role} is null
     *         or not within the same guild.
     *
     * @return The current ChannelAction, for chaining convenience
     */
    public ChannelAction addPermissionOverride(Role role, Collection<Permission> allow, Collection<Permission> deny)
    {
        checkPermissions(allow);
        checkPermissions(deny);
        final long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        final long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addPermissionOverride(role, allowRaw, denyRaw);
    }

    /**
     * Adds a new Member-{@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the new Channel.
     *
     * @param  member
     *         The not-null {@link net.dv8tion.jda.core.entities.Member Member} for the override
     * @param  allow
     *         The granted {@link net.dv8tion.jda.core.Permission Permissions} for the override or null
     * @param  deny
     *         The denied {@link net.dv8tion.jda.core.Permission Permissions} for the override or null
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified {@link net.dv8tion.jda.core.entities.Member Member} is null
     *         or not within the same guild.
     *
     * @return The current ChannelAction, for chaining convenience
     */
    public ChannelAction addPermissionOverride(Member member, Collection<Permission> allow, Collection<Permission> deny)
    {
        checkPermissions(allow);
        checkPermissions(deny);
        final long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        final long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addPermissionOverride(member, allowRaw, denyRaw);
    }

    /**
     * Adds a new Role-{@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the new Channel.
     *
     * @param  role
     *         The not-null {@link net.dv8tion.jda.core.entities.Role Role} for the override
     * @param  allow
     *         The granted {@link net.dv8tion.jda.core.Permission Permissions} for the override
     *         Use {@link net.dv8tion.jda.core.Permission#getRawValue()} to retrieve these Permissions.
     * @param  deny
     *         The denied {@link net.dv8tion.jda.core.Permission Permissions} for the override
     *         Use {@link net.dv8tion.jda.core.Permission#getRawValue()} to retrieve these Permissions.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified {@link net.dv8tion.jda.core.entities.Role Role} is null
     *                 or not within the same guild.</li>
     *             <li>If one of the provided Permission values is invalid</li>
     *         </ul>
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    net.dv8tion.jda.core.Permission#getRawValue()
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection)
     * @see    net.dv8tion.jda.core.Permission#getRaw(net.dv8tion.jda.core.Permission...)
     */
    public ChannelAction addPermissionOverride(Role role, long allow, long deny)
    {
        Args.notNull(role, "Override Role");
        Args.notNegative(allow, "Granted permissions value");
        Args.notNegative(deny, "Denied permissions value");
        Args.check(allow <= Permission.ALL_PERMISSIONS, "Specified allow value may not be greater than a full permission set");
        Args.check(deny <= Permission.ALL_PERMISSIONS,  "Specified deny value may not be greater than a full permission set");
        Args.check(role.getGuild().equals(guild), "Specified Role is not in the same Guild!");

        String id = role.getId();
        overrides.add(new PromisePermissionOverride(ROLE_TYPE, id, allow, deny));
        return this;
    }

    /**
     * Adds a new Member-{@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the new Channel.
     *
     * @param  member
     *         The not-null {@link net.dv8tion.jda.core.entities.Member Member} for the override
     * @param  allow
     *         The granted {@link net.dv8tion.jda.core.Permission Permissions} for the override
     *         Use {@link net.dv8tion.jda.core.Permission#getRawValue()} to retrieve these Permissions.
     * @param  deny
     *         The denied {@link net.dv8tion.jda.core.Permission Permissions} for the override
     *         Use {@link net.dv8tion.jda.core.Permission#getRawValue()} to retrieve these Permissions.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified {@link net.dv8tion.jda.core.entities.Member Member} is null
     *                 or not within the same guild.</li>
     *             <li>If one of the provided Permission values is invalid</li>
     *         </ul>
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    net.dv8tion.jda.core.Permission#getRawValue()
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection)
     * @see    net.dv8tion.jda.core.Permission#getRaw(net.dv8tion.jda.core.Permission...)
     */
    public ChannelAction addPermissionOverride(Member member, long allow, long deny)
    {
        Args.notNull(member, "Override Member");
        Args.notNegative(allow, "Granted permissions value");
        Args.notNegative(deny, "Denied permissions value");
        Args.check(allow <= Permission.ALL_PERMISSIONS, "Specified allow value may not be greater than a full permission set");
        Args.check(deny <= Permission.ALL_PERMISSIONS,  "Specified deny value may not be greater than a full permission set");
        Args.check(member.getGuild().equals(guild), "Specified Member is not in the same Guild!");

        String id = member.getUser().getId();
        overrides.add(new PromisePermissionOverride(MEMBER_TYPE, id, allow, deny));
        return this;
    }

    // --voice only--
    /**
     * Sets the bitrate for the new VoiceChannel
     *
     * @param  bitrate
     *         The bitrate for the new Channel (min 8000) or null to use default (64000)
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is for a TextChannel
     * @throws IllegalArgumentException
     *         If the provided bitrate is less than 8000 or greater than 128000
     *
     * @return The current ChannelAction, for chaining convenience
     */
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

    /**
     * Sets the userlimit for the new VoiceChannel
     *
     * @param  userlimit
     *         The userlimit for the new VoiceChannel or {@code null}/{@code 0} to use no limit,
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is for a TextChannel
     * @throws IllegalArgumentException
     *         If the provided userlimit is negative or above {@code 99}
     *
     * @return The current ChannelAction, for chaining convenience
     */
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

    protected void checkPermissions(Permission... permissions)
    {
        if (permissions == null)
            return;
        for (Permission p : permissions)
            Args.notNull(p, "Permissions");
    }

    protected void checkPermissions(Collection<Permission> permissions)
    {
        if (permissions == null)
            return;
        for (Permission p : permissions)
            Args.notNull(p, "Permissions");
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
