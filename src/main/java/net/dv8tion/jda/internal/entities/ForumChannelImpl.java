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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.managers.channel.concrete.ForumChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.IThreadContainerMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.StandardGuildChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.ForumChannelManagerImpl;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ForumChannelImpl extends AbstractGuildChannelImpl<ForumChannelImpl>
        implements ForumChannel,
                   GuildChannelUnion,
                   StandardGuildChannelMixin<ForumChannelImpl>,
                   IThreadContainerMixin<ForumChannelImpl>
{
    private final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    private String topic;
    private long parentCategoryId;
    private boolean nsfw = false;
    private int position;
    private int slowmode;

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
        ChannelAction<ForumChannel> action = guild.createForumChannel(name).setNSFW(nsfw).setTopic(topic).setSlowmode(slowmode);
        if (guild.equals(getGuild()))
        {
            Category parent = getParentCategory();
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

    @Nonnull
    @Override
    public RestAction<ThreadChannel> createForumPost(@Nonnull String name, @Nonnull Message message, @Nonnull Collection<String> tags, @Nonnull FileUpload... uploads)
    {
        Checks.notBlank(name, "Name");
        Checks.notNull(message, "Message");
        Checks.notNull(tags, "Tags");
        Checks.noneNull(uploads, "Uploads");

        Member selfMember = getGuild().getSelfMember();
        if (!selfMember.hasAccess(this))
            throw new MissingAccessException(this, Permission.VIEW_CHANNEL);
        if (!selfMember.hasPermission(this, Permission.MESSAGE_SEND))
            throw new InsufficientPermissionException(this, Permission.MESSAGE_SEND);

        DataObject payload = DataObject.empty();
        payload.put("name", name);

        if (!tags.isEmpty())
            payload.put("applied_tags", DataArray.fromCollection(tags));

        DataObject messageJson;
        payload.put("message", messageJson = DataObject.empty());

        messageJson.put("content", message.getContentRaw());
        messageJson.put("embeds", DataArray.fromCollection(message.getEmbeds()));

        RequestBody body;

        if (uploads.length > 0)
        {
            MultipartBody.Builder form = AttachedFile.createMultipartBody(Arrays.asList(uploads), null);

            DataArray attachments = DataArray.empty();
            for (int i = 0; i < uploads.length; i++)
                attachments.add(uploads[i].toAttachmentData(i));

            form.addFormDataPart("payload_json", payload.toString());
            body = form.build();
        }
        else
        {
            body = RequestBody.create(payload.toString(), Requester.MEDIA_TYPE_JSON);
        }

        Route.CompiledRoute route = Route.Channels.CREATE_THREAD_WITHOUT_MESSAGE.compile(getId());
        route = route.withQueryParams("use_nested_fields", "true");

        return new RestActionImpl<>(getJDA(), route, body, (response, request) -> {
            DataObject json = response.getObject();
            EntityBuilder builder = api.getEntityBuilder();
            return builder.createThreadChannel(getGuild(), json, guild.getIdLong());
        });
    }

    @NotNull
    @Override
    public ThreadChannelAction createThreadChannel(String name)
    {
        throw new UnsupportedOperationException("You cannot create threads without a message payload in forum channels! Use createForumPost(...) instead.");
    }

    @NotNull
    @Override
    public ThreadChannelAction createThreadChannel(String name, String messageId)
    {
        throw new UnsupportedOperationException("You cannot create threads without a message payload in forum channels! Use createForumPost(...) instead.");
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
}
