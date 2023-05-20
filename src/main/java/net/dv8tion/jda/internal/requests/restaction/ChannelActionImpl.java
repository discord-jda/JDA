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
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class ChannelActionImpl<T extends GuildChannel> extends AuditableRestActionImpl<T> implements ChannelAction<T>
{
    private static final EnumSet<ChannelType> SLOWMODE_SUPPORTED = EnumSet.of(ChannelType.TEXT, ChannelType.FORUM,
                                                                              ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PRIVATE_THREAD);
    private static final EnumSet<ChannelType> NSFW_SUPPORTED = EnumSet.of(ChannelType.TEXT, ChannelType.VOICE, ChannelType.FORUM, ChannelType.NEWS);
    private static final EnumSet<ChannelType> TOPIC_SUPPORTED = EnumSet.of(ChannelType.TEXT, ChannelType.FORUM, ChannelType.NEWS);

    protected final TLongObjectMap<PermOverrideData> overrides = new TLongObjectHashMap<>();
    protected final Guild guild;
    protected final Class<T> clazz;
    protected final ChannelType type;

    // --all channels--
    protected String name;
    protected Category parent;
    protected Integer position;

    // --forum only--
    protected List<? extends BaseForumTag> availableTags;
    protected Emoji defaultReactionEmoji;

    // --text/forum/voice only--
    protected Integer slowmode = null;

    // --text/forum/voice/news--
    protected String topic = null;
    protected Boolean nsfw = null;

    // --voice only--
    protected Integer userlimit = null;

    // --audio only--
    protected Integer bitrate = null;
    protected Region region = null;

    // --forum only--
    protected Integer defaultLayout = null;

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
    public ChannelActionImpl<T> reason(@Nullable String reason)
    {
        return (ChannelActionImpl<T>) super.reason(reason);
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
        Checks.notLonger(name, Channel.MAX_NAME_LENGTH, "Name");
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setParent(Category category)
    {
        if (category != null)
        {
            Checks.check(category.getGuild().equals(guild), "Category is not from same guild!");
            if (type == ChannelType.CATEGORY)
                throw new UnsupportedOperationException("Cannot set a parent Category on a Category");
        }

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
        Checks.checkSupportedChannelTypes(TOPIC_SUPPORTED, type, "Topic");
        if (topic != null)
        {
            if (type == ChannelType.FORUM)
                Checks.notLonger(topic, ForumChannel.MAX_FORUM_TOPIC_LENGTH, "Topic");
            else
                Checks.notLonger(topic, StandardGuildMessageChannel.MAX_TOPIC_LENGTH, "Topic");
        }
        this.topic = topic;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setNSFW(boolean nsfw)
    {
        Checks.checkSupportedChannelTypes(NSFW_SUPPORTED, type, "NSFW (age-restricted)");
        this.nsfw = nsfw;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setSlowmode(int slowmode)
    {
        Checks.checkSupportedChannelTypes(SLOWMODE_SUPPORTED, type, "Slowmode");
        Checks.check(slowmode <= ISlowmodeChannel.MAX_SLOWMODE && slowmode >= 0, "Slowmode must be between 0 and %d (seconds)!", ISlowmodeChannel.MAX_SLOWMODE);
        this.slowmode = slowmode;
        return this;
    }

    @Nonnull
    @Override
    public ChannelAction<T> setDefaultReaction(@Nullable Emoji emoji)
    {
        if (type != ChannelType.FORUM)
            throw new UnsupportedOperationException("Can only set default reaction emoji on a ForumChannel!");
        this.defaultReactionEmoji = emoji;
        return this;
    }

    @Nonnull
    @Override
    public ChannelAction<T> setDefaultLayout(@Nonnull ForumChannel.Layout layout)
    {
        Checks.checkSupportedChannelTypes(EnumSet.of(ChannelType.FORUM), type, "Default Layout");
        Checks.notNull(layout, "layout");
        if (layout == ForumChannel.Layout.UNKNOWN)
            throw new IllegalStateException("Layout type cannot be UNKNOWN.");
        this.defaultLayout = layout.getKey();
        return this;
    }

    @Nonnull
    @Override
    public ChannelAction<T> setAvailableTags(@Nonnull List<? extends BaseForumTag> tags)
    {
        if (type != ChannelType.FORUM)
            throw new UnsupportedOperationException("Can only set available tags on a ForumChannel!");
        Checks.noneNull(tags, "Tags");
        this.availableTags = new ArrayList<>(tags);
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
        if (userlimit != null)
        {
            Checks.notNegative(userlimit, "Userlimit");
            if (type == ChannelType.VOICE)
                Checks.check(userlimit <= VoiceChannel.MAX_USERLIMIT, "Userlimit may not be greater than %d for voice channels", VoiceChannel.MAX_USERLIMIT);
            else if (type == ChannelType.STAGE)
                Checks.check(userlimit <= StageChannel.MAX_USERLIMIT, "Userlimit may not be greater than %d for stage channels", StageChannel.MAX_USERLIMIT);
            else
                throw new IllegalStateException("Can only set userlimit on audio channels");
        }
        this.userlimit = userlimit;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setRegion(@Nullable Region region)
    {
        if (!type.isAudio())
            throw new UnsupportedOperationException("Can only set the region for AudioChannels!");
        this.region = region;
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

        //Text and Forum
        if (slowmode != null)
            object.put("rate_limit_per_user", slowmode);

        //Text, Forum, and News
        if (topic != null && !topic.isEmpty())
            object.put("topic", topic);
        if (nsfw != null)
            object.put("nsfw", nsfw);

        //Forum only
        if (defaultReactionEmoji instanceof CustomEmoji)
            object.put("default_reaction_emoji", DataObject.empty().put("emoji_id", ((CustomEmoji) defaultReactionEmoji).getId()));
        else if (defaultReactionEmoji instanceof UnicodeEmoji)
            object.put("default_reaction_emoji", DataObject.empty().put("emoji_name", defaultReactionEmoji.getName()));
        if (availableTags != null)
            object.put("available_tags", DataArray.fromCollection(availableTags));
        if (defaultLayout != null)
            object.put("default_forum_layout", defaultLayout);

        //Voice only
        if (userlimit != null)
            object.put("user_limit", userlimit);

        //Voice and Stage
        if (bitrate != null)
            object.put("bitrate", bitrate);
        if (region != null)
            object.put("rtc_region", region.getKey());

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
            case FORUM:
                channel = builder.createForumChannel(response.getObject(), guild.getIdLong());
                break;
            default:
                request.onFailure(new IllegalStateException("Created channel of unknown type!"));
                return;
        }
        request.onSuccess(clazz.cast(channel));
    }
}
