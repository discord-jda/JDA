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
import net.dv8tion.jda.core.utils.Checks;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WebhookMessageBuilder
{
    protected final StringBuilder content = new StringBuilder();
    protected final List<MessageEmbed> embeds = new LinkedList<>();
    protected String username, avatarUrl, fileName;
    protected InputStream file;
    protected boolean isTTS;

    public WebhookMessageBuilder(Message message)
    {
        if (message != null)
        {
            embeds.addAll(message.getEmbeds());
            setContent(message.getRawContent());
            isTTS = message.isTTS();
        }
    }

    public WebhookMessageBuilder() {}

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

    public WebhookMessageBuilder resetEmbeds()
    {
        this.embeds.clear();
        return this;
    }

    public WebhookMessageBuilder addEmbeds(MessageEmbed... embeds)
    {
        Checks.notNull(embeds, "Embeds");
        for (MessageEmbed embed : embeds)
        {
            Checks.notNull(embed, "Embed");
            this.embeds.add(embed);
        }
        return this;
    }

    public WebhookMessageBuilder addEmbeds(Collection<MessageEmbed> embeds)
    {
        Checks.notNull(embeds, "Embeds");
        this.embeds.addAll(embeds);
        return this;
    }

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

    public WebhookMessageBuilder append(String content)
    {
        Checks.notNull(content, "Content");
        Checks.check(this.content.length() + content.length() <= 2000,
            "Content may not exceed 2000 characters!");
        this.content.append(content);
        return this;
    }

    public WebhookMessageBuilder setUsername(String username)
    {
        this.username = username;
        return this;
    }

    public WebhookMessageBuilder setAvatarUrl(String avatarUrl)
    {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public WebhookMessageBuilder setFile(File file)
    {
        Checks.notNull(file, "File");
        return setFile(file, fileName);
    }

    public WebhookMessageBuilder setFile(File file, String fileName)
    {
        Checks.notNull(file, "File");
        Checks.notBlank(fileName, "File name");
        Checks.check(file.canRead() && file.exists(),
            "File must exist and be readable!");
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

    public WebhookMessageBuilder setFile(byte[] data, String fileName)
    {
        Checks.notNull(data, "Data");
        Checks.notBlank(fileName, "File name");
        this.file = new ByteArrayInputStream(data);
        this.fileName = fileName;
        return this;
    }

    public WebhookMessageBuilder setFile(InputStream file, String fileName)
    {
        this.file = file;
        this.fileName = fileName;
        return this;
    }

    public WebhookMessageBuilder setTTS(boolean TTS)
    {
        isTTS = TTS;
        return this;
    }

    public WebhookMessage build()
    {
        return new WebhookMessage(username, avatarUrl, content.toString(), embeds, isTTS, file, fileName);
    }
}
