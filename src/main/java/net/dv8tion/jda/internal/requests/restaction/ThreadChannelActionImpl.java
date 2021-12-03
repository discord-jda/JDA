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

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class ThreadChannelActionImpl extends AuditableRestActionImpl<ThreadChannel> implements ThreadChannelAction
{
    protected final Guild guild;
    protected final ChannelType type;
    protected final String parentMessageId;

    protected String name;
    protected ThreadChannel.AutoArchiveDuration autoArchiveDuration = null;
    protected Boolean invitable = null;

    public ThreadChannelActionImpl(GuildChannel channel, String name, ChannelType type)
    {
        super(channel.getJDA(), Route.Channels.CREATE_THREAD_WITHOUT_MESSAGE.compile(channel.getId()));
        this.guild = channel.getGuild();
        this.type = type;
        this.parentMessageId = null;

        this.name = name;
    }

    public ThreadChannelActionImpl(GuildChannel channel, String name, String parentMessageId)
    {
        super(channel.getJDA(), Route.Channels.CREATE_THREAD_WITH_MESSAGE.compile(channel.getId(), parentMessageId));
        this.guild = channel.getGuild();
        this.type = channel.getType() == ChannelType.TEXT ? ChannelType.GUILD_PUBLIC_THREAD : ChannelType.GUILD_NEWS_THREAD;
        this.parentMessageId = parentMessageId;

        this.name = name;
    }

    @Nonnull
    @Override
    public ThreadChannelActionImpl setCheck(BooleanSupplier checks)
    {
        return (ThreadChannelActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public ThreadChannelActionImpl timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (ThreadChannelActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public ThreadChannelActionImpl deadline(long timestamp)
    {
        return (ThreadChannelActionImpl) super.deadline(timestamp);
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
    public ThreadChannelActionImpl setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    public ThreadChannelAction setAutoArchiveDuration(@Nonnull ThreadChannel.AutoArchiveDuration autoArchiveDuration)
    {
        Checks.notNull(autoArchiveDuration, "autoArchiveDuration");

        Set<String> features = guild.getFeatures();
        if (autoArchiveDuration == ThreadChannel.AutoArchiveDuration.TIME_3_DAYS && !features.contains("THREE_DAY_THREAD_ARCHIVE"))
            throw new IllegalStateException("Cannot use TIME_3_DAYS archive duration because feature isn't supported on this Guild." +
                    " Missing THREE_DAY_THREAD_ARCHIVE feature due to boost level being too low.");

        if (autoArchiveDuration == ThreadChannel.AutoArchiveDuration.TIME_1_WEEK && !features.contains("SEVEN_DAY_THREAD_ARCHIVE"))
            throw new IllegalStateException("Cannot use TIME_1_WEEK archive duration because feature isn't supported on this Guild." +
                    " Missing SEVEN_DAY_THREAD_ARCHIVE feature due to boost level being too low.");

        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    @Nonnull
    @Override
    public ThreadChannelAction setInvitable(boolean invitable)
    {
        if (type != ChannelType.GUILD_PRIVATE_THREAD)
            throw new UnsupportedOperationException("Can only set invitable on private threads");

        this.invitable = invitable;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();

        object.put("name", name);

        //The type is selected by discord itself if we are using a parent message, so don't send it.
        if (parentMessageId == null)
            object.put("type", type.getId());

        if (autoArchiveDuration != null)
            object.put("auto_archive_duration", autoArchiveDuration.getMinutes());
        if (invitable != null)
            object.put("invitable", invitable);

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<ThreadChannel> request)
    {
        ThreadChannel channel = api.getEntityBuilder().createThreadChannel(response.getObject(), guild.getIdLong());
        request.onSuccess(channel);
    }
}
