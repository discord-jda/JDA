/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.*;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import net.dv8tion.jda.core.utils.MiscUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Extension of a default {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 * that allows setting message information before sending!
 *
 * <p>This is available as return type of all sendMessage/sendFile methods in {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
 * or by using {@link net.dv8tion.jda.core.MessageBuilder#sendTo(net.dv8tion.jda.core.entities.MessageChannel) MessageBuilder.sendTo(MessageChannel)}
 *
 * <p>When updating a Message, unset fields will be ignored by default. To override existing fields with no value (remove content)
 * you can use {@link #override(boolean) override(true)}. Setting this to {@code true} will cause all fields to be considered
 * and will override the Message entirely causing unset values to be removed from that message.
 * <br>This can be used to remove existing embeds from a message:
 * <br>{@code message.editMessage("This message had an embed").override(true).queue()}
 *
 * <p><u>When this RestAction has been executed all provided files will be closed.
 * If you decide not to execute this action, you should call {@link #clearFiles()} to free resources.</u>
 * <br>Note that the garbage collector also frees opened file streams when it finalizes the stream object.
 *
 * <h1>Example</h1>
 * <pre><code>
 * {@literal @Override}
 *  public void onMessageReceived(MessageReceivedEvent event)
 *  {
 *      MessageChannel channel = event.getChannel();
 *      channel.sendMessage("This has an embed with an image!")
 *             .addFile(new File("dog.png"))
 *             .embed(new EmbedBuilder()
 *                 .setImage("attachment://dog.png")
 *                 .build())
 *             .queue(); // this actually sends the information to discord
 *  }
 * </code></pre>
 *
 * @since  3.4.0
 */
public class MessageAction extends RestAction<Message> implements Appendable
{
    private static final String CONTENT_TOO_BIG = String.format("A message may not exceed %d characters. Please limit your input!", Message.MAX_CONTENT_LENGTH);
    protected final Map<String, InputStream> files = new HashMap<>();
    protected final Set<InputStream> ownedResources = new HashSet<>();
    protected final StringBuilder content;
    protected final MessageChannel channel;
    protected MessageEmbed embed = null;
    protected String nonce = null;
    protected boolean tts = false, override = false;

    public MessageAction(JDA api, Route.CompiledRoute route, MessageChannel channel)
    {
        super(api, route);
        this.content = new StringBuilder();
        this.channel = channel;
    }

    public MessageAction(JDA api, Route.CompiledRoute route, MessageChannel channel, StringBuilder contentBuilder)
    {
        super(api, route);
        Checks.check(contentBuilder.length() <= Message.MAX_CONTENT_LENGTH,
            "Cannot build a Message with more than %d characters. Please limit your input.", Message.MAX_CONTENT_LENGTH);
        this.content = contentBuilder;
        this.channel = channel;
    }

    /**
     * Whether this MessageAction has no values set.
     * <br>Trying to execute with {@code isEmpty() == true} will result in an {@link java.lang.IllegalStateException IllegalStateException}!
     *
     * <p><b>This does not check for files!</b>
     *
     * @return True, if no settings have been applied
     */
    public boolean isEmpty()
    {
        return Helpers.isBlank(content)
            && (embed == null || embed.isEmpty() || !hasPermission(Permission.MESSAGE_EMBED_LINKS));
    }

    /**
     * Whether this MessageAction will be used to update an existing message.
     *
     * @return True, if this MessageAction targets an existing message
     */
    public boolean isEdit()
    {
        return finalizeRoute().getMethod() == Method.PATCH;
    }

    /**
     * Applies the sendable information of the provided {@link net.dv8tion.jda.core.entities.Message Message}
     * to this MessageAction settings.
     * <br>This will override all existing settings <b>if</b> new settings are available.
     *
     * <p><b>This does not copy files!</b>
     *
     * @param  message
     *         The nullable Message to apply settings from
     *
     * @throws java.lang.IllegalArgumentException
     *         If the message contains an {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     *         that exceeds the sendable character limit,
     *         see {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) MessageEmbed.isSendable(AccountType)}
     *
     * @return Updated MessageAction for chaining convenience
     */
    @CheckReturnValue
    public MessageAction apply(final Message message)
    {
        if (message == null || message.getType() != MessageType.DEFAULT)
            return this;
        final List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds != null && !embeds.isEmpty())
            embed(embeds.get(0));
        files.clear();

        return content(message.getContentRaw()).tts(message.isTTS());
    }

    /**
     * Enable/Disable Text-To-Speech for the resulting message.
     * <br>This is only relevant to MessageActions that are not {@code isEdit() == true}!
     *
     * @param  isTTS
     *         True, if this should cause a Text-To-Speech effect when sent to the channel
     *
     * @return Updated MessageAction for chaining convenience
     */
    @CheckReturnValue
    public MessageAction tts(final boolean isTTS)
    {
        this.tts = isTTS;
        return this;
    }

    /**
     * Resets this MessageAction to empty state
     * <br>{@link #isEmpty()} will result in {@code true} after this has been performed!
     *
     * <p>Convenience over using
     * {@code content(null).nonce(null).embed(null).tts(false).override(false).clearFiles()}
     *
     * @return Updated MessageAction for chaining convenience
     */
    @CheckReturnValue
    public MessageAction reset()
    {
        return content(null).nonce(null).embed(null).tts(false).override(false).clearFiles();
    }

    /**
     * Sets the validation nonce for the outgoing Message
     *
     * <p>For more information see {@link net.dv8tion.jda.core.MessageBuilder#setNonce(String) MessageBuilder.setNonce(String)}
     * and {@link net.dv8tion.jda.core.entities.Message#getNonce() Message.getNonce()}
     *
     * @param  nonce
     *         The nonce that shall be used
     *
     * @return Updated MessageAction for chaining convenience
     *
     * @see    net.dv8tion.jda.core.entities.Message#getNonce()
     * @see    net.dv8tion.jda.core.MessageBuilder#setNonce(String)
     * @see    <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank">Cryptographic Nonce - Wikipedia</a>
     */
    @CheckReturnValue
    public MessageAction nonce(final String nonce)
    {
        this.nonce = nonce;
        return this;
    }

    /**
     * Overrides existing content with the provided input
     * <br>The content of a Message may not exceed {@value Message#MAX_CONTENT_LENGTH}!
     *
     * @param  content
     *         Sets the specified content and overrides previous content
     *         or {@code null} to reset content
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided content exceeds the {@value Message#MAX_CONTENT_LENGTH} character limit
     *
     * @return Updated MessageAction for chaining convenience
     */
    @CheckReturnValue
    public MessageAction content(final String content)
    {
        if (content == null || content.isEmpty())
            this.content.setLength(0);
        else if (content.length() <= Message.MAX_CONTENT_LENGTH)
            this.content.replace(0, this.content.length(), content);
        else
            throw new IllegalArgumentException(CONTENT_TOO_BIG);
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     * that should be used for this Message.
     * Refer to {@link net.dv8tion.jda.core.EmbedBuilder EmbedBuilder} for more information.
     *
     * @param  embed
     *         The {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} that should
     *         be attached to this message, {@code null} to use no embed.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided MessageEmbed is not sendable according to
     *         {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) MessageEmbed.isSendable(AccountType)}!
     *         If the provided MessageEmbed is an unknown implementation this operation will fail as we are unable to deserialize it.
     *
     * @return Updated MessageAction for chaining convenience
     */
    @CheckReturnValue
    public MessageAction embed(final MessageEmbed embed)
    {
        if (embed != null)
        {
            final AccountType type = getJDA().getAccountType();
            Checks.check(embed.isSendable(type),
                "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for %s accounts!",
                type == AccountType.BOT ? MessageEmbed.EMBED_MAX_LENGTH_BOT : MessageEmbed.EMBED_MAX_LENGTH_CLIENT, type);
        }
        this.embed = embed;
        return this;
    }

    /**
     * {@inheritDoc}
     * @throws java.lang.IllegalArgumentException
     *         If the appended CharSequence is too big and will cause the content to
     *         exceed the {@value net.dv8tion.jda.core.entities.Message#MAX_CONTENT_LENGTH} character limit
     *
     * @return Updated MessageAction for chaining convenience
     */
    @Override
    @CheckReturnValue
    public MessageAction append(final CharSequence csq)
    {
        return append(csq, 0, csq.length());
    }

    /**
     * {@inheritDoc}
     * @throws java.lang.IllegalArgumentException
     *         If the appended CharSequence is too big and will cause the content to
     *         exceed the {@value net.dv8tion.jda.core.entities.Message#MAX_CONTENT_LENGTH} character limit
     *
     * @return Updated MessageAction for chaining convenience
     */
    @Override
    @CheckReturnValue
    public MessageAction append(final CharSequence csq, final int start, final int end)
    {
        if (content.length() + end - start > Message.MAX_CONTENT_LENGTH)
            throw new IllegalArgumentException("A message may not exceed 2000 characters. Please limit your input!");
        content.append(csq, start, end);
        return this;
    }

    /**
     * {@inheritDoc}
     * @throws java.lang.IllegalArgumentException
     *         If the appended CharSequence is too big and will cause the content to
     *         exceed the {@value net.dv8tion.jda.core.entities.Message#MAX_CONTENT_LENGTH} character limit
     *
     * @return Updated MessageAction for chaining convenience
     */
    @Override
    @CheckReturnValue
    public MessageAction append(final char c)
    {
        if (content.length() == Message.MAX_CONTENT_LENGTH)
            throw new IllegalArgumentException("A message may not exceed 2000 characters. Please limit your input!");
        content.append(c);
        return this;
    }

    /**
     * Applies the result of {@link String#format(String, Object...) String.format(String, Object...)}
     * as content.
     *
     * <p>For more information of formatting review the {@link java.util.Formatter Formatter} documentation!
     *
     * @param  format
     *         The format String
     * @param  args
     *         The arguments that should be used for conversion
     *
     * @throws java.lang.IllegalArgumentException
     *         If the appended formatting is too big and will cause the content to
     *         exceed the {@value net.dv8tion.jda.core.entities.Message#MAX_CONTENT_LENGTH} character limit
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return Updated MessageAction for chaining convenience
     */
    @CheckReturnValue
    public MessageAction appendFormat(final String format, final Object... args)
    {
        return append(String.format(format, args));
    }

    /**
     * Adds the provided {@link java.io.InputStream InputStream} as file data.
     * <br><u>The stream will be closed upon execution!</u>
     *
     * <p>To reset all files use {@link #clearFiles()}
     *
     * @param  data
     *         The InputStream that will be interpreted as file data
     * @param  name
     *         The file name that should be used to interpret the type of the given data
     *         using the file-name extension. This name is similar to what will be visible
     *         through {@link net.dv8tion.jda.core.entities.Message.Attachment#getFileName() Message.Attachment.getFileName()}
     *
     * @throws java.lang.IllegalStateException
     *         If the file limit of {@value Message#MAX_FILE_AMOUNT} has been reached prior to calling this method,
     *         or if this MessageAction will perform an edit operation on an existing Message (see {@link #isEdit()})
     * @throws java.lang.IllegalArgumentException
     *         If the provided data is {@code null} or the provided name is blank or {@code null}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is targeting a TextChannel and the currently logged in account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *
     * @return Updated MessageAction for chaining convenience
     */
    @CheckReturnValue
    public MessageAction addFile(final InputStream data, final String name)
    {
        checkEdit();
        Checks.notNull(data, "Data");
        Checks.notBlank(name, "Name");
        checkFileAmount();
        checkPermission(Permission.MESSAGE_ATTACH_FILES);
        files.put(name, data);
        return this;
    }

    /**
     * Adds the provided byte[] as file data.
     *
     * <p>To reset all files use {@link #clearFiles()}
     *
     * @param  data
     *         The byte[] that will be interpreted as file data
     * @param  name
     *         The file name that should be used to interpret the type of the given data
     *         using the file-name extension. This name is similar to what will be visible
     *         through {@link net.dv8tion.jda.core.entities.Message.Attachment#getFileName() Message.Attachment.getFileName()}
     *
     * @throws java.lang.IllegalStateException
     *         If the file limit of {@value Message#MAX_FILE_AMOUNT} has been reached prior to calling this method,
     *         or if this MessageAction will perform an edit operation on an existing Message (see {@link #isEdit()})
     * @throws java.lang.IllegalArgumentException
     *         If the provided data is {@code null} or the provided name is blank or {@code null}
     *         or if the provided data exceeds the maximum file size of the currently logged in account
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is targeting a TextChannel and the currently logged in account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *
     * @return Updated MessageAction for chaining convenience
     *
     * @see    net.dv8tion.jda.core.entities.SelfUser#getAllowedFileSize() SelfUser.getAllowedFileSize()
     */
    @CheckReturnValue
    public MessageAction addFile(final byte[] data, final String name)
    {
        Checks.notNull(data, "Data");
        final long maxSize = getJDA().getSelfUser().getAllowedFileSize();
        Checks.check(data.length <= maxSize, "File may not exceed the maximum file length of %d bytes!", maxSize);
        return addFile(new ByteArrayInputStream(data), name);
    }

    /**
     * Adds the provided {@link java.io.File File} as file data.
     * <br>Shortcut for {@link #addFile(java.io.File, String) addFile(file, file.getName())} with the same side-effects.
     *
     * @param  file
     *         The File that will be interpreted as file data
     *
     * @throws java.lang.IllegalStateException
     *         If the file limit of {@value Message#MAX_FILE_AMOUNT} has been reached prior to calling this method,
     *         or if this MessageAction will perform an edit operation on an existing Message (see {@link #isEdit()})
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null} or if the provided File is bigger than the maximum file size of the currently logged in account
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is targeting a TextChannel and the currently logged in account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *
     * @return Updated MessageAction for chaining convenience
     *
     * @see    net.dv8tion.jda.core.entities.SelfUser#getAllowedFileSize() SelfUser.getAllowedFileSize()
     */
    @CheckReturnValue
    public MessageAction addFile(final File file)
    {
        Checks.notNull(file, "File");
        return addFile(file, file.getName());
    }

    /**
     * Adds the provided {@link java.io.File File} as file data.
     *
     * <p>To reset all files use {@link #clearFiles()}
     * <br><u>This method opens a {@link java.io.FileInputStream FileInputStream} which will be closed by executing this action or using {@link #clearFiles()}!</u>
     *
     * @param  file
     *         The File that will be interpreted as file data
     * @param  name
     *         The file name that should be used to interpret the type of the given data
     *         using the file-name extension. This name is similar to what will be visible
     *         through {@link net.dv8tion.jda.core.entities.Message.Attachment#getFileName() Message.Attachment.getFileName()}
     *
     * @throws java.lang.IllegalStateException
     *         If the file limit of {@value Message#MAX_FILE_AMOUNT} has been reached prior to calling this method,
     *         or if this MessageAction will perform an edit operation on an existing Message (see {@link #isEdit()})
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null} or the provided name is blank or {@code null}
     *         or if the provided file is bigger than the maximum file size of the currently logged in account,
     *         or if the provided file does not exist/ is not readable
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is targeting a TextChannel and the currently logged in account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *
     * @return Updated MessageAction for chaining convenience
     *
     * @see    net.dv8tion.jda.core.entities.SelfUser#getAllowedFileSize() SelfUser.getAllowedFileSize()
     */
    @CheckReturnValue
    public MessageAction addFile(final File file, final String name)
    {
        Checks.notNull(file, "File");
        Checks.check(file.exists() && file.canRead(), "Provided file either does not exist or cannot be read from!");
        final long maxSize = getJDA().getSelfUser().getAllowedFileSize();
        Checks.check(file.length() <= maxSize, "File may not exceed the maximum file length of %d bytes!", maxSize);
        try
        {
            FileInputStream data = new FileInputStream(file);
            ownedResources.add(data);
            return addFile(data, name);
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Clears all previously added files
     * <br>And closes {@code FileInputStreams} generated by {@link #addFile(File, String)}.
     * <br>To close all stream (including ones given by {@link #addFile(InputStream, String)}) use {@link #clearFiles(Consumer)}.
     *
     * @return Updated MessageAction for chaining convenience
     *
     * @see    #clearFiles(Consumer)
     * @see    #clearFiles(BiConsumer)
     */
    @CheckReturnValue
    public MessageAction clearFiles()
    {
        files.clear();
        clearResources();
        return this;
    }

    /**
     * Clears all previously added files
     *
     * @param  finalizer
     *         BiConsumer useful to <b>close</b> remaining resources,
     *         the consumer will receive the name as a string parameter and the resource as {@code InputStream}.
     *
     * @return Updated MessageAction for chaining convenience
     *
     * @see    java.io.Closeable
     */
    @CheckReturnValue
    public MessageAction clearFiles(BiConsumer<String, InputStream> finalizer)
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

    /**
     * Clears all previously added files
     * <br>The {@link #clearFiles(BiConsumer)} version provides the resource name for more selective operations.
     *
     * @param  finalizer
     *         Consumer useful to <b>close</b> remaining resources,
     *         the consumer will receive only the resource in the form of an {@code InputStream}
     *
     * @return Updated MessageAction for chaining convenience
     *
     * @see    java.io.Closeable
     */
    @CheckReturnValue
    public MessageAction clearFiles(Consumer<InputStream> finalizer)
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

    /**
     * Whether all fields should be considered when editing a message
     *
     * @param  bool
     *         True, to override all fields even if they are not set
     *
     * @return Updated MessageAction for chaining convenience
     */
    @CheckReturnValue
    public MessageAction override(final boolean bool)
    {
        this.override = isEdit() && bool;
        return this;
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
            final RequestBody body = MiscUtil.createRequestBody(Requester.MEDIA_TYPE_OCTET, entry.getValue());
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

    protected JSONObject getJSON()
    {
        final JSONObject obj = new JSONObject();
        if (override)
        {
            if (embed == null)
                obj.put("embed", JSONObject.NULL);
            else
                obj.put("embed", getJSONEmbed(embed));
            if (content.length() == 0)
                obj.put("content", JSONObject.NULL);
            else
                obj.put("content", content.toString());
            if (nonce == null)
                obj.put("nonce", JSONObject.NULL);
            else
                obj.put("nonce", nonce);
            obj.put("tts", tts);
        }
        else
        {
            if (embed != null)
                obj.put("embed", getJSONEmbed(embed));
            if (content.length() > 0)
                obj.put("content", content.toString());
            if (nonce != null)
                obj.put("nonce", nonce);
            obj.put("tts", tts);
        }
        return obj;
    }

    protected static JSONObject getJSONEmbed(final MessageEmbed embed)
    {
        return embed.toJSONObject();
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
            throw new InsufficientPermissionException(perm);
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
    protected void handleResponse(Response response, Request<Message> request)
    {
        if (response.isOk())
            request.onSuccess(api.getEntityBuilder().createMessage(response.getObject(), channel, false));
        else
            request.onFailure(response);
    }

    @Override
    @Deprecated /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */
    protected void finalize()
    {
        if (ownedResources.isEmpty())
            return;
        LOG.warn("Found unclosed resources in MessageAction instance, closing on finalization step!");
        clearResources();
    }
}
