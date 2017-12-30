/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

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
    protected final String username, avatarUrl, content, fileName;
    protected final List<MessageEmbed> embeds;
    protected final boolean isTTS;
    protected final InputStream file;

    protected WebhookMessage(@Nullable final String username, @Nullable final String avatarUrl, final String content,
                             final List<MessageEmbed> embeds, final boolean isTTS,
                             @Nullable final InputStream file, @Nullable final String fileName)
    {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.embeds = embeds;
        this.isTTS = isTTS;
        this.file = file;
        this.fileName = fileName;
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
        return new WebhookMessage(null, null, content, embeds, isTTS, null, null);
    }

    /**
     * Whether this message contains an attachment
     *
     * @return True, if this message contains an attachment
     */
    public boolean isFile()
    {
        return file != null;
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
            return builder.addFormDataPart("file", fileName, MiscUtil.createRequestBody(OCTET, file))
                          .addFormDataPart("payload_json", payload.toString()).build();
        }
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, payload.toString());
    }
}
