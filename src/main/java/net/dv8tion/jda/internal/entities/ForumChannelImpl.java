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

package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.managers.channel.concrete.ForumChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.ForumPostAction;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IAgeRestrictedChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IWebhookContainerMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IThreadContainerMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ITopicChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.StandardGuildChannelMixin;
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl;
import net.dv8tion.jda.internal.managers.channel.concrete.ForumChannelManagerImpl;
import net.dv8tion.jda.internal.requests.restaction.ForumPostActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class ForumChannelImpl extends AbstractGuildChannelImpl<ForumChannelImpl>
        implements ForumChannel,
                   GuildChannelUnion,
                   StandardGuildChannelMixin<ForumChannelImpl>,
                   IAgeRestrictedChannelMixin<ForumChannelImpl>,
                   ISlowmodeChannelMixin<ForumChannelImpl>,
                   IWebhookContainerMixin<ForumChannelImpl>,
                   IThreadContainerMixin<ForumChannelImpl>,
                   ITopicChannelMixin<ForumChannelImpl>
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
//    private int defaultSortOrder;
    private int defaultLayout;
    protected int defaultThreadSlowmode;

    public ForumChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
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
        return Collections.unmodifiableList(getGuild().getMembers()
                .stream()
                .filter(m -> m.hasPermission(this, Permission.VIEW_CHANNEL))
                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public ChannelAction<ForumChannel> createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        ChannelAction<ForumChannel> action = guild.createForumChannel(name)
                .setNSFW(nsfw)
                .setTopic(topic)
                .setSlowmode(slowmode)
                .setAvailableTags(getAvailableTags())
                .setDefaultLayout(Layout.fromKey(defaultLayout));
        if (defaultReaction instanceof UnicodeEmoji)
            action.setDefaultReaction(defaultReaction);
        if (guild.equals(getGuild()))
        {
            Category parent = getParentCategory();
            action.setDefaultReaction(defaultReaction);
            if (parent != null)
                action.setParent(parent);
            for (PermissionOverride o : overrides.valueCollection())
            {
                if (o.isMemberOverride())
                    action.addMemberPermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                else
                    action.addRolePermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
            }
        }
        return action;
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

//    @Nonnull
//    @Override
//    public SortOrder getDefaultSortOrder()
//    {
//        return SortOrder.fromKey(defaultSortOrder);
//    }

    @Nonnull
    @Override
    public Layout getDefaultLayout()
    {
        return Layout.fromKey(defaultLayout);
    }

    @Nonnull
    @Override
    public ForumPostAction createForumPost(@Nonnull String name, @Nonnull MessageCreateData message)
    {
        checkPermission(Permission.MESSAGE_SEND);
        return new ForumPostActionImpl(this, name, new MessageCreateBuilder().applyData(message));
    }

    @Nonnull
    @Override
    public ThreadChannelAction createThreadChannel(@Nonnull String name)
    {
        throw new UnsupportedOperationException("You cannot create threads without a message payload in forum channels! Use createForumPost(...) instead.");
    }

    @Nonnull
    @Override
    public ThreadChannelAction createThreadChannel(@Nonnull String name, @Nonnull String messageId)
    {
        throw new UnsupportedOperationException("You cannot create threads without a message payload in forum channels! Use createForumPost(...) instead.");
    }

    public int getRawFlags()
    {
        return flags;
    }

//    public int getRawSortOrder()
//    {
//        return defaultSortOrder;
//    }

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

    public ForumChannelImpl setNSFW(boolean nsfw)
    {
        this.nsfw = nsfw;
        return this;
    }

    public ForumChannelImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }

    public ForumChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    public ForumChannelImpl setFlags(int flags)
    {
        this.flags = flags;
        return this;
    }

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

//    public ForumChannelImpl setDefaultSortOrder(int defaultSortOrder)
//    {
//        this.defaultSortOrder = defaultSortOrder;
//        return this;
//    }

    public ForumChannelImpl setDefaultLayout(int layout)
    {
        this.defaultLayout = layout;
        return this;
    }
}
