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

package net.dv8tion.jda.webhook;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A special Message that can only be sent to a {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient}.
 * <br>This message provides special attributes called {@code username} and {@code avatar_url} which override
 * the default username and avatar of a Webhook message.
 *
 * <p>This message can send multiple embeds at once!
 */
public class WebhookMessage
{
    protected final String username, avatarUrl, content;
    protected final List<MessageEmbed> embeds;
    protected final boolean isTTS;
    protected final MessageAttachment[] attachments;

    protected WebhookMessage(final String username, final String avatarUrl, final String content,
                             final List<MessageEmbed> embeds, final boolean isTTS,
                             final MessageAttachment[] files)
    {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.embeds = embeds;
        this.isTTS = isTTS;
        this.attachments = files;
    }

    /**
     * Creates a new WebhookMessage instance with the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     *
     * @param  embeds
     *         The embeds to use for this message
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null}
     *         or exceeds the maximum total character count of {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}
     *
     * @return The resulting WebhookMessage instance
     *
     * @deprecated
     *         Use {@link #embeds(MessageEmbed, MessageEmbed...)} instead.
     */
    @Deprecated
    public static WebhookMessage of(MessageEmbed... embeds)
    {
        Checks.notEmpty(embeds, "Embeds");
        if (embeds.length > 1)
            return embeds(Arrays.asList(embeds));
        return embeds(embeds[0]);
    }

    /**
     * Creates a new WebhookMessage instance with the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     *
     * @param  embeds
     *         The embeds to use for this message
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null}
     *         or exceeds the maximum total character count of {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}
     *
     * @return The resulting WebhookMessage instance
     *
     * @deprecated
     *         Use {@link #embeds(Collection)} instead.
     */
    @Deprecated
    public static WebhookMessage of(Collection<MessageEmbed> embeds)
    {
        return embeds(embeds);
    }

    /**
     * Creates a new WebhookMessage instance with the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     *
     * @param  first
     *         The first embed to use for this message
     * @param  embeds
     *         The other embeds to use for this message
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null}
     *         or exceeds the maximum total character count of {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}
     *
     * @return The resulting WebhookMessage instance
     */
    // forcing first embed as we expect at least one entry (Effective Java 3rd. Edition - Item 53)
    public static WebhookMessage embeds(MessageEmbed first, MessageEmbed... embeds)
    {
        Checks.notNull(first,   "Embeds");
        Checks.noneNull(embeds, "Embeds");
        List<MessageEmbed> list = new ArrayList<>(1 + embeds.length);
        list.add(first);
        Collections.addAll(list, embeds);
        return new WebhookMessage(null, null, null, list, false, null);
    }

    /**
     * Creates a new WebhookMessage instance with the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     *
     * @param  embeds
     *         The embeds to use for this message
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null}
     *         or exceeds the maximum total character count of {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}
     *
     * @return The resulting WebhookMessage instance
     */
    public static WebhookMessage embeds(Collection<MessageEmbed> embeds)
    {
        Checks.notEmpty(embeds, "Embeds");
        Checks.noneNull(embeds, "Embeds");
        return new WebhookMessage(null, null, null, new ArrayList<>(embeds), false, null);
    }

    /**
     * Constructs a message around pairs of (Name, Data).
     * <br>You can add up to {@value WebhookMessageBuilder#MAX_FILES} attachments to one message.
     * <br>The supported data types are {@link InputStream}, {@link File}, and {@code byte[]}.
     *
     * <h2>Example</h2>
     * <pre><code>
     * {@literal Map<String, Object>} map = {@literal new HashMap<>()};
     * map.put("dog", new File("dog.png"));
     * map.put("cat", new URL("https://random.cat/meow").openStream());
     * map.put("bird", new byte[100]);
     * WebhookMessage message = WebhookMessage.files(map);
     * </code></pre>
     *
     * @param  attachments
     *         Map containing pairs from file names to data streams
     *
     * @throws IllegalArgumentException
     *         If one of the provided files is not readable
     *         or the provided map is null/empty
     *
     * @return WebhookMessage for the provided files
     *
     * @see    WebhookMessageBuilder#addFile(String, InputStream)
     * @see    WebhookMessageBuilder#addFile(String, byte[])
     * @see    WebhookMessageBuilder#addFile(String, File)
     * @see    WebhookMessageBuilder#addFile(File)
     */
    public static WebhookMessage files(Map<String, ?> attachments)
    {
        Checks.notNull(attachments, "Attachments");
        Set<? extends Map.Entry<String, ?>> entries = attachments.entrySet();
        Checks.notEmpty(entries, "Attachments");
        int fileAmount = attachments.size();
        Checks.check(fileAmount <= WebhookMessageBuilder.MAX_FILES, "Cannot add more than %d files to a message", WebhookMessageBuilder.MAX_FILES);
        MessageAttachment[] files = new MessageAttachment[fileAmount];
        int i = 0;
        for (Map.Entry<String, ?> attachment : entries)
        {
            String name = attachment.getKey();
            Checks.notEmpty(name, "Name");
            Object data = attachment.getValue();
            files[i++] = convertAttachment(name, data);
        }
        return new WebhookMessage(null, null, null, null, false, files);
    }

    /**
     * Constructs a message around pairs of (Name, Data).
     * <br>You can add up to {@value WebhookMessageBuilder#MAX_FILES} attachments to one message. This means you can have up to 20 arguments.
     * <br>The supported data types are {@link InputStream}, {@link File}, and {@code byte[]}.
     *
     * <h2>Example</h2>
     * <pre><code>
     * WebhookMessage message = WebhookMessage.files(
     *     "dog", new File("dog.png"),
     *     "cat", new URL("https://random.cat/meow").openStream(),
     *     "bird", new byte[100]
     * );
     * </code></pre>
     *
     * @param  name1
     *         The first name argument
     * @param  data1
     *         The first data argument
     * @param  attachments
     *         Additional pairs in the form of (Name, Data)
     *
     * @throws IllegalArgumentException
     *         If the provided arguments are not pairs of (Name, Data)
     *         or if one of the provided files is not readable
     *
     * @return WebhookMessage for the provided files
     *
     * @see    WebhookMessageBuilder#addFile(String, InputStream)
     * @see    WebhookMessageBuilder#addFile(String, byte[])
     * @see    WebhookMessageBuilder#addFile(String, File)
     * @see    WebhookMessageBuilder#addFile(File)
     */
    // forcing first pair as we expect at least one entry (Effective Java 3rd. Edition - Item 53)
    public static WebhookMessage files(String name1, Object data1, Object... attachments)
    {
        Checks.notBlank(name1, "Name");
        Checks.notNull(data1,  "Data");
        Checks.notNull(attachments, "Attachments");
        Checks.check(attachments.length % 2 == 0, "Must provide even number of varargs arguments");
        int fileAmount = 1 + attachments.length / 2;
        Checks.check(fileAmount <= WebhookMessageBuilder.MAX_FILES, "Cannot add more than %d files to a message", WebhookMessageBuilder.MAX_FILES);
        MessageAttachment[] files = new MessageAttachment[fileAmount];
        files[0] = convertAttachment(name1, data1);
        for (int i = 0, j = 1; i < attachments.length; j++, i += 2)
        {
            Object name = attachments[i];
            Object data = attachments[i+1];
            if (!(name instanceof String))
                throw new IllegalArgumentException("Provided arguments must be pairs for (String, Data). Expected String and found " + (name == null ? null : name.getClass().getName()));
            files[j] = convertAttachment((String) name, data);
        }
        return new WebhookMessage(null, null, null, null, false, files);
    }

    /**
     * Creates a new WebhookMessage instance with the provided {@link net.dv8tion.jda.core.entities.Message Message}
     * as layout for copying.
     * <br><b>This does not copy the attachments of the provided message!</b>
     *
     * @param  message
     *         The message to copy
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided message is {@code null}
     *
     * @return The resulting WebhookMessage instance
     */
    public static WebhookMessage from(Message message)
    {
        Checks.notNull(message, "Message");
        final String content = message.getContentRaw();
        final List<MessageEmbed> embeds = message.getEmbeds();
        final boolean isTTS = message.isTTS();
        return new WebhookMessage(null, null, content, embeds, isTTS, null);
    }

    /**
     * Whether this message contains an attachment
     *
     * @return True, if this message contains an attachment
     */
    public boolean isFile()
    {
        return attachments != null;
    }

    protected RequestBody getBody()
    {
        final JSONObject payload = new JSONObject();
        if (content != null)
            payload.put("content", content);
        if (embeds != null && !embeds.isEmpty())
        {
            final JSONArray array = new JSONArray();
            for (MessageEmbed embed : embeds)
                array.put(embed.toJSONObject());
            payload.put("embeds", array);
        }
        if (avatarUrl != null)
            payload.put("avatar_url", avatarUrl);
        if (username != null)
            payload.put("username", username);
        payload.put("tts", isTTS);
        if (isFile())
        {
            final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            for (int i = 0; i < attachments.length; i++)
            {
                final MessageAttachment attachment = attachments[i];
                if (attachment == null)
                    break;
                builder.addFormDataPart("file" + i, attachment.name, MiscUtil.createRequestBody(Requester.MEDIA_TYPE_OCTET, attachment.getData()));
            }
            return builder.addFormDataPart("payload_json", payload.toString()).build();
        }
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, payload.toString());
    }

    private static MessageAttachment convertAttachment(String name, Object data)
    {
        Checks.notNull(data, "Data");
        try
        {
            MessageAttachment a;
            if (data instanceof File)
                a = new MessageAttachment(name, (File) data);
            else if (data instanceof InputStream)
                a = new MessageAttachment(name, (InputStream) data);
            else if (data instanceof byte[])
                a = new MessageAttachment(name, (byte[]) data);
            else
                throw new IllegalArgumentException("Provided arguments must be pairs for (String, Data). Unexpected data type " + data.getClass().getName());
            return a;
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
}
