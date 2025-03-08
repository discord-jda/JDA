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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumPost;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.ForumPostAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.ChannelUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.BooleanSupplier;

public class ForumPostActionImpl extends RestActionImpl<ForumPost> implements ForumPostAction, MessageCreateBuilderMixin<ForumPostAction>
{
    private final MessageCreateBuilder builder;
    private final IPostContainer channel;
    private final TLongSet appliedTags = new TLongHashSet();
    private String name;
    private ThreadChannel.AutoArchiveDuration autoArchiveDuration;
    protected Integer slowmode = null;

    public ForumPostActionImpl(IPostContainer channel, String name, MessageCreateBuilder builder)
    {
        super(channel.getJDA(), Route.Channels.CREATE_THREAD.compile(channel.getId()));
        this.builder = builder;
        this.channel = channel;
        setName(name);
    }

    @Nonnull
    @Override
    public ForumPostAction setCheck(BooleanSupplier checks)
    {
        return (ForumPostAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public ForumPostAction addCheck(@Nonnull BooleanSupplier checks)
    {
        return (ForumPostAction) super.addCheck(checks);
    }

    @Nonnull
    @Override
    public ForumPostAction deadline(long timestamp)
    {
        return (ForumPostAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return channel.getGuild();
    }

    @Nonnull
    @Override
    public IPostContainer getChannel()
    {
        return channel;
    }

    @Nonnull
    @Override
    public ForumPostAction setTags(@Nonnull Collection<? extends ForumTagSnowflake> tags)
    {
        Checks.noneNull(tags, "Tags");
        Checks.check(tags.size() <= ForumChannel.MAX_POST_TAGS, "Provided more than %d tags.", ForumChannel.MAX_POST_TAGS);
        Checks.check(!channel.isTagRequired() || !tags.isEmpty(), "This forum requires at least one tag per post! See ForumChannel#isRequireTag()");
        this.appliedTags.clear();
        tags.forEach(t -> this.appliedTags.add(t.getIdLong()));
        return this;
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.GUILD_PUBLIC_THREAD;
    }

    @Nonnull
    @Override
    public ForumPostAction setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, Channel.MAX_NAME_LENGTH, "Name");
        this.name = name.trim();
        return this;
    }

    @Nonnull
    @Override
    public ForumPostAction setAutoArchiveDuration(@Nonnull ThreadChannel.AutoArchiveDuration autoArchiveDuration)
    {
        Checks.notNull(autoArchiveDuration, "AutoArchiveDuration");
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }
    
    @Nonnull
    @Override
    public ForumPostAction setSlowmode(int slowmode)
    {
        Checks.checkSupportedChannelTypes(ChannelUtil.SLOWMODE_SUPPORTED, getType(), "slowmode");
        Checks.check(slowmode <= ISlowmodeChannel.MAX_SLOWMODE && slowmode >= 0, "Slowmode per user must be between 0 and %d (seconds)!", ISlowmodeChannel.MAX_SLOWMODE);
        if (!getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_THREADS))
            throw new InsufficientPermissionException(channel, Permission.MANAGE_THREADS, "You must have Permission.MANAGE_THREADS on the parent forum channel to set a slowmode!");
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public MessageCreateBuilder getBuilder()
    {
        return builder;
    }

    @Override
    protected RequestBody finalizeData()
    {
        try (MessageCreateData message = builder.build())
        {
            DataObject json = DataObject.empty();
            json.put("message", message);
            json.put("name", name);
            if (autoArchiveDuration != null)
                json.put("auto_archive_duration", autoArchiveDuration.getMinutes());
            if (slowmode != null)
                json.put("rate_limit_per_user", slowmode);
            if (!appliedTags.isEmpty())
                json.put("applied_tags", appliedTags.toArray());
            else if (getChannel().isTagRequired())
                throw new IllegalStateException("Cannot create posts without a tag in this forum. Apply at least one tag!");
            return getMultipartBody(message.getFiles(), message.getAdditionalFiles(), json);
        }
    }

    @Override
    protected void handleSuccess(Response response, Request<ForumPost> request)
    {
        DataObject json = response.getObject();

        EntityBuilder entityBuilder = api.getEntityBuilder();

        ThreadChannel thread = entityBuilder.createThreadChannel(json, getGuild().getIdLong());
        Message message = entityBuilder.createMessageWithChannel(json.getObject("message"), thread, false);

        request.onSuccess(new ForumPost(message, thread));
    }
}
