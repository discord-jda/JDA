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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.core.requests.RestAction RestAction} specifically
 * designed to create a {@link net.dv8tion.jda.core.entities.Channel Channel}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 */
public class ChannelAction extends AuditableRestAction<Channel>
{
    protected final Set<PermOverrideData> overrides = new HashSet<>();
    protected final Guild guild;
    protected final ChannelType type;
    protected String name;
    protected Category parent;

    // --text only--
    protected String topic = null;
    protected Boolean nsfw = null;

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
     * @param  type
     *         What kind of channel should be created
     */
    public ChannelAction(Route.CompiledRoute route, String name, Guild guild, ChannelType type)
    {
        super(guild.getJDA(), route);
        this.guild = guild;
        this.type = type;
        this.name = name;
    }

    @Override
    public ChannelAction setCheck(BooleanSupplier checks)
    {
        return (ChannelAction) super.setCheck(checks);
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
    @CheckReturnValue
    public ChannelAction setName(String name)
    {
        Checks.notNull(name, "Channel name");
        if (name.length() < 2 || name.length() > 100)
            throw new IllegalArgumentException("Provided channel name must be 2 to 100 characters in length");

        this.name = name;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Category Category} for the new Channel
     *
     * @param  category
     *         The parent for the new Channel
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is for a Category
     * @throws IllegalArgumentException
     *         If the provided category is {@code null}
     *         or not from this Guild
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    public ChannelAction setParent(Category category)
    {
        Checks.check(category == null || category.getGuild().equals(guild), "Category is not from same guild!");
        this.parent = category;
        return this;
    }

    /**
     * Sets the topic for the new TextChannel
     *
     * @param  topic
     *         The topic for the new Channel (max 1024 chars)
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a TextChannel
     * @throws IllegalArgumentException
     *         If the provided topic is longer than 1024 chars
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    public ChannelAction setTopic(String topic)
    {
        if (type != ChannelType.TEXT)
            throw new UnsupportedOperationException("Can only set the topic for a TextChannel!");
        if (topic != null && topic.length() > 1024)
            throw new IllegalArgumentException("Channel Topic must not be greater than 1024 in length!");
        this.topic = topic;
        return this;
    }

    /**
     * Sets the NSFW flag for the new TextChannel
     *
     * @param  nsfw
     *         The NSFW flag for the new Channel
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a TextChannel
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    public ChannelAction setNSFW(boolean nsfw)
    {
        if (type != ChannelType.TEXT)
            throw new UnsupportedOperationException("Can only set nsfw for a TextChannel!");
        this.nsfw = nsfw;
        return this;
    }

    /**
     * Adds a new Role-{@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the new Channel.
     *
     * @param  target
     *         The not-null {@link net.dv8tion.jda.core.entities.Role Role} or {@link net.dv8tion.jda.core.entities.Member Member} for the override
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
    @CheckReturnValue
    public ChannelAction addPermissionOverride(IPermissionHolder target, Collection<Permission> allow, Collection<Permission> deny)
    {
        checkPermissions(allow);
        checkPermissions(deny);
        final long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        final long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addPermissionOverride(target, allowRaw, denyRaw);
    }

    /**
     * Adds a new Role-{@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the new Channel.
     *
     * @param  target
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
    @CheckReturnValue
    public ChannelAction addPermissionOverride(IPermissionHolder target, long allow, long deny)
    {
        Checks.notNull(target, "Override Role");
        Checks.notNegative(allow, "Granted permissions value");
        Checks.notNegative(deny, "Denied permissions value");
        Checks.check(allow <= Permission.ALL_PERMISSIONS, "Specified allow value may not be greater than a full permission set");
        Checks.check(deny <= Permission.ALL_PERMISSIONS,  "Specified deny value may not be greater than a full permission set");
        Checks.check(target.getGuild().equals(guild), "Specified Role is not in the same Guild!");

        if (target instanceof Role)
        {
            Role r = (Role) target;
            long id = r.getIdLong();
            overrides.add(new PermOverrideData(PermOverrideData.ROLE_TYPE, id, allow, deny));
        }
        else
        {
            Member m = (Member) target;
            long id = m.getUser().getIdLong();
            overrides.add(new PermOverrideData(PermOverrideData.MEMBER_TYPE, id, allow, deny));
        }
        return this;
    }

    // --voice only--
    /**
     * Sets the bitrate for the new VoiceChannel
     *
     * @param  bitrate
     *         The bitrate for the new Channel (min {@code 8000}; max {@code 96000}/{@code 128000}
     *         (for {@link net.dv8tion.jda.core.entities.Guild#getFeatures() VIP Guilds})) or null to use default ({@code 64000})
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a VoiceChannel
     * @throws IllegalArgumentException
     *         If the provided bitrate is less than 8000 or greater than 128000
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    public ChannelAction setBitrate(Integer bitrate)
    {
        if (type != ChannelType.VOICE)
            throw new UnsupportedOperationException("Can only set the bitrate for a VoiceChannel!");
        if (bitrate != null)
        {
            int maxBitrate = guild.getFeatures().contains("VIP_REGIONS") ? 128000 : 96000;
            if (bitrate < 8000)
                throw new IllegalArgumentException("Bitrate must be greater than 8000.");
            else if (bitrate > maxBitrate)
                throw new IllegalArgumentException("Bitrate must be less than " + maxBitrate);
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
     *         If this ChannelAction is not for a VoiceChannel
     * @throws IllegalArgumentException
     *         If the provided userlimit is negative or above {@code 99}
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    public ChannelAction setUserlimit(Integer userlimit)
    {
        if (type != ChannelType.VOICE)
            throw new UnsupportedOperationException("Can only set the userlimit for a VoiceChannel!");
        if (userlimit != null && (userlimit < 0 || userlimit > 99))
            throw new IllegalArgumentException("Userlimit must be between 0-99!");
        this.userlimit = userlimit;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("type", type.getId());
        object.put("permission_overwrites", new JSONArray(overrides));
        switch (type)
        {
            case VOICE:
                if (bitrate != null)
                    object.put("bitrate", bitrate.intValue());
                if (userlimit != null)
                    object.put("user_limit", userlimit.intValue());
                break;
            case TEXT:
                if (topic != null && !topic.isEmpty())
                    object.put("topic", topic);
                if (nsfw != null)
                    object.put("nsfw", nsfw);
        }
        if (type != ChannelType.CATEGORY && parent != null)
            object.put("parent_id", parent.getId());

        return getRequestBody(object);
    }

    @Override
    protected void handleResponse(Response response, Request<Channel> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        EntityBuilder builder = api.getEntityBuilder();;
        Channel channel;
        switch (type)
        {
            case VOICE:
                channel = builder.createVoiceChannel(response.getObject(), guild.getIdLong());
                break;
            case TEXT:
                channel = builder.createTextChannel(response.getObject(), guild.getIdLong());
                break;
            case CATEGORY:
                channel = builder.createCategory(response.getObject(), guild.getIdLong());
                break;
            default:
                request.onFailure(new IllegalStateException("Created channel of unknown type!"));
                return;
        }
        request.onSuccess(channel);
    }

    protected void checkPermissions(Permission... permissions)
    {
        if (permissions == null)
            return;
        for (Permission p : permissions)
            Checks.notNull(p, "Permissions");
    }

    protected void checkPermissions(Collection<Permission> permissions)
    {
        if (permissions == null)
            return;
        for (Permission p : permissions)
            Checks.notNull(p, "Permissions");
    }
}
