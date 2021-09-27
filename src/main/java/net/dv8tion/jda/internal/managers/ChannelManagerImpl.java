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

package net.dv8tion.jda.internal.managers;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.AbstractChannelImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.PermOverrideData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;

public class ChannelManagerImpl extends ManagerBase<ChannelManager> implements ChannelManager
{
    protected GuildChannel channel;

    protected String name;
    protected String parent;
    protected String topic;
    protected String region;
    protected int position;
    protected boolean nsfw;
    protected int slowmode;
    protected int userlimit;
    protected int bitrate;
    protected boolean news;

    protected final Object lock = new Object();
    protected final TLongObjectHashMap<PermOverrideData> overridesAdd;
    protected final TLongSet overridesRem;

    /**
     * Creates a new ChannelManager instance
     *
     * @param channel
     *        {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} that should be modified
     *        <br>Either {@link net.dv8tion.jda.api.entities.VoiceChannel Voice}- or {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     */
    public ChannelManagerImpl(GuildChannel channel)
    {
        super(channel.getJDA(),
              Route.Channels.MODIFY_CHANNEL.compile(channel.getId()));
        JDA jda = channel.getJDA();
        ChannelType type = channel.getType();
        this.channel = channel;
        if (isPermissionChecksEnabled())
            checkPermissions();
        this.overridesAdd = new TLongObjectHashMap<>();
        this.overridesRem = new TLongHashSet();
    }

    @Nonnull
    @Override
    public GuildChannel getChannel()
    {
        GuildChannel realChannel = api.getGuildChannelById(channel.getType(), channel.getIdLong());
        if (realChannel != null)
            channel = realChannel;
        return channel;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & PARENT) == PARENT)
            this.parent = null;
        if ((fields & TOPIC) == TOPIC)
            this.topic = null;
        if ((fields & NEWS) == NEWS)
            this.news = false;
        if ((fields & REGION) == REGION)
            this.region = null;
        if ((fields & PERMISSION) == PERMISSION)
        {
            withLock(lock, (lock) ->
            {
                this.overridesRem.clear();
                this.overridesAdd.clear();
            });
        }
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl reset()
    {
        super.reset();
        this.name = null;
        this.parent = null;
        this.topic = null;
        this.region = null;
        this.news = false;
        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            this.overridesAdd.clear();
        });
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl clearOverridesAdded()
    {
        withLock(lock, (lock) ->
        {
            this.overridesAdd.clear();
            if (this.overridesRem.isEmpty())
                set &= ~PERMISSION;
        });
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl clearOverridesRemoved()
    {
        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            if (this.overridesAdd.isEmpty())
                set &= ~PERMISSION;
        });
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl putPermissionOverride(@Nonnull IPermissionHolder permHolder, long allow, long deny)
    {
        Checks.notNull(permHolder, "PermissionHolder");
        Checks.check(permHolder.getGuild().equals(getGuild()), "PermissionHolder is not from the same Guild!");
        final long id = permHolder.getIdLong();
        final int type = permHolder instanceof Role ? PermOverrideData.ROLE_TYPE : PermOverrideData.MEMBER_TYPE;
        putPermissionOverride(new PermOverrideData(type, id, allow, deny));
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl putMemberPermissionOverride(long memberId, long allow, long deny)
    {
        putPermissionOverride(new PermOverrideData(PermOverrideData.MEMBER_TYPE, memberId, allow, deny));
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl putRolePermissionOverride(long roleId, long allow, long deny)
    {
        putPermissionOverride(new PermOverrideData(PermOverrideData.ROLE_TYPE, roleId, allow, deny));
        return this;
    }

    private void checkCanPutPermissions(long allow, long deny)
    {
        Member selfMember = getGuild().getSelfMember();
        if (isPermissionChecksEnabled() && !selfMember.hasPermission(Permission.ADMINISTRATOR))
        {
            if (!selfMember.hasPermission(channel, Permission.MANAGE_ROLES))
                throw new InsufficientPermissionException(channel, Permission.MANAGE_PERMISSIONS); // We can't manage permissions at all!

            // Check on channel level to make sure we are actually able to set all the permissions!
            long channelPermissions = PermissionUtil.getExplicitPermission(channel, selfMember, false);
            if ((channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0) // This implies we can only set permissions the bot also has in the channel!
            {
                //You can only set MANAGE_ROLES if you have ADMINISTRATOR or MANAGE_PERMISSIONS as an override on the channel
                // That is why we explicitly exclude it here!
                // This is by far the most complex and weird permission logic in the entire API...
                long botPerms = PermissionUtil.getEffectivePermission(channel, selfMember) & ~Permission.MANAGE_ROLES.getRawValue();
                EnumSet<Permission> missing = Permission.getPermissions((allow | deny) & ~botPerms);
                if (!missing.isEmpty())
                    throw new InsufficientPermissionException(channel, Permission.MANAGE_PERMISSIONS, "You must have Permission.MANAGE_PERMISSIONS on the channel explicitly in order to set permissions you don't already have!");
            }
        }
    }

    private void putPermissionOverride(@Nonnull final PermOverrideData overrideData)
    {
        checkCanPutPermissions(overrideData.allow, overrideData.deny);
        withLock(lock, (lock) ->
        {
            this.overridesRem.remove(overrideData.id);
            this.overridesAdd.put(overrideData.id, overrideData);
            set |= PERMISSION;
        });
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl removePermissionOverride(@Nonnull IPermissionHolder permHolder)
    {
        Checks.notNull(permHolder, "PermissionHolder");
        Checks.check(permHolder.getGuild().equals(getGuild()), "PermissionHolder is not from the same Guild!");
        return removePermissionOverride(permHolder.getIdLong());
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl removePermissionOverride(final long id)
    {
        if (isPermissionChecksEnabled() && !getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS);
        withLock(lock, (lock) ->
        {
            this.overridesRem.add(id);
            this.overridesAdd.remove(id);
            set |= PERMISSION;
        });
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl sync(@Nonnull GuildChannel syncSource)
    {
        Checks.notNull(syncSource, "SyncSource");
        Checks.check(getGuild().equals(syncSource.getGuild()), "Sync only works for channels of same guild");

        if (syncSource.equals(getChannel()))
            return this;

        if (isPermissionChecksEnabled())
        {
            Member selfMember = getGuild().getSelfMember();
            if (!selfMember.hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
                throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS);

            if (!selfMember.canSync(channel, syncSource))
                throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS,
                    "Cannot sync channel with parent due to permission escalation issues. " +
                    "One of the overrides would set MANAGE_PERMISSIONS or a permission that the bot does not have. " +
                    "This is not possible without explicitly having MANAGE_PERMISSIONS on this channel or ADMINISTRATOR on a role.");
        }


        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            this.overridesAdd.clear();

            //set all current overrides to-be-removed
            getChannel().getPermissionOverrides()
                .stream()
                .mapToLong(PermissionOverride::getIdLong)
                .forEach(overridesRem::add);

            //re-add all perm-overrides of syncSource
            syncSource.getPermissionOverrides().forEach(override -> {
                int type = override.isRoleOverride() ? PermOverrideData.ROLE_TYPE : PermOverrideData.MEMBER_TYPE;
                long id = override.getIdLong();

                this.overridesRem.remove(id);
                this.overridesAdd.put(id, new PermOverrideData(type, id, override.getAllowedRaw(), override.getDeniedRaw()));
            });

            set |= PERMISSION;
        });
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setRegion(@Nonnull Region region)
    {
        Checks.notNull(region, "Region");
        if (!getType().isAudio())
            throw new IllegalStateException("Can only change region on voice channels!");
        Checks.check(Region.VOICE_CHANNEL_REGIONS.contains(region), "Region is not usable for VoiceChannel region overrides!");
        this.region = region == Region.AUTOMATIC ? null : region.getKey();
        set |= REGION;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setParent(Category category)
    {
        if (category != null)
        {
            if (getType() == ChannelType.CATEGORY)
                throw new IllegalStateException("Cannot set the parent of a category");
            Checks.check(category.getGuild().equals(getGuild()), "Category is not from the same guild");
        }
        this.parent = category == null ? null : category.getId();
        set |= PARENT;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setPosition(int position)
    {
        this.position = position;
        set |= POSITION;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setTopic(String topic)
    {
        if (getType() != ChannelType.TEXT)
            throw new IllegalStateException("Can only set topic on text channels");
        if (topic != null)
            Checks.notLonger(topic, 1024, "Topic");
        this.topic = topic;
        set |= TOPIC;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setNSFW(boolean nsfw)
    {
        if (getType() != ChannelType.TEXT)
            throw new IllegalStateException("Can only set nsfw on text channels");
        this.nsfw = nsfw;
        set |= NSFW;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setSlowmode(int slowmode)
    {
        if (getType() != ChannelType.TEXT)
            throw new IllegalStateException("Can only set slowmode on text channels");
        Checks.check(slowmode <= TextChannel.MAX_SLOWMODE && slowmode >= 0, "Slowmode per user must be between 0 and %d (seconds)!", TextChannel.MAX_SLOWMODE);
        this.slowmode = slowmode;
        set |= SLOWMODE;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setUserLimit(int userLimit)
    {
        if (getType() != ChannelType.VOICE)
            throw new IllegalStateException("Can only set userlimit on voice channels");
        Checks.notNegative(userLimit, "Userlimit");
        Checks.check(userLimit <= 99, "Userlimit may not be greater than 99");
        this.userlimit = userLimit;
        set |= USERLIMIT;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setBitrate(int bitrate)
    {
        if (!getType().isAudio())
            throw new IllegalStateException("Can only set bitrate on voice channels");
        final int maxBitrate = getGuild().getMaxBitrate();
        Checks.check(bitrate >= 8000, "Bitrate must be greater or equal to 8000");
        Checks.check(bitrate <= maxBitrate, "Bitrate must be less or equal to %s", maxBitrate);
        this.bitrate = bitrate;
        set |= BITRATE;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl setNews(boolean news)
    {
        if (getType() != ChannelType.TEXT)
            throw new IllegalStateException("Can only set channel as news on text channels");
        if (news && !getGuild().getFeatures().contains("NEWS"))
            throw new IllegalStateException("Can only set channel as news for guilds with NEWS feature");
        this.news = news;
        set |= NEWS;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject frame = DataObject.empty().put("name", getChannel().getName());
        if (shouldUpdate(NAME))
            frame.put("name", name);
        if (shouldUpdate(POSITION))
            frame.put("position", position);
        if (shouldUpdate(TOPIC))
            frame.put("topic", topic);
        if (shouldUpdate(NSFW))
            frame.put("nsfw", nsfw);
        if (shouldUpdate(SLOWMODE))
            frame.put("rate_limit_per_user", slowmode);
        if (shouldUpdate(USERLIMIT))
            frame.put("user_limit", userlimit);
        if (shouldUpdate(BITRATE))
            frame.put("bitrate", bitrate);
        if (shouldUpdate(PARENT))
            frame.put("parent_id", parent);
        if (shouldUpdate(NEWS))
            frame.put("type", news ? 5 : 0);
        if (shouldUpdate(REGION))
            frame.put("rtc_region", region);
        withLock(lock, (lock) ->
        {
            if (shouldUpdate(PERMISSION))
                frame.put("permission_overwrites", getOverrides());
        });

        reset();
        return getRequestBody(frame);
    }

    @Override
    protected boolean checkPermissions()
    {
        final Member selfMember = getGuild().getSelfMember();
        GuildChannel channel = getChannel();
        if (!selfMember.hasPermission(channel, Permission.VIEW_CHANNEL))
            throw new MissingAccessException(channel, Permission.VIEW_CHANNEL);
        if (!selfMember.hasAccess(channel))
            throw new MissingAccessException(channel, Permission.VOICE_CONNECT);
        if (!selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(channel, Permission.MANAGE_CHANNEL);
        return super.checkPermissions();
    }

    protected Collection<PermOverrideData> getOverrides()
    {
        //note: overridesAdd and overridesRem are mutually disjoint
        TLongObjectHashMap<PermOverrideData> data = new TLongObjectHashMap<>(this.overridesAdd);

        AbstractChannelImpl<?,?> impl = (AbstractChannelImpl<?,?>) getChannel();
        impl.getOverrideMap().forEachEntry((id, override) ->
        {
            //removed by not adding them here, this data set overrides the existing one
            //we can use remove because it will be reset afterwards either way
            if (!overridesRem.remove(id) && !data.containsKey(id))
                data.put(id, new PermOverrideData(override));
            return true;
        });
        return data.valueCollection();
    }
}
