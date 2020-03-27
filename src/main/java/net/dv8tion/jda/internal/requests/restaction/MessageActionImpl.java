/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Method;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class MessageActionImpl extends RestActionImpl<Message> implements MessageAction
{
    private static final String CONTENT_TOO_BIG = String.format("A message may not exceed %d characters. Please limit your input!", Message.MAX_CONTENT_LENGTH);
    protected final Map<String, InputStream> files = new HashMap<>();
    protected final Set<InputStream> ownedResources = new HashSet<>();
    protected final StringBuilder content;
    protected final MessageChannel channel;
    protected MessageEmbed embed = null;
    protected String nonce = null;
    protected boolean tts = false, override = false;

    public MessageActionImpl(JDA api, Route.CompiledRoute route, MessageChannel channel)
    {
        super(api, route);
        this.content = new StringBuilder();
        this.channel = channel;
    }

    public MessageActionImpl(JDA api, Route.CompiledRoute route, MessageChannel channel, StringBuilder contentBuilder)
    {
        super(api, route);
        Checks.check(contentBuilder.length() <= Message.MAX_CONTENT_LENGTH,
            "Cannot build a Message with more than %d characters. Please limit your input.", Message.MAX_CONTENT_LENGTH);
        this.content = contentBuilder;
        this.channel = channel;
    }

    @Nonnull
    @Override
    public MessageAction setCheck(BooleanSupplier checks)
    {
        return (MessageAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public MessageAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (MessageAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public MessageAction deadline(long timestamp)
    {
        return (MessageAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Override
    public boolean isEmpty()
    {
        return Helpers.isBlank(content)
            && (embed == null || embed.isEmpty() || !hasPermission(Permission.MESSAGE_EMBED_LINKS));
    }

    @Override
    public boolean isEdit()
    {
        return finalizeRoute().getMethod() == Method.PATCH;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl apply(final Message message)
    {
        if (message == null || message.getType() != MessageType.DEFAULT)
            return this;
        final List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds != null && !embeds.isEmpty())
            embed(embeds.get(0));
        files.clear();

        return content(message.getContentRaw()).tts(message.isTTS());
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl tts(final boolean isTTS)
    {
        this.tts = isTTS;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl reset()
    {
        return content(null).nonce(null).embed(null).tts(false).override(false).clearFiles();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl nonce(final String nonce)
    {
        this.nonce = nonce;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl content(final String content)
    {
        if (content == null || content.isEmpty())
            this.content.setLength(0);
        else if (content.length() <= Message.MAX_CONTENT_LENGTH)
            this.content.replace(0, this.content.length(), content);
        else
            throw new IllegalArgumentException(CONTENT_TOO_BIG);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl embed(final MessageEmbed embed)
    {
        if (embed != null)
        {
            Checks.check(embed.isSendable(),
               "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
               MessageEmbed.EMBED_MAX_LENGTH_BOT);
        }
        this.embed = embed;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl append(final CharSequence csq, final int start, final int end)
    {
        if (content.length() + end - start > Message.MAX_CONTENT_LENGTH)
            throw new IllegalArgumentException("A message may not exceed 2000 characters. Please limit your input!");
        content.append(csq, start, end);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl append(final char c)
    {
        if (content.length() == Message.MAX_CONTENT_LENGTH)
            throw new IllegalArgumentException("A message may not exceed 2000 characters. Please limit your input!");
        content.append(c);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl addFile(@Nonnull final InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        checkEdit();
        Checks.notNull(data, "Data");
        Checks.notBlank(name, "Name");
        Checks.noneNull(options, "Options");
        checkFileAmount();
        checkPermission(Permission.MESSAGE_ATTACH_FILES);
        name = applyOptions(name, options);
        files.put(name, data);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl addFile(@Nonnull final File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        Checks.noneNull(options, "Options");
        Checks.check(file.exists() && file.canRead(), "Provided file either does not exist or cannot be read from!");
        final long maxSize = getJDA().getSelfUser().getAllowedFileSize();
        Checks.check(file.length() <= maxSize, "File may not exceed the maximum file length of %d bytes!", maxSize);
        try
        {
            FileInputStream data = new FileInputStream(file);
            ownedResources.add(data);
            name = applyOptions(name, options);
            return addFile(data, name);
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl clearFiles()
    {
        files.clear();
        clearResources();
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl clearFiles(@Nonnull BiConsumer<String, InputStream> finalizer)
    {
        Checks.notNull(finalizer, "Finalizer");
        for (Iterator<Map.Entry<String, InputStream>> it = files.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<String, InputStream> entry = it.next();
            finalizer.accept(entry.getKey(), entry.getValue());
            it.remove();
        }
        clearResources();
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl clearFiles(@Nonnull Consumer<InputStream> finalizer)
    {
        Checks.notNull(finalizer, "Finalizer");
        for (Iterator<InputStream> it = files.values().iterator(); it.hasNext(); )
        {
            finalizer.accept(it.next());
            it.remove();
        }
        clearResources();
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl override(final boolean bool)
    {
        this.override = isEdit() && bool;
        return this;
    }

    private String applyOptions(String name, AttachmentOption[] options)
    {
        for (AttachmentOption opt : options)
        {
            if (opt == AttachmentOption.SPOILER)
            {
                name = "SPOILER_" + name;
                break;
            }
        }
        return name;
    }

    private void clearResources()
    {
        for (InputStream ownedResource : ownedResources)
        {
            try
            {
                ownedResource.close();
            }
            catch (IOException ex)
            {
                if (!ex.getMessage().toLowerCase().contains("closed"))
                    LOG.error("Encountered IOException trying to close owned resource", ex);
            }
        }
        ownedResources.clear();
    }

    protected RequestBody asMultipart()
    {
        final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        int index = 0;
        for (Map.Entry<String, InputStream> entry : files.entrySet())
        {
            final RequestBody body = IOUtil.createRequestBody(Requester.MEDIA_TYPE_OCTET, entry.getValue());
            builder.addFormDataPart("file" + index++, entry.getKey(), body);
        }
        if (!isEmpty())
            builder.addFormDataPart("payload_json", getJSON().toString());
        // clear remaining resources, they will be closed after being sent
        files.clear();
        ownedResources.clear();
        return builder.build();
    }

    protected RequestBody asJSON()
    {
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, getJSON().toString());
    }

    protected DataObject getJSON()
    {
        final DataObject obj = DataObject.empty();
        if (override)
        {
            if (embed == null)
                obj.putNull("embed");
            else
                obj.put("embed", embed);
            if (content.length() == 0)
                obj.putNull("content");
            else
                obj.put("content", content.toString());
            if (nonce == null)
                obj.putNull("nonce");
            else
                obj.put("nonce", nonce);
            obj.put("tts", tts);
        }
        else
        {
            if (embed != null)
                obj.put("embed", embed);
            if (content.length() > 0)
                obj.put("content", content.toString());
            if (nonce != null)
                obj.put("nonce", nonce);
            obj.put("tts", tts);
        }
        return obj;
    }

    protected void checkFileAmount()
    {
        if (files.size() >= Message.MAX_FILE_AMOUNT)
            throw new IllegalStateException("Cannot add more than " + Message.MAX_FILE_AMOUNT + " files!");
    }

    protected void checkEdit()
    {
        if (isEdit())
            throw new IllegalStateException("Cannot add files to an existing message! Edit-Message does not support this operation!");
    }

    protected void checkPermission(Permission perm)
    {
        if (!hasPermission(perm))
        {
            TextChannel channel = (TextChannel) this.channel;
            throw new InsufficientPermissionException(channel, perm);
        }
    }

    protected boolean hasPermission(Permission perm)
    {
        if (channel.getType() != ChannelType.TEXT)
            return true;
        TextChannel text = (TextChannel) channel;
        Member self = text.getGuild().getSelfMember();
        return self.hasPermission(text, perm);
    }

    @Override
    protected RequestBody finalizeData()
    {
        if (!files.isEmpty())
            return asMultipart();
        else if (!isEmpty())
            return asJSON();
        throw new IllegalStateException("Cannot build a message without content!");
    }

    @Override
    protected void handleSuccess(Response response, Request<Message> request)
    {
        request.onSuccess(api.getEntityBuilder().createMessage(response.getObject(), channel, false));
    }

    @Override
    @SuppressWarnings("deprecation") /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */
    protected void finalize()
    {
        if (ownedResources.isEmpty())
            return;
        LOG.warn("Found unclosed resources in MessageAction instance, closing on finalization step!");
        clearResources();
    }
}
