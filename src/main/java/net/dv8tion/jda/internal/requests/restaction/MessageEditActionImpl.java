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

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class MessageEditActionImpl extends RestActionImpl<Message> implements MessageEditAction
{
    private final MessageChannel channel;
    private final MessageEditBuilder builder;

    public MessageEditActionImpl(MessageChannel channel, String messageId, MessageEditBuilder builder)
    {
        super(channel.getJDA(), Route.Messages.EDIT_MESSAGE.compile(channel.getId(), messageId));
        this.channel = channel;
        this.builder = builder;
    }

    @Override
    protected RequestBody finalizeData()
    {
        MessageEditData data = builder.build();
        try
        {
            DataObject json = data.toData();

            List<FileUpload> files = data.getFiles();
            if (files.isEmpty())
                return getRequestBody(json);

            return AttachedFile.createMultipartBody(files, json).build();
        }
        catch (Throwable e)
        {
            IOUtil.silentClose(data);
            throw e;
        }
    }

    @Override
    protected void handleSuccess(Response response, Request<Message> request)
    {
        api.getEntityBuilder().createMessageWithChannel(response.getObject(), channel, false);
    }

    @Nonnull
    @Override
    public MessageEditAction mentionRepliedUser(boolean mention)
    {
        builder.mentionRepliedUser(mention);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        builder.allowedMentions(allowedMentions);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction mention(@Nonnull IMentionable... mentions)
    {
        builder.mention(mentions);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction mentionUsers(@Nonnull String... userIds)
    {
        builder.mentionUsers(userIds);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction mentionRoles(@Nonnull String... roleIds)
    {
        builder.mentionRoles(roleIds);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction setAttachments(@Nullable Collection<? extends AttachedFile> attachments)
    {
        builder.setAttachments(attachments);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction replace(boolean isReplace)
    {
        builder.replace(isReplace);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction setContent(@Nullable String content)
    {
        builder.setContent(content);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        builder.setEmbeds(embeds);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditAction setComponents(@Nonnull Collection<? extends LayoutComponent> layouts)
    {
        builder.setComponents(layouts);
        return this;
    }
}
