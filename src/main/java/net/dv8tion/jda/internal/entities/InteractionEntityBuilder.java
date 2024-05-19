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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.PrivateChannelImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.detached.*;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import net.dv8tion.jda.internal.entities.detached.DetachedGuildImpl;
import net.dv8tion.jda.internal.entities.detached.DetachedMemberImpl;
import net.dv8tion.jda.internal.entities.detached.DetachedRoleImpl;
import net.dv8tion.jda.internal.interactions.ChannelInteractionPermissions;
import net.dv8tion.jda.internal.interactions.MemberInteractionPermissions;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class InteractionEntityBuilder extends AbstractEntityBuilder
{
    private static final Logger LOG = JDALogger.getLog(InteractionEntityBuilder.class);

    private final EntityBuilder entityBuilder = api.getEntityBuilder();
    private final long interactionChannelId;
    private final long interactionUserId;

    public InteractionEntityBuilder(JDAImpl api, long interactionChannelId, long interactionUserId)
    {
        super(api);
        this.interactionChannelId = interactionChannelId;
        this.interactionUserId = interactionUserId;
    }

    public Guild getOrCreateGuild(DataObject guildJson)
    {
        final long guildId = guildJson.getUnsignedLong("id");
        final Guild guild = api.getGuildById(guildId);
        if (guild != null)
            return guild;

        final Optional<DataArray> featuresArray = guildJson.optArray("features");
        final String locale = guildJson.getString("preferred_locale", "en-US");

        final DetachedGuildImpl detachedGuild = new DetachedGuildImpl(api, guildId);
        detachedGuild.setLocale(DiscordLocale.from(locale));
        detachedGuild.setFeatures(featuresArray.map(array ->
                array.stream(DataArray::getString)
                        .map(String::intern) // Prevent allocating the same feature string over and over
                        .collect(Collectors.toSet())
        ).orElse(Collections.emptySet()));

        return detachedGuild;
    }

    public GroupChannel createGroupChannel(DataObject channelData)
    {
        return new DetachedGroupChannelImpl(api, channelData.getLong("id"))
                .setLatestMessageIdLong(channelData.getLong("last_message_id", 0L))
                .setName(channelData.getString("name", ""))
                .setOwnerId(channelData.getLong("owner_id"))
                .setIcon(channelData.getString("icon", null));
    }

    public GuildChannel createGuildChannel(@Nonnull Guild guild, DataObject channelData)
    {
        final ChannelType channelType = ChannelType.fromId(channelData.getInt("type"));
        switch (channelType)
        {
        case TEXT:
            return createTextChannel(guild, channelData);
        case NEWS:
            return createNewsChannel(guild, channelData);
        case STAGE:
            return createStageChannel(guild, channelData);
        case VOICE:
            return createVoiceChannel(guild, channelData);
        case CATEGORY:
            return createCategory(guild, channelData);
        case FORUM:
            return createForumChannel(guild, channelData);
        case MEDIA:
            return createMediaChannel(guild, channelData);
        default:
            LOG.debug("Cannot create channel for type " + channelData.getInt("type"));
            return null;
        }
    }

    public Category createCategory(@Nonnull Guild guild, DataObject json)
    {
        if (!guild.isDetached())
            return guild.getCategoryById(json.getLong("id"));

        final long id = json.getLong("id");
        final DetachedCategoryImpl channel = new DetachedCategoryImpl(id, (DetachedGuildImpl) guild);
        configureCategory(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public TextChannel createTextChannel(@Nonnull Guild guild, DataObject json)
    {
        if (!guild.isDetached())
            return guild.getTextChannelById(json.getLong("id"));

        final long id = json.getLong("id");
        DetachedTextChannelImpl channel = new DetachedTextChannelImpl(id, (DetachedGuildImpl) guild);
        configureTextChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public NewsChannel createNewsChannel(@Nonnull Guild guild, DataObject json)
    {
        if (!guild.isDetached())
            return guild.getNewsChannelById(json.getLong("id"));

        final long id = json.getLong("id");
        DetachedNewsChannelImpl channel = new DetachedNewsChannelImpl(id, (DetachedGuildImpl) guild);
        configureNewsChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public VoiceChannel createVoiceChannel(@Nonnull Guild guild, DataObject json)
    {
        if (!guild.isDetached())
            return guild.getVoiceChannelById(json.getLong("id"));

        final long id = json.getLong("id");
        DetachedVoiceChannelImpl channel = new DetachedVoiceChannelImpl(id, (DetachedGuildImpl) guild);
        configureVoiceChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public StageChannel createStageChannel(@Nonnull Guild guild, DataObject json)
    {
        if (!guild.isDetached())
            return guild.getStageChannelById(json.getLong("id"));
        final long id = json.getLong("id");
        final DetachedStageChannelImpl channel = new DetachedStageChannelImpl(id, (DetachedGuildImpl) guild);
        configureStageChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public MediaChannel createMediaChannel(@Nonnull Guild guild, DataObject json)
    {
        if (!guild.isDetached())
            return guild.getMediaChannelById(json.getLong("id"));

        final long id = json.getLong("id");
        final DetachedMediaChannelImpl channel = new DetachedMediaChannelImpl(id, (DetachedGuildImpl) guild);
        configureMediaChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public ThreadChannel createThreadChannel(@Nonnull Guild guild, DataObject json)
    {
        if (!guild.isDetached())
        {
            final ThreadChannel threadChannel = guild.getThreadChannelById(json.getLong("id"));
            if (threadChannel != null)
                return threadChannel;
            else
                return entityBuilder.createThreadChannel((GuildImpl) guild, json, guild.getIdLong(), false);
        }

        final long id = json.getUnsignedLong("id");
        final ChannelType type = ChannelType.fromId(json.getInt("type"));
        DetachedThreadChannelImpl channel = new DetachedThreadChannelImpl(id, (DetachedGuildImpl) guild, type);
        configureThreadChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public ForumChannel createForumChannel(@Nonnull Guild guild, DataObject json)
    {
        if (!guild.isDetached())
            return guild.getForumChannelById(json.getLong("id"));

        final long id = json.getLong("id");
        final DetachedForumChannelImpl channel = new DetachedForumChannelImpl(id, (DetachedGuildImpl) guild);
        configureForumChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    private void configureChannelInteractionPermissions(IInteractionPermissionMixin<?> channel, DataObject json)
    {
        channel.setInteractionPermissions(new ChannelInteractionPermissions(interactionUserId, json.getLong("permissions")));
    }

    public Member createMember(@Nonnull Guild guild, DataObject memberJson)
    {
        if (!guild.isDetached())
            return entityBuilder.createMember((GuildImpl) guild, memberJson);

        User user = entityBuilder.createUser(memberJson.getObject("user"));
        DetachedMemberImpl member = new DetachedMemberImpl((DetachedGuildImpl) guild, user);
        configureMember(memberJson, member);

        // Absent outside interactions and in message mentions
        if (memberJson.hasKey("permissions"))
            member.setInteractionPermissions(new MemberInteractionPermissions(interactionChannelId, memberJson.getLong("permissions")));

        return member;
    }

    public Role createRole(@Nonnull Guild guild, DataObject roleJson)
    {
        if (!guild.isDetached())
            return guild.getRoleById(roleJson.getLong("id"));

        final long id = roleJson.getLong("id");
        DetachedRoleImpl role = new DetachedRoleImpl(id, (DetachedGuildImpl) guild);
        configureRole(roleJson, role, id);
        return role;
    }

    public PrivateChannel createPrivateChannel(DataObject json, User user)
    {
        final PrivateChannelImpl channel = new PrivateChannelImpl(getJDA(), json.getUnsignedLong("id"), user);
        configurePrivateChannel(json, channel);
        return channel;
    }
}
