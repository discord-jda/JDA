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

package net.dv8tion.jda.internal.entities.channel.concrete;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.managers.channel.concrete.ForumChannelManager;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.ForumChannelMixin;
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl;
import net.dv8tion.jda.internal.managers.channel.concrete.ForumChannelManagerImpl;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class ForumChannelImpl extends AbstractGuildChannelImpl<ForumChannelImpl>
    implements ForumChannel,
        GuildChannelUnion,
        ForumChannelMixin<ForumChannelImpl>
{
    private final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();
    private final SortedSnowflakeCacheViewImpl<ForumTag> tagCache = new SortedSnowflakeCacheViewImpl<>(ForumTag.class, ForumTag::getName, Comparator.naturalOrder());

    private Emoji defaultReaction;
    private String topic;
    private long parentCategoryId;
    private boolean nsfw = false;
    private int position;
    private int flags;
    private int slowmode;
    private int defaultSortOrder;
    private int defaultLayout;
    protected int defaultThreadSlowmode;

    public ForumChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public boolean isDetached()
    {
        return false;
    }

    @Nonnull
    @Override
    public GuildImpl getGuild()
    {
        return (GuildImpl) super.getGuild();
    }

    @Nonnull
    @Override
    public ForumChannelManager getManager()
    {
        return new ForumChannelManagerImpl(this);
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return getGuild().getMembers()
                .stream()
                .filter(m -> m.hasPermission(this, Permission.VIEW_CHANNEL))
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public EnumSet<ChannelFlag> getFlags()
    {
        return ChannelFlag.fromRaw(flags);
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheViewImpl<ForumTag> getAvailableTagCache()
    {
        return tagCache;
    }

    @Override
    public TLongObjectMap<PermissionOverride> getPermissionOverrideMap()
    {
        return overrides;
    }

    @Override
    public boolean isNSFW()
    {
        return nsfw;
    }

    @Override
    public int getPositionRaw()
    {
        return position;
    }

    @Override
    public long getParentCategoryIdLong()
    {
        return parentCategoryId;
    }

    @Override
    public int getSlowmode()
    {
        return slowmode;
    }

    @Override
    public String getTopic()
    {
        return topic;
    }

    @Override
    public EmojiUnion getDefaultReaction()
    {
        return (EmojiUnion) defaultReaction;
    }

    @Override
    public int getDefaultThreadSlowmode()
    {
        return defaultThreadSlowmode;
    }

    @Nonnull
    @Override
    public SortOrder getDefaultSortOrder()
    {
        return SortOrder.fromKey(defaultSortOrder);
    }

    @Nonnull
    @Override
    public Layout getDefaultLayout()
    {
        return Layout.fromKey(defaultLayout);
    }

    public int getRawFlags()
    {
        return flags;
    }

    public int getRawSortOrder()
    {
        return defaultSortOrder;
    }

    public int getRawLayout()
    {
        return defaultLayout;
    }

    // Setters

    @Override
    public ForumChannelImpl setParentCategory(long parentCategoryId)
    {
        this.parentCategoryId = parentCategoryId;
        return this;
    }

    @Override
    public ForumChannelImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    @Override
    public ForumChannelImpl setDefaultThreadSlowmode(int defaultThreadSlowmode)
    {
        this.defaultThreadSlowmode = defaultThreadSlowmode;
        return this;
    }

    @Override
    public ForumChannelImpl setNSFW(boolean nsfw)
    {
        this.nsfw = nsfw;
        return this;
    }

    @Override
    public ForumChannelImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public ForumChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    @Override
    public ForumChannelImpl setFlags(int flags)
    {
        this.flags = flags;
        return this;
    }

    @Override
    public ForumChannelImpl setDefaultReaction(DataObject emoji)
    {
        if (emoji != null && !emoji.isNull("emoji_id"))
            this.defaultReaction = new CustomEmojiImpl("", emoji.getUnsignedLong("emoji_id"), false);
        else if (emoji != null && !emoji.isNull("emoji_name"))
            this.defaultReaction = Emoji.fromUnicode(emoji.getString("emoji_name"));
        else
            this.defaultReaction = null;
        return this;
    }

    @Override
    public ForumChannelImpl setDefaultSortOrder(int defaultSortOrder)
    {
        this.defaultSortOrder = defaultSortOrder;
        return this;
    }

    @Override
    public ForumChannelImpl setDefaultLayout(int layout)
    {
        this.defaultLayout = layout;
        return this;
    }
}
