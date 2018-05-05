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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A special Message that can only be sent to a {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient}.
 * <br>This message provides special attributes called {@code username} and {@code avatar_url} which override
 * the default username and avatar of a Webhook message.
 *
 * <p>This message can send multiple embeds at once!
 */
public class WebhookMessage
{
    protected static final MediaType OCTET = MediaType.parse("application/octet-stream");
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
     */
    public static WebhookMessage of(MessageEmbed... embeds)
    {
        return new WebhookMessageBuilder().addEmbeds(embeds).build();
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
    public static WebhookMessage of(Collection<MessageEmbed> embeds)
    {
        return new WebhookMessageBuilder().addEmbeds(embeds).build();
    }

    public static WebhookMessage of(Map<String, ?> attachments) throws FileNotFoundException
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

    public static WebhookMessage of(Object... attachments) throws FileNotFoundException
    {
        Checks.notNull(attachments, "Attachments");
        Checks.notEmpty(attachments, "Attachments");
        Checks.check(attachments.length % 2 == 0, "Must provide even number of arguments");
        int fileAmount = attachments.length / 2;
        Checks.check(fileAmount <= WebhookMessageBuilder.MAX_FILES, "Cannot add more than %d files to a message", WebhookMessageBuilder.MAX_FILES);
        MessageAttachment[] files = new MessageAttachment[fileAmount];
        for (int i = 0, j = 0; i < attachments.length; j++, i += 2)
        {
            Object name = attachments[i];
            Object data = attachments[i+1];
            if (!(name instanceof String))
                throw new IllegalArgumentException("Provided arguments must be pairs for (String, Data)");
            files[j] = convertAttachment((String) name, data);
        }
        return new WebhookMessage(null, null, null, null, false, files);
    }

    /**
     * Creates a new WebhookMessage instance with the provided {@link net.dv8tion.jda.core.entities.Message Message}
     * <br><b>This does not copy the attachments of the provided message!</b>
     *
     * @param  message
     *         The message to use for this message
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
        if (!embeds.isEmpty())
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
                builder.addFormDataPart("file" + i, attachment.name, MiscUtil.createRequestBody(OCTET, attachment.data));
            }
            return builder.addFormDataPart("payload_json", payload.toString()).build();
        }
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, payload.toString());
    }

    private static MessageAttachment convertAttachment(String name, Object data) throws FileNotFoundException
    {
        Checks.notNull(data, "Data");
        MessageAttachment a;
        if (data instanceof File)
            a = new MessageAttachment(name, new FileInputStream((File) data));
        else if (data instanceof InputStream)
            a = new MessageAttachment(name, (InputStream) data);
        else if (data instanceof byte[])
            a = new MessageAttachment(name, new ByteArrayInputStream((byte[]) data));
        else
            throw new IllegalArgumentException("Provided arguments must be pairs for (String, Data). Unexpected data type " + data.getClass().getName());
        return a;
    }
}
