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

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Builder for a {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage}
 */
public class WebhookMessageBuilder
{
    protected final StringBuilder content = new StringBuilder();
    protected final List<MessageEmbed> embeds = new LinkedList<>();
    protected String username, avatarUrl, fileName;
    protected InputStream file;
    protected boolean isTTS;

    /**
     * Creates a new WebhookMessageBuilder and applies
     * the information of the provided {@link net.dv8tion.jda.core.entities.Message Message}
     * as preset values.
     * <br>This will not copy any attachments!
     *
     * @param  message
     *         The {@link net.dv8tion.jda.core.entities.Message Message} used
     *         to set initial values of the builder
     */
    public WebhookMessageBuilder(Message message)
    {
        if (message != null)
        {
            embeds.addAll(message.getEmbeds());
            setContent(message.getRawContent());
            isTTS = message.isTTS();
        }
    }

    /**
     * Creates a new empty WebhookMessageBuilder
     */
    public WebhookMessageBuilder() {}

    /**
     * Whether this WebhookMessageBuilder contains any readable content.
     * <br>When the builder is empty it cannot successfully build a {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage}.
     *
     * @return True, if this builder has no readable content
     */
    public boolean isEmpty()
    {
        return content.length() == 0 && embeds.isEmpty() && file == null;
    }

    /**
     * Resets this builder to default settings.
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder reset()
    {
        content.setLength(0);
        embeds.clear();
        username = null;
        avatarUrl = null;
        fileName = null;
        file = null;
        isTTS = false;
        return this;
    }

    /**
     * Removes all embeds from this builder.
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder resetEmbeds()
    {
        this.embeds.clear();
        return this;
    }

    /**
     * Adds the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     * to this builder.
     *
     * <p><b>You can send up to 10 embeds per message! If more are sent they will not be displayed.</b>
     *
     * @param  embeds
     *         The embeds to add
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null}
     *         or exceeds the maximum total character count of {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder addEmbeds(MessageEmbed... embeds)
    {
        Checks.notNull(embeds, "Embeds");
        for (MessageEmbed embed : embeds)
        {
            Checks.notNull(embed, "Embed");
            Checks.check(embed.isSendable(AccountType.BOT),
                "One of the provided embeds exceeds the maximum character count of %d!", MessageEmbed.EMBED_MAX_LENGTH_BOT);
            this.embeds.add(embed);
        }
        return this;
    }

    /**
     * Adds the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     * to this builder.
     *
     * <p><b>You can send up to 10 embeds per message! If more are sent they will not be displayed.</b>
     *
     * @param  embeds
     *         The embeds to add
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null}
     *         or exceeds the maximum total character count of {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder addEmbeds(Collection<MessageEmbed> embeds)
    {
        Checks.notNull(embeds, "Embeds");
        for (MessageEmbed embed : embeds)
        {
            Checks.notNull(embed, "Embed");
            Checks.check(embed.isSendable(AccountType.BOT),
                "One of the provided embeds exceeds the maximum character count of %d!", MessageEmbed.EMBED_MAX_LENGTH_BOT);
            this.embeds.add(embed);
        }
        return this;
    }

    /**
     * Sets the content of the resulting message.
     * <br>This will override the previous content.
     *
     * @param  content
     *         The new content
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided content exceeds {@code 2000} characters in length
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder setContent(String content)
    {
        Checks.check(content == null || content.length() <= 2000,
            "Content may not exceed 2000 characters!");
        if (content != null)
            this.content.replace(0, content.length(), content);
        else
            this.content.setLength(0);
        return this;
    }

    /**
     * Appends to the currently set content of the resulting message.
     *
     * @param  content
     *         The content to append
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided content is {@code null} or
     *         the resulting content would exceed {@code 2000} characters in length
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder append(String content)
    {
        Checks.notNull(content, "Content");
        Checks.check(this.content.length() + content.length() <= 2000,
            "Content may not exceed 2000 characters!");
        this.content.append(content);
        return this;
    }

    /**
     * Sets the username that should be used for the resulting message.
     * <br>This will override the default username of the webhook.
     *
     * @param  username
     *         The username to use for this message
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder setUsername(String username)
    {
        this.username = Helpers.isBlank(username) ? null : username;
        return this;
    }

    /**
     * Sets the avatar url that should be used for the resulting message.
     * <br>This will override the default avatar of the webhook.
     *
     * @param  avatarUrl
     *         The avatar url to use for this message
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder setAvatarUrl(String avatarUrl)
    {
        this.avatarUrl = Helpers.isBlank(avatarUrl) ? null : avatarUrl;
        return this;
    }

    /**
     * Sets the attached file for the resulting message.
     *
     * @param  file
     *         The {@link java.io.File File} that should be attached to the message
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null}, does not exist, is not readable
     *         or exceeds the maximum size of 8MB
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder setFile(File file)
    {
        return setFile(file, file == null ? null : file.getName());
    }

    /**
     * Sets the attached file for the resulting message.
     *
     * @param  file
     *         The {@link java.io.File File} that should be attached to the message
     * @param  fileName
     *         The name that should be used for this attachment
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is does not exist, is not readable
     *         or exceeds the maximum size of 8MB
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder setFile(File file, String fileName)
    {
        if (file == null)
        {
            this.file = null;
            this.fileName = null;
            return this;
        }
        Checks.check(file.canRead() && file.exists(), "File must exist and be readable!");
        Checks.notBlank(fileName, "File name");
        Checks.check(file.length() <= Message.MAX_FILE_SIZE, "Provided data exceeds the maximum size of 8MB!");
        try
        {
            this.file = new FileInputStream(file);
            this.fileName = fileName;
        }
        catch (FileNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Sets the attached file for the resulting message.
     *
     * @param  data
     *         The {@code byte[]} data that should be attached to the message
     * @param  fileName
     *         The name that should be used for this attachment
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided data exceeds the maximum size of 8MB
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder setFile(byte[] data, String fileName)
    {
        if (data == null)
        {
            this.fileName = null;
            this.file = null;
            return this;
        }
        Checks.notBlank(fileName, "File name");
        Checks.check(data.length <= Message.MAX_FILE_SIZE, "Provided data exceeds the maximum size of 8MB!");
        this.file = new ByteArrayInputStream(data);
        this.fileName = fileName;
        return this;
    }

    /**
     * Sets the attached file for the resulting message.
     *
     * @param  data
     *         The {@link java.io.InputStream InputStream} data that should be attached to the message
     * @param  fileName
     *         The name that should be used for this attachment
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder setFile(InputStream data, String fileName)
    {
        Checks.check(data == null || !Helpers.isBlank(fileName),
            "The provided file name must not be null, empty or blank!");
        this.file = data;
        this.fileName = fileName;
        return this;
    }

    /**
     * Sets whether the resulting message should use Text-To-Speech.
     *
     * @param  tts
     *         True, if the resulting message should use Text-To-Speech
     *
     * @return The current WebhookMessageBuilder for chaining convenience
     */
    public WebhookMessageBuilder setTTS(boolean tts)
    {
        isTTS = tts;
        return this;
    }

    /**
     * Builds a {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage} instance
     * with the current state of this builder.
     *
     * @throws java.lang.IllegalStateException
     *         If this builder is empty
     *
     * @return The resulting {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage}
     */
    public WebhookMessage build()
    {
        if (isEmpty())
            throw new IllegalStateException("Cannot build an empty message!");
        return new WebhookMessage(username, avatarUrl, content.toString(), embeds, isTTS, file, fileName);
    }
}
