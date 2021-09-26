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

package net.dv8tion.jda.internal.requests.restaction;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class ChannelActionImpl<T extends GuildChannel> extends AuditableRestActionImpl<T> implements ChannelAction<T>
{
    protected final TLongObjectMap<PermOverrideData> overrides = new TLongObjectHashMap<>();
    protected final Guild guild;
    protected final Class<T> clazz;

    // --all channels--
    protected String name;
    protected Category parent;
    protected Integer position;
    protected ChannelType type;

    // --text only--
    protected Integer slowmode = null;

    // --text and news--
    protected String topic = null;
    protected Boolean nsfw = null;

    // --voice only--
    protected Integer userlimit = null;

    // --voice and stage--
    protected Integer bitrate = null;

    public ChannelActionImpl(Class<T> clazz, String name, Guild guild, ChannelType type)
    {
        super(guild.getJDA(), Route.Guilds.CREATE_CHANNEL.compile(guild.getId()));
        this.clazz = clazz;
        this.guild = guild;
        this.type = type;
        this.name = name;
    }

    @Nonnull
    @Override
    public ChannelActionImpl<T> setCheck(BooleanSupplier checks)
    {
        return (ChannelActionImpl<T>) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public ChannelActionImpl<T> timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (ChannelActionImpl<T>) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public ChannelActionImpl<T> deadline(long timestamp)
    {
        return (ChannelActionImpl<T>) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return type;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setType(@Nonnull ChannelType type)
    {
        Checks.check(type == ChannelType.TEXT || type == ChannelType.NEWS, "Can only change ChannelType to TEXT or NEWS");

        if (this.type != ChannelType.TEXT && this.type != ChannelType.NEWS)
            throw new UnsupportedOperationException("Can only set ChannelType for TextChannel and NewsChannels");
        if (type == ChannelType.NEWS && !getGuild().getFeatures().contains("NEWS"))
            throw new IllegalStateException("Can only set ChannelType to NEWS for guilds with NEWS feature");

        this.type = type;

        //After the type is changed, be sure to clean up any properties that are exclusive to a specific channel type
        if (type != ChannelType.TEXT)
        {
            slowmode = null;
        }

        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setParent(Category category)
    {
        Checks.check(category == null || category.getGuild().equals(guild), "Category is not from same guild!");
        if (type == ChannelType.CATEGORY)
            throw new UnsupportedOperationException("Cannot set a parent Category on a Category");

        this.parent = category;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setPosition(Integer position)
    {
        Checks.check(position == null || position >= 0, "Position must be >= 0!");
        this.position = position;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setTopic(String topic)
    {
        if (type != ChannelType.TEXT && type != ChannelType.NEWS)
            throw new UnsupportedOperationException("Can only set the topic for a TextChannel or NewsChannel!");
        if (topic != null && topic.length() > 1024)
            throw new IllegalArgumentException("Channel Topic must not be greater than 1024 in length!");
        this.topic = topic;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setNSFW(boolean nsfw)
    {
        if (type != ChannelType.TEXT && type != ChannelType.NEWS)
            throw new UnsupportedOperationException("Can only set nsfw for a TextChannel or NewsChannel!");
        this.nsfw = nsfw;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setSlowmode(int slowmode)
    {
        if (type != ChannelType.TEXT)
            throw new UnsupportedOperationException("Can only set slowmode on text channels");
        Checks.check(slowmode <= TextChannel.MAX_SLOWMODE && slowmode >= 0, "Slowmode must be between 0 and %d (seconds)!", TextChannel.MAX_SLOWMODE);
        this.slowmode = slowmode;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> addMemberPermissionOverride(long userId, long allow, long deny)
    {
        return addOverride(userId, PermOverrideData.MEMBER_TYPE, allow, deny);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> addRolePermissionOverride(long roleId, long allow, long deny)
    {
        return addOverride(roleId, PermOverrideData.ROLE_TYPE, allow, deny);
    }

    @Nonnull
    @Override
    public ChannelAction<T> removePermissionOverride(long id)
    {
        overrides.remove(id);
        return this;
    }

    @Nonnull
    @Override
    public ChannelAction<T> clearPermissionOverrides()
    {
        overrides.clear();
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ChannelAction<T> syncPermissionOverrides()
    {
        if (parent == null)
            throw new IllegalStateException("Cannot sync overrides without parent category! Use setParent(category) first!");
        clearPermissionOverrides();
        Member selfMember = getGuild().getSelfMember();
        boolean canSetRoles = selfMember.hasPermission(parent, Permission.MANAGE_ROLES);
        //You can only set MANAGE_ROLES if you have ADMINISTRATOR or MANAGE_PERMISSIONS as an override on the channel
        // That is why we explicitly exclude it here!
        // This is by far the most complex and weird permission logic in the entire API...
        long botPerms = PermissionUtil.getEffectivePermission(selfMember) & ~Permission.MANAGE_PERMISSIONS.getRawValue();

        parent.getRolePermissionOverrides().forEach(override -> {
            long allow = override.getAllowedRaw();
            long deny = override.getDeniedRaw();
            if (!canSetRoles)
            {
                allow &= botPerms;
                deny &= botPerms;
            }
            addRolePermissionOverride(override.getIdLong(), allow, deny);
        });

        parent.getMemberPermissionOverrides().forEach(override -> {
            long allow = override.getAllowedRaw();
            long deny = override.getDeniedRaw();
            if (!canSetRoles)
            {
                allow &= botPerms;
                deny &= botPerms;
            }
            addMemberPermissionOverride(override.getIdLong(), allow, deny);
        });
        return this;
    }

    private ChannelActionImpl<T> addOverride(long targetId, int type, long allow, long deny)
    {
        Member selfMember = getGuild().getSelfMember();
        boolean canSetRoles = selfMember.hasPermission(Permission.ADMINISTRATOR);
        if (!canSetRoles && parent != null) // You can also set MANAGE_ROLES if you have it on the category (apparently?)
            canSetRoles = selfMember.hasPermission(parent, Permission.MANAGE_ROLES);
        if (!canSetRoles)
        {
            // Prevent permission escalation
            //You can only set MANAGE_ROLES if you have ADMINISTRATOR or MANAGE_PERMISSIONS as an override on the channel
            // That is why we explicitly exclude it here!
            // This is by far the most complex and weird permission logic in the entire API...
            long botPerms = PermissionUtil.getEffectivePermission(selfMember) & ~Permission.MANAGE_PERMISSIONS.getRawValue();

            EnumSet<Permission> missingPerms = Permission.getPermissions((allow | deny) & ~botPerms);
            if (!missingPerms.isEmpty())
                throw new InsufficientPermissionException(guild, Permission.MANAGE_PERMISSIONS, "You must have Permission.MANAGE_PERMISSIONS on the channel explicitly in order to set permissions you don't already have!");
        }

        overrides.put(targetId, new PermOverrideData(type, targetId, allow, deny));
        return this;
    }

    // --voice only--
    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setBitrate(Integer bitrate)
    {
        if (!type.isAudio())
            throw new UnsupportedOperationException("Can only set the bitrate for an Audio Channel!");
        if (bitrate != null)
        {
            int maxBitrate = getGuild().getMaxBitrate();
            if (bitrate < 8000)
                throw new IllegalArgumentException("Bitrate must be greater than 8000.");
            else if (bitrate > maxBitrate)
                throw new IllegalArgumentException("Bitrate must be less than " + maxBitrate);
        }

        this.bitrate = bitrate;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setUserlimit(Integer userlimit)
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
        DataObject object = DataObject.empty();

        //All channel types
        object.put("name", name);
        object.put("type", type.getId());
        object.put("permission_overwrites", DataArray.fromCollection(overrides.valueCollection()));
        if (position != null)
            object.put("position", position);
        if (parent != null)
            object.put("parent_id", parent.getId());

        //Text only
        if (slowmode != null)
            object.put("rate_limit_per_user", slowmode);

        //Text and News
        if (topic != null && !topic.isEmpty())
            object.put("topic", topic);
        if (nsfw != null)
            object.put("nsfw", nsfw);

        //Voice only
        if (userlimit != null)
            object.put("user_limit", userlimit);

        //Voice and Stage
        if (bitrate != null)
            object.put("bitrate", bitrate);

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<T> request)
    {
        EntityBuilder builder = api.getEntityBuilder();
        GuildChannel channel;
        switch (type)
        {
            case TEXT:
                channel = builder.createTextChannel(response.getObject(), guild.getIdLong());
                break;
            case NEWS:
                channel = builder.createNewsChannel(response.getObject(), guild.getIdLong());
                break;
            case VOICE:
                channel = builder.createVoiceChannel(response.getObject(), guild.getIdLong());
                break;
            case STAGE:
                channel = builder.createStageChannel(response.getObject(), guild.getIdLong());
                break;
            case CATEGORY:
                channel = builder.createCategory(response.getObject(), guild.getIdLong());
                break;
            default:
                request.onFailure(new IllegalStateException("Created channel of unknown type!"));
                return;
        }
        request.onSuccess(clazz.cast(channel));
    }
}
