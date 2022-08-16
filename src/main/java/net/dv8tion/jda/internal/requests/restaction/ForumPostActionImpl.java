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

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.ForumPostAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;

public class ForumPostActionImpl extends RestActionImpl<ForumPost> implements ForumPostAction, MessageCreateBuilderMixin<ForumPostAction>
{
    private final MessageCreateBuilder builder;
    private final ForumChannel channel;
    private String name;
    private ThreadChannel.AutoArchiveDuration autoArchiveDuration;

    public ForumPostActionImpl(ForumChannel channel, String name, MessageCreateBuilder builder)
    {
        super(channel.getJDA(), Route.Channels.CREATE_THREAD_WITHOUT_MESSAGE.compile(channel.getId()));
        this.builder = builder;
        this.channel = channel;
        setName(name);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return channel.getGuild();
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
            return getMultipartBody(message.getFiles(), json);
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
