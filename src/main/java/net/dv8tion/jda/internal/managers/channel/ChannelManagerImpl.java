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

package net.dv8tion.jda.internal.managers.channel;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import net.dv8tion.jda.internal.managers.ManagerBase;
import net.dv8tion.jda.internal.requests.restaction.PermOverrideData;
import net.dv8tion.jda.internal.utils.ChannelUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked") //We do a lot of (M) and (T) casting that we know is correct but the compiler warns about.
public class ChannelManagerImpl<T extends GuildChannel, M extends ChannelManager<T, M>> extends ManagerBase<M> implements ChannelManager<T, M>
{
    protected T channel;

    protected final EnumSet<ChannelFlag> flags;
    protected ThreadChannel.AutoArchiveDuration autoArchiveDuration;
    protected List<BaseForumTag> availableTags;
    protected List<String> appliedTags;
    protected Emoji defaultReactionEmoji;
    protected int defaultLayout;
    protected int defaultSortOrder;
    protected ChannelType type;
    protected String name;
    protected String parent;
    protected String topic;
    protected String region;
    protected boolean nsfw;
    protected boolean archived;
    protected boolean locked;
    protected boolean invitable;
    protected int position;
    protected int slowmode;
    protected int defaultThreadSlowmode;
    protected int userlimit;
    protected int bitrate;

    protected final Object lock = new Object();
    protected final TLongObjectHashMap<PermOverrideData> overridesAdd;
    protected final TLongSet overridesRem;

    public ChannelManagerImpl(T channel)
    {
        super(channel.getJDA(), Route.Channels.MODIFY_CHANNEL.compile(channel.getId()));
        this.channel = channel;
        this.type = channel.getType();
        this.flags = channel.getFlags();

        if (isPermissionChecksEnabled())
            checkPermissions();
        this.overridesAdd = new TLongObjectHashMap<>();
        this.overridesRem = new TLongHashSet();
    }

    @Nonnull
    @Override
    public T getChannel()
    {
        T realChannel = (T) api.getGuildChannelById(channel.getType(), channel.getIdLong());
        if (realChannel != null)
            channel = realChannel;
        return channel;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public M reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & TYPE) == TYPE)
            this.type = this.channel.getType();
        if ((fields & PARENT) == PARENT)
            this.parent = null;
        if ((fields & TOPIC) == TOPIC)
            this.topic = null;
        if ((fields & REGION) == REGION)
            this.region = null;
        if ((fields & AVAILABLE_TAGS) == AVAILABLE_TAGS)
            this.availableTags = null;
        if ((fields & APPLIED_TAGS) == APPLIED_TAGS)
            this.appliedTags = null;
        if ((fields & DEFAULT_REACTION) == DEFAULT_REACTION)
            this.defaultReactionEmoji = null;
        if ((fields & PERMISSION) == PERMISSION)
        {
            withLock(lock, (lock) ->
            {
                this.overridesRem.clear();
                this.overridesAdd.clear();
            });
        }

        if ((fields & PINNED) == PINNED)
        {
            if (channel.getFlags().contains(ChannelFlag.PINNED))
                this.flags.add(ChannelFlag.PINNED);
            else
                this.flags.remove(ChannelFlag.PINNED);
        }

        if ((fields & REQUIRE_TAG) == REQUIRE_TAG)
        {
            if (channel.getFlags().contains(ChannelFlag.REQUIRE_TAG))
                this.flags.add(ChannelFlag.REQUIRE_TAG);
            else
                this.flags.remove(ChannelFlag.REQUIRE_TAG);
        }

        if ((fields & HIDE_MEDIA_DOWNLOAD_OPTIONS) == HIDE_MEDIA_DOWNLOAD_OPTIONS)
        {
            if (channel.getFlags().contains(ChannelFlag.HIDE_MEDIA_DOWNLOAD_OPTIONS))
                this.flags.add(ChannelFlag.HIDE_MEDIA_DOWNLOAD_OPTIONS);
            else
                this.flags.remove(ChannelFlag.HIDE_MEDIA_DOWNLOAD_OPTIONS);
        }

        return (M) this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public M reset(long... fields)
    {
        super.reset(fields);
        return (M) this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public M reset()
    {
        super.reset();
        this.name = null;
        this.type = this.channel.getType();
        this.parent = null;
        this.topic = null;
        this.region = null;
        this.availableTags = null;
        this.appliedTags = null;
        this.defaultReactionEmoji = null;
        this.flags.clear();
        this.flags.addAll(channel.getFlags());
        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            this.overridesAdd.clear();
        });
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M clearOverridesAdded()
    {
        withLock(lock, (lock) ->
        {
            this.overridesAdd.clear();
            if (this.overridesRem.isEmpty())
                set &= ~PERMISSION;
        });
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M clearOverridesRemoved()
    {
        withLock(lock, (lock) ->
        {
            this.overridesRem.clear();
            if (this.overridesAdd.isEmpty())
                set &= ~PERMISSION;
        });
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M putPermissionOverride(@Nonnull IPermissionHolder permHolder, long allow, long deny)
    {
        if (!(channel instanceof IPermissionContainer))
            throw new IllegalStateException("Can only set permissions on Channels that implement IPermissionContainer");

        Checks.notNull(permHolder, "PermissionHolder");
        Checks.check(permHolder.getGuild().equals(getGuild()), "PermissionHolder is not from the same Guild!");
        final long id = permHolder.getIdLong();
        final int type = permHolder instanceof Role ? PermOverrideData.ROLE_TYPE : PermOverrideData.MEMBER_TYPE;
        putPermissionOverride(new PermOverrideData(type, id, allow, deny));
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M putMemberPermissionOverride(long memberId, long allow, long deny)
    {
        putPermissionOverride(new PermOverrideData(PermOverrideData.MEMBER_TYPE, memberId, allow, deny));
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M putRolePermissionOverride(long roleId, long allow, long deny)
    {
        putPermissionOverride(new PermOverrideData(PermOverrideData.ROLE_TYPE, roleId, allow, deny));
        return (M) this;
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
    @CheckReturnValue
    public M removePermissionOverride(@Nonnull IPermissionHolder permHolder)
    {
        if (!(channel instanceof IPermissionContainer))
        {
            throw new IllegalStateException("Can only set permissions on Channels that implement IPermissionContainer");
        }

        Checks.notNull(permHolder, "PermissionHolder");
        Checks.check(permHolder.getGuild().equals(getGuild()), "PermissionHolder is not from the same Guild!");
        return (M) removePermissionOverride(permHolder.getIdLong());
    }

    @Nonnull
    @CheckReturnValue
    public M removePermissionOverride(final long id)
    {
        if (isPermissionChecksEnabled() && !getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS);
        withLock(lock, (lock) ->
        {
            this.overridesRem.add(id);
            this.overridesAdd.remove(id);
            set |= PERMISSION;
        });
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M sync(@Nonnull IPermissionContainer syncSource)
    {
        if (!(channel instanceof IPermissionContainer))
            throw new IllegalStateException("Can only set permissions on Channels that implement IPermissionContainer");

        Checks.notNull(syncSource, "SyncSource");
        Checks.check(getGuild().equals(syncSource.getGuild()), "Sync only works for channels of same guild");

        IPermissionContainer permChannel = (IPermissionContainer) channel;
        if (syncSource.equals(getChannel()))
            return (M) this;

        if (isPermissionChecksEnabled())
        {
            Member selfMember = getGuild().getSelfMember();
            if (!selfMember.hasPermission(permChannel, Permission.MANAGE_PERMISSIONS))
                throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS);

            if (!selfMember.canSync(permChannel, syncSource))
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
            permChannel.getPermissionOverrides()
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
        return (M) this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public M setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, Channel.MAX_NAME_LENGTH, "Name");
        this.name = name;
        set |= NAME;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setType(@Nonnull ChannelType type)
    {
        Checks.check(type == ChannelType.TEXT || type == ChannelType.NEWS, "Can only change ChannelType to TEXT or NEWS");

        if (this.type != ChannelType.TEXT && this.type != ChannelType.NEWS)
            throw new UnsupportedOperationException("Can only set ChannelType for TextChannel and NewsChannels");
        if (type == ChannelType.NEWS && !getGuild().getFeatures().contains("NEWS"))
            throw new IllegalStateException("Can only set ChannelType to NEWS for guilds with NEWS feature");

        this.type = type;

        //If we've just set the type to be what the channel type already is, then treat it as a reset, not a set.
        if (this.type == this.channel.getType())
            reset(TYPE);
        else
            set |= TYPE;


        //After the type is changed, be sure to clean up any properties that are exclusive to a specific channel type
        if (type != ChannelType.TEXT)
            reset(SLOWMODE);

        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setRegion(@Nonnull Region region)
    {
        Checks.notNull(region, "Region");
        if (!type.isAudio())
            throw new IllegalStateException("Can only change region on audio channels!");
        this.region = region == Region.AUTOMATIC ? null : region.getKey();
        set |= REGION;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setParent(Category category)
    {
        if (type == ChannelType.CATEGORY)
            throw new IllegalStateException("Cannot set the parent of a category");

        Checks.check(category == null || category.getGuild().equals(getGuild()), "Category is not from the same guild");

        this.parent = category == null ? null : category.getId();
        set |= PARENT;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setPosition(int position)
    {
        this.position = position;
        set |= POSITION;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setTopic(String topic)
    {
        Checks.checkSupportedChannelTypes(ChannelUtil.TOPIC_SUPPORTED, type, "topic");
        if (topic != null)
        {
            if (channel instanceof IPostContainer)
                Checks.notLonger(topic, IPostContainer.MAX_POST_CONTAINER_TOPIC_LENGTH, "Topic");
            else
                Checks.notLonger(topic, StandardGuildMessageChannel.MAX_TOPIC_LENGTH, "Topic");
        }
        this.topic = topic;
        set |= TOPIC;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setNSFW(boolean nsfw)
    {
        Checks.checkSupportedChannelTypes(ChannelUtil.NSFW_SUPPORTED, type, "NSFW (age-restriction)");
        this.nsfw = nsfw;
        set |= NSFW;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setSlowmode(int slowmode)
    {
        Checks.checkSupportedChannelTypes(ChannelUtil.SLOWMODE_SUPPORTED, type, "slowmode");
        Checks.check(slowmode <= ISlowmodeChannel.MAX_SLOWMODE && slowmode >= 0, "Slowmode per user must be between 0 and %d (seconds)!", ISlowmodeChannel.MAX_SLOWMODE);
        this.slowmode = slowmode;
        set |= SLOWMODE;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setDefaultThreadSlowmode(int slowmode)
    {
        Checks.check(channel instanceof IThreadContainer, "Cannot set default thread slowmode on channels of type %s!", channel.getType());
        Checks.check(slowmode <= ISlowmodeChannel.MAX_SLOWMODE && slowmode >= 0, "Slowmode per user must be between 0 and %d (seconds)!", ISlowmodeChannel.MAX_SLOWMODE);
        this.defaultThreadSlowmode = slowmode;
        set |= DEFAULT_THREAD_SLOWMODE;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setUserLimit(int userLimit)
    {
        Checks.notNegative(userLimit, "Userlimit");
        if (type == ChannelType.VOICE)
            Checks.check(userLimit <= VoiceChannel.MAX_USERLIMIT, "Userlimit may not be greater than %d for voice channels", VoiceChannel.MAX_USERLIMIT);
        else if (type == ChannelType.STAGE)
            Checks.check(userLimit <= StageChannel.MAX_USERLIMIT, "Userlimit may not be greater than %d for stage channels", StageChannel.MAX_USERLIMIT);
        else
            throw new IllegalStateException("Can only set userlimit on audio channels");
        this.userlimit = userLimit;
        set |= USERLIMIT;
        return (M) this;
    }

    @Nonnull
    @CheckReturnValue
    public M setBitrate(int bitrate)
    {
        if (!type.isAudio())
            throw new IllegalStateException("Can only set bitrate on voice channels");
        final int maxBitrate = getGuild().getMaxBitrate();
        Checks.check(bitrate >= 8000, "Bitrate must be greater or equal to 8000");
        Checks.check(bitrate <= maxBitrate, "Bitrate must be less or equal to %s", maxBitrate);
        this.bitrate = bitrate;
        set |= BITRATE;
        return (M) this;
    }

    public M setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration autoArchiveDuration)
    {
        Checks.notNull(autoArchiveDuration, "autoArchiveDuration");

        if (!type.isThread())
            throw new IllegalStateException("Can only set autoArchiveDuration on threads");

        this.autoArchiveDuration = autoArchiveDuration;
        set |= AUTO_ARCHIVE_DURATION;
        return (M) this;
    }

    public M setArchived(boolean archived)
    {
        if (!type.isThread())
            throw new IllegalStateException("Can only set archived on threads");

        if (isPermissionChecksEnabled()) {
            ThreadChannel thread = (ThreadChannel) channel;
            if (!thread.isOwner())
                checkPermission(Permission.MANAGE_THREADS, "Cannot unarchive a thread without MANAGE_THREADS if not the thread owner");

            if (thread.isLocked())
                checkPermission(Permission.MANAGE_THREADS, "Cannot unarchive a thread that is locked without MANAGE_THREADS");
        }

        this.archived = archived;
        set |= ARCHIVED;
        return (M) this;
    }

    public M setLocked(boolean locked)
    {
        if (!type.isThread())
            throw new IllegalStateException("Can only set locked on threads");

        if (isPermissionChecksEnabled()) {
            checkPermission(Permission.MANAGE_THREADS, "Cannot modified a thread's locked status without MANAGE_THREADS");
        }

        this.locked = locked;
        set |= LOCKED;
        return (M) this;
    }

    public M setInvitable(boolean invitable)
    {
        if (type != ChannelType.GUILD_PRIVATE_THREAD)
            throw new IllegalStateException("Can only set invitable on private threads.");

        if (isPermissionChecksEnabled()) {
            ThreadChannel thread = (ThreadChannel) channel;
            if (!thread.isOwner())
                checkPermission(Permission.MANAGE_THREADS, "Cannot modify a thread's invitable status without MANAGE_THREADS if not the thread owner");
        }

        this.invitable = invitable;
        set |= INVITEABLE;
        return (M) this;
    }

    public M setPinned(boolean pinned)
    {
        if (!type.isThread())
            throw new IllegalStateException("Can only pin threads.");
        if (pinned)
            flags.add(ChannelFlag.PINNED);
        else
            flags.remove(ChannelFlag.PINNED);
        set |= PINNED;
        return (M) this;
    }

    public M setTagRequired(boolean requireTag)
    {
        if (!(channel instanceof IPostContainer))
            throw new IllegalStateException("Can only set tag required flag on forum/media channels.");
        if (requireTag)
            flags.add(ChannelFlag.REQUIRE_TAG);
        else
            flags.remove(ChannelFlag.REQUIRE_TAG);
        set |= REQUIRE_TAG;
        return (M) this;
    }

    public M setHideMediaDownloadOption(boolean hideOption)
    {
        if (!(channel instanceof MediaChannel))
            throw new IllegalStateException("Can only set hide media download flag on media channels.");
        if (hideOption)
            flags.add(ChannelFlag.HIDE_MEDIA_DOWNLOAD_OPTIONS);
        else
            flags.remove(ChannelFlag.HIDE_MEDIA_DOWNLOAD_OPTIONS);
        set |= HIDE_MEDIA_DOWNLOAD_OPTIONS;
        return (M) this;
    }

    public M setAvailableTags(List<? extends BaseForumTag> tags)
    {
        if (!(channel instanceof IPostContainer))
            throw new IllegalStateException("Can only set available tags on forum/media channels.");
        Checks.noneNull(tags, "Available Tags");
        this.availableTags = new ArrayList<>(tags);
        set |= AVAILABLE_TAGS;
        return (M) this;
    }

    public M setAppliedTags(Collection<? extends ForumTagSnowflake> tags)
    {
        if (type != ChannelType.GUILD_PUBLIC_THREAD)
            throw new IllegalStateException("Can only set applied tags on forum post thread channels.");
        Checks.noneNull(tags, "Applied Tags");
        Checks.check(tags.size() <= IPostContainer.MAX_POST_TAGS, "Cannot apply more than %d tags to a post thread!", ForumChannel.MAX_POST_TAGS);
        ThreadChannel thread = (ThreadChannel) getChannel();
        IThreadContainerUnion parentChannel = thread.getParentChannel();
        if (!(parentChannel instanceof IPostContainer))
            throw new IllegalStateException("Cannot apply tags to threads outside of forum/media channels.");
        if (tags.isEmpty() && parentChannel.asForumChannel().isTagRequired())
            throw new IllegalArgumentException("Cannot remove all tags from a forum post which requires at least one tag! See IPostContainer#isRequireTag()");
        this.appliedTags = tags.stream().map(ISnowflake::getId).collect(Collectors.toList());
        set |= APPLIED_TAGS;
        return (M) this;
    }

    public M setDefaultReaction(Emoji emoji)
    {
        if (!(channel instanceof IPostContainer))
            throw new IllegalStateException("Can only set default reaction on forum/media channels.");
        this.defaultReactionEmoji = emoji;
        set |= DEFAULT_REACTION;
        return (M) this;
    }

    public M setDefaultLayout(ForumChannel.Layout layout)
    {
        if (type != ChannelType.FORUM)
            throw new IllegalStateException("Can only set default layout on forum channels.");
        Checks.notNull(layout, "layout");
        if (layout == ForumChannel.Layout.UNKNOWN)
            throw new IllegalStateException("Layout type cannot be UNKNOWN.");
        this.defaultLayout = layout.getKey();
        set |= DEFAULT_LAYOUT;
        return (M) this;
    }

    public M setDefaultSortOrder(IPostContainer.SortOrder sortOrder)
    {
        if (!(channel instanceof IPostContainer))
            throw new IllegalStateException("Can only set default layout on forum/media channels.");
        Checks.notNull(sortOrder, "SortOrder");
        if (sortOrder == IPostContainer.SortOrder.UNKNOWN)
            throw new IllegalStateException("SortOrder type cannot be UNKNOWN.");
        this.defaultSortOrder = sortOrder.getKey();
        set |= DEFAULT_SORT_ORDER;
        return (M) this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject frame = DataObject.empty();
        if (shouldUpdate(NAME))
            frame.put("name", name);
        if (shouldUpdate(TYPE))
            frame.put("type", type.getId());
        if (shouldUpdate(POSITION))
            frame.put("position", position);
        if (shouldUpdate(TOPIC))
            frame.put("topic", topic);
        if (shouldUpdate(NSFW))
            frame.put("nsfw", nsfw);
        if (shouldUpdate(SLOWMODE))
            frame.put("rate_limit_per_user", slowmode);
        if (shouldUpdate(DEFAULT_THREAD_SLOWMODE))
            frame.put("default_thread_rate_limit_per_user", defaultThreadSlowmode);
        if (shouldUpdate(USERLIMIT))
            frame.put("user_limit", userlimit);
        if (shouldUpdate(BITRATE))
            frame.put("bitrate", bitrate);
        if (shouldUpdate(PARENT))
            frame.put("parent_id", parent);
        if (shouldUpdate(REGION))
            frame.put("rtc_region", region);
        if (shouldUpdate(AUTO_ARCHIVE_DURATION))
            frame.put("auto_archive_duration", autoArchiveDuration.getMinutes());
        if (shouldUpdate(ARCHIVED))
            frame.put("archived", archived);
        if (shouldUpdate(LOCKED))
            frame.put("locked", locked);
        if (shouldUpdate(INVITEABLE))
            frame.put("invitable", invitable);
        if (shouldUpdate(AVAILABLE_TAGS))
            frame.put("available_tags", DataArray.fromCollection(availableTags));
        if (shouldUpdate(APPLIED_TAGS))
            frame.put("applied_tags", DataArray.fromCollection(appliedTags));
        if (shouldUpdate(PINNED | REQUIRE_TAG | HIDE_MEDIA_DOWNLOAD_OPTIONS))
            frame.put("flags", ChannelFlag.getRaw(flags));
        if (shouldUpdate(DEFAULT_REACTION))
        {
            if (defaultReactionEmoji instanceof CustomEmoji)
                frame.put("default_reaction_emoji", DataObject.empty().put("emoji_id", ((CustomEmoji) defaultReactionEmoji).getId()));
            else if (defaultReactionEmoji instanceof UnicodeEmoji)
                frame.put("default_reaction_emoji", DataObject.empty().put("emoji_name", defaultReactionEmoji.getName()));
            else
                frame.put("default_reaction_emoji", null);
        }
        if (shouldUpdate(DEFAULT_LAYOUT))
            frame.put("default_forum_layout", defaultLayout);
        if (shouldUpdate(DEFAULT_SORT_ORDER))
            frame.put("default_sort_order", defaultSortOrder);

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

        Checks.checkAccess(selfMember, channel);
        ((GuildChannelMixin<?>) channel).checkCanManage();

        return super.checkPermissions();
    }

    protected void checkPermission(Permission permission, String errMessage)
    {
        if (!getGuild().getSelfMember().hasPermission(getChannel(), permission))
            throw new InsufficientPermissionException(getChannel(), permission, errMessage);
    }

    protected Collection<PermOverrideData> getOverrides()
    {
        //note: overridesAdd and overridesRem are mutually disjoint
        TLongObjectHashMap<PermOverrideData> data = new TLongObjectHashMap<>(this.overridesAdd);

        IPermissionContainerMixin<?> impl = (IPermissionContainerMixin<?>) getChannel();
        impl.getPermissionOverrideMap().forEachEntry((id, override) ->
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
