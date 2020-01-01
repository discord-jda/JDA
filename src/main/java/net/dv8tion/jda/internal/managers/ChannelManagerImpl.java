/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.AbstractChannelImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.PermOverrideData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;

public class ChannelManagerImpl extends ManagerBase<ChannelManager> implements ChannelManager
{
    protected final SnowflakeReference<GuildChannel> channel;

    protected String name;
    protected String parent;
    protected String topic;
    protected int position;
    protected boolean nsfw;
    protected int slowmode;
    protected int userlimit;
    protected int bitrate;

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
        this.channel = new SnowflakeReference<>(channel, (channelId) -> jda.getGuildChannelById(type, channelId));
        if (isPermissionChecksEnabled())
            checkPermissions();
        this.overridesAdd = new TLongObjectHashMap<>();
        this.overridesRem = new TLongHashSet();
    }

    @Nonnull
    @Override
    public GuildChannel getChannel()
    {
        return channel.resolve();
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
        if (isPermissionChecksEnabled() && !getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS);
        final long id = getId(permHolder);
        final int type = permHolder instanceof Role ? PermOverrideData.ROLE_TYPE : PermOverrideData.MEMBER_TYPE;
        withLock(lock, (lock) ->
        {
            this.overridesRem.remove(id);
            this.overridesAdd.put(id, new PermOverrideData(type, id, allow, deny));
            set |= PERMISSION;
        });
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelManagerImpl removePermissionOverride(@Nonnull IPermissionHolder permHolder)
    {
        Checks.notNull(permHolder, "PermissionHolder");
        Checks.check(permHolder.getGuild().equals(getGuild()), "PermissionHolder is not from the same Guild!");
        if (isPermissionChecksEnabled() && !getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS);
        final long id = getId(permHolder);
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

        if(syncSource.equals(getChannel()))
            return this;

        if (isPermissionChecksEnabled() && !getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS);

        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            this.overridesAdd.clear();

            //set all current overrides to-be-removed
            getChannel().getPermissionOverrides().forEach(permO ->
                this.overridesRem.add(getId(permO.isRoleOverride() ? permO.getRole() : permO.getMember()))
            );

            //re-add all perm-overrides of syncSource
            syncSource.getPermissionOverrides().forEach(permO ->
            {
                int type = permO.isRoleOverride() ? PermOverrideData.ROLE_TYPE : PermOverrideData.MEMBER_TYPE;
                long id = getId(permO.isRoleOverride() ? permO.getRole() : permO.getMember());

                this.overridesRem.remove(id);
                this.overridesAdd.put(id, new PermOverrideData(type, id, permO.getAllowedRaw(), permO.getDeniedRaw()));
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
        Checks.check(name.length() > 0 && name.length() <= 100, "Name must be between 1-100 characters long");
        if (getType() == ChannelType.TEXT)
            Checks.noWhitespace(name, "Name");
        this.name = name;
        set |= NAME;
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
        Checks.check(topic == null || topic.length() <= 1024, "Topic must be less or equal to 1024 characters in length");
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
        if (getType() != ChannelType.VOICE)
            throw new IllegalStateException("Can only set bitrate on voice channels");
        final int maxBitrate = getGuild().getMaxBitrate();
        Checks.check(bitrate >= 8000, "Bitrate must be greater or equal to 8000");
        Checks.check(bitrate <= maxBitrate, "Bitrate must be less or equal to %s", maxBitrate);
        this.bitrate = bitrate;
        set |= BITRATE;
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
        if (!selfMember.hasPermission(getChannel(), Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_CHANNEL);
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

    protected long getId(IPermissionHolder holder)
    {
        if (holder instanceof Role)
            return ((Role) holder).getIdLong();
        else
            return ((Member) holder).getUser().getIdLong();
    }
}
