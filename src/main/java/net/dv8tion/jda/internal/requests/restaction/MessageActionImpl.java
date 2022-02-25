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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.AllowedMentionsImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MessageActionImpl extends RestActionImpl<Message> implements MessageAction
{
    private static final String CONTENT_TOO_BIG = Helpers.format("A message may not exceed %d characters. Please limit your input!", Message.MAX_CONTENT_LENGTH);
    protected static boolean defaultFailOnInvalidReply = false;
    protected final Map<String, InputStream> files = new HashMap<>();
    protected final Set<InputStream> ownedResources = new HashSet<>();
    protected final StringBuilder content;
    protected final MessageChannel channel;
    protected final AllowedMentionsImpl allowedMentions = new AllowedMentionsImpl();
    protected List<ActionRow> components;
    protected List<String> retainedAttachments;
    protected List<MessageEmbed> embeds = null;
    protected String nonce = null;
    protected boolean tts = false, override = false;
    protected boolean failOnInvalidReply = defaultFailOnInvalidReply;
    protected long messageReference;

    protected final String messageId;
    private InteractionHook hook = null;

    public static void setDefaultFailOnInvalidReply(boolean fail)
    {
        defaultFailOnInvalidReply = fail;
    }

    public static boolean isDefaultFailOnInvalidReply()
    {
        return defaultFailOnInvalidReply;
    }

    public MessageActionImpl(JDA api, String messageId, MessageChannel channel)
    {
        super(api, messageId != null
            ? Route.Messages.EDIT_MESSAGE.compile(channel.getId(), messageId)
            : Route.Messages.SEND_MESSAGE.compile(channel.getId()));
        this.content = new StringBuilder();
        this.channel = channel;
        this.messageId = messageId;
    }

    public MessageActionImpl(JDA api, String messageId, MessageChannel channel, StringBuilder contentBuilder)
    {
        super(api, messageId != null
            ? Route.Messages.EDIT_MESSAGE.compile(channel.getId(), messageId)
            : Route.Messages.SEND_MESSAGE.compile(channel.getId()));
        Checks.check(contentBuilder.length() <= Message.MAX_CONTENT_LENGTH,
            "Cannot build a Message with more than %d characters. Please limit your input.", Message.MAX_CONTENT_LENGTH);
        this.content = contentBuilder;
        this.channel = channel;
        this.messageId = messageId;
    }

    public MessageActionImpl withHook(InteractionHook hook)
    {
        this.hook = hook;
        return this;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        if (hook == null || hook.isExpired())
            return super.finalizeRoute(); // Try as a normal bot message
        if (isEdit()) // Try with interaction hook if its not expired, these have different rate limits and scale better
            return Route.Interactions.EDIT_FOLLOWUP.compile(api.getSelfUser().getApplicationId(), hook.getInteraction().getToken(), messageId);
        else
            return Route.Interactions.CREATE_FOLLOWUP.compile(api.getSelfUser().getApplicationId(), hook.getInteraction().getToken());
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
        return !isEdit() // PATCH can be technically empty since you can update stuff like components or remove embeds etc
            && Helpers.isBlank(content)
            && (embeds == null || embeds.isEmpty() || !hasPermission(Permission.MESSAGE_EMBED_LINKS));
    }

    @Override
    public boolean isEdit()
    {
        return messageId != null;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public MessageActionImpl apply(final Message message)
    {
        if (message == null || message.getType().isSystem())
            return this;
        final List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds != null && !embeds.isEmpty())
            setEmbeds(embeds.stream().filter(e -> e != null && e.getType() == EmbedType.RICH).collect(Collectors.toList()));
        files.clear();

        components = new ArrayList<>();
        components.addAll(message.getActionRows());
        allowedMentions.applyMessage(message);
        String content = message.getContentRaw();
        return content(content).tts(message.isTTS());
    }

    @Nonnull
    @Override
    public MessageActionImpl referenceById(long messageId)
    {
        messageReference = messageId;
        return this;
    }

    @Nonnull
    @Override
    public MessageActionImpl failOnInvalidReply(boolean fail)
    {
        failOnInvalidReply = fail;
        return this;
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
        return content(null).nonce(null).setEmbeds(Collections.emptyList()).tts(false).override(false).clearFiles();
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
    public MessageActionImpl setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "MessageEmbeds");
        embeds.forEach(embed ->
            Checks.check(embed.isSendable(),
                "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
                MessageEmbed.EMBED_MAX_LENGTH_BOT)
        );
        Checks.check(embeds.size() <= 10, "Cannot have more than 10 embeds in a message!");
        Checks.check(embeds.stream().mapToInt(MessageEmbed::getLength).sum() <= MessageEmbed.EMBED_MAX_LENGTH_BOT, "The sum of all MessageEmbeds may not exceed %d!", MessageEmbed.EMBED_MAX_LENGTH_BOT);
        if (this.embeds == null)
            this.embeds = new ArrayList<>();
        this.embeds.clear();
        this.embeds.addAll(embeds);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl append(final CharSequence csq, final int start, final int end)
    {
        if (content.length() + end - start > Message.MAX_CONTENT_LENGTH)
            throw new IllegalArgumentException("A message may not exceed " + Message.MAX_CONTENT_LENGTH + " characters. Please limit your input!");
        content.append(csq, start, end);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl append(final char c)
    {
        if (content.length() == Message.MAX_CONTENT_LENGTH)
            throw new IllegalArgumentException("A message may not exceed " + Message.MAX_CONTENT_LENGTH + " characters. Please limit your input!");
        content.append(c);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageActionImpl addFile(@Nonnull final InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
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
        final long maxSize = getMaxFileSize();
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
    public MessageAction addFile(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "Data");
        final long maxSize = getMaxFileSize();
        Checks.check(data.length <= maxSize, "File may not exceed the maximum file length of %d bytes!", maxSize);
        return addFile(new ByteArrayInputStream(data), name, options);
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
    public MessageActionImpl retainFilesById(@Nonnull Collection<String> ids)
    {
        if (!isEdit()) return this; // You can't retain files for messages that don't exist lol
        if (this.retainedAttachments == null)
            this.retainedAttachments = new ArrayList<>();
        this.retainedAttachments.addAll(ids);
        return this;
    }

    @Nonnull
    @Override
    public MessageActionImpl setActionRows(@Nonnull ActionRow... rows)
    {
        Checks.noneNull(rows, "ActionRows");
        if (components == null)
            components = new ArrayList<>();
        Checks.check(rows.length <= 5, "Can only have 5 action rows per message!");
        Checks.checkDuplicateIds(Arrays.stream(rows));
        this.components.clear();
        Collections.addAll(this.components, rows);
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

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public MessageActionImpl mentionRepliedUser(boolean mention)
    {
        allowedMentions.mentionRepliedUser(mention);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public MessageActionImpl allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        this.allowedMentions.allowedMentions(allowedMentions);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public MessageActionImpl mention(@Nonnull IMentionable... mentions)
    {
        this.allowedMentions.mention(mentions);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public MessageActionImpl mentionUsers(@Nonnull String... userIds)
    {
        this.allowedMentions.mentionUsers(userIds);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public MessageActionImpl mentionRoles(@Nonnull String... roleIds)
    {
        this.allowedMentions.mentionRoles(roleIds);
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

    private long getMaxFileSize()
    {
        if (channel.getType().isGuild())
            return ((GuildChannel) channel).getGuild().getMaxFileSize();
        return getJDA().getSelfUser().getAllowedFileSize();
    }

    protected RequestBody asMultipart()
    {
        final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        int index = 0;
        for (Map.Entry<String, InputStream> entry : files.entrySet())
        {
            final RequestBody body = IOUtil.createRequestBody(Requester.MEDIA_TYPE_OCTET, entry.getValue());
            builder.addFormDataPart("files[" + (index++) + "]", entry.getKey(), body);
        }
        if (messageReference != 0L || components != null || retainedAttachments != null || !isEmpty())
            builder.addFormDataPart("payload_json", getJSON().toString());
        // clear remaining resources, they will be closed after being sent
        files.clear();
        ownedResources.clear();
        return builder.build();
    }

    protected RequestBody asJSON()
    {
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, getJSON().toJson());
    }

    protected DataObject getJSON()
    {
        final DataObject obj = DataObject.empty();
        if (override)
        {
            if (embeds == null)
                obj.put("embeds", DataArray.empty());
            else
                obj.put("embeds", DataArray.fromCollection(embeds));
            if (content.length() == 0)
                obj.putNull("content");
            else
                obj.put("content", content.toString());
            if (nonce == null)
                obj.putNull("nonce");
            else
                obj.put("nonce", nonce);
            if (components == null)
                obj.put("components", DataArray.empty());
            else
                obj.put("components", DataArray.fromCollection(components));
            if (retainedAttachments != null)
                obj.put("attachments", DataArray.fromCollection(retainedAttachments.stream()
                        .map(id -> DataObject.empty()
                            .put("id", id))
                        .collect(Collectors.toList())));
            else
                obj.put("attachments", DataArray.empty());
        }
        else
        {
            if (embeds != null)
                obj.put("embeds", DataArray.fromCollection(embeds));
            if (content.length() > 0)
                obj.put("content", content.toString());
            if (nonce != null)
                obj.put("nonce", nonce);
            if (components != null)
                obj.put("components", DataArray.fromCollection(components));
            if (retainedAttachments != null)
                obj.put("attachments", DataArray.fromCollection(retainedAttachments.stream()
                        .map(id -> DataObject.empty()
                            .put("id", id))
                        .collect(Collectors.toList())));
        }
        if (messageReference != 0)
        {
            obj.put("message_reference", DataObject.empty()
                .put("message_id", messageReference)
                .put("channel_id", channel.getId())
                .put("fail_if_not_exists", failOnInvalidReply));
        }
        obj.put("tts", tts);
        obj.put("allowed_mentions", allowedMentions);
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
        if (!channel.getType().isGuild())
            return;

        if (!(channel instanceof IPermissionContainer))
            return;

        IPermissionContainer gc = (IPermissionContainer) channel;
        if (!gc.getGuild().getSelfMember().hasAccess(gc))
            throw new MissingAccessException(gc, Permission.VIEW_CHANNEL);
        if (!hasPermission(perm))
            throw new InsufficientPermissionException(gc, perm);
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
        else if (embeds != null && !embeds.isEmpty() && channel instanceof GuildChannel)
            throw new InsufficientPermissionException((GuildChannel) channel, Permission.MESSAGE_EMBED_LINKS, "Cannot send message with only embeds without Permission.MESSAGE_EMBED_LINKS!");
        throw new IllegalStateException("Cannot build a message without content!");
    }

    @Override
    protected void handleSuccess(Response response, Request<Message> request)
    {
        request.onSuccess(api.getEntityBuilder().createMessageWithChannel(response.getObject(), channel, false));
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
