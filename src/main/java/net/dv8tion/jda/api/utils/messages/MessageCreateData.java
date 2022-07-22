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

package net.dv8tion.jda.api.utils.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.AllowedMentionsImpl;
import net.dv8tion.jda.internal.utils.IOUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class MessageCreateData implements SerializableData, AutoCloseable
{
    private final String content;
    private final List<MessageEmbed> embeds;
    private final List<FileUpload> files;
    private final List<LayoutComponent> components;
    private final AllowedMentionsImpl allowedMentions;
    private final boolean tts;

    protected MessageCreateData(
            String content,
            List<MessageEmbed> embeds, List<FileUpload> files, List<LayoutComponent> components,
            AllowedMentionsImpl allowedMentions, boolean tts)
    {
        this.content = content;
        this.embeds = Collections.unmodifiableList(embeds);
        this.files = Collections.unmodifiableList(files);
        this.components = Collections.unmodifiableList(components);
        this.allowedMentions = allowedMentions;
        this.tts = tts;
    }

    @Nonnull
    public static MessageCreateData fromContent(@Nonnull String content)
    {
        return new MessageCreateBuilder().setContent(content).build();
    }

    @Nonnull
    public static MessageCreateData fromEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return new MessageCreateBuilder().setEmbeds(embeds).build();
    }

    @Nonnull
    public static MessageCreateData fromFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        return new MessageCreateBuilder().setFiles(files).build();
    }

    public String getContent()
    {
        return content;
    }

    public List<MessageEmbed> getEmbeds()
    {
        return embeds;
    }

    public List<LayoutComponent> getComponents()
    {
        return components;
    }

    public boolean isTTS()
    {
        return tts;
    }

    @Nonnull
    public Set<String> getMentionedUsers()
    {
        return allowedMentions.getUsers();
    }

    @Nonnull
    public Set<String> getMentionedRoles()
    {
        return allowedMentions.getRoles();
    }

    @Nonnull
    public EnumSet<Message.MentionType> getAllowedMentions()
    {
        return allowedMentions.getAllowedMentions();
    }

    public boolean isMentionRepliedUser()
    {
        return allowedMentions.isMentionRepliedUser();
    }

    @Nonnull
    @Override
    public synchronized DataObject toData()
    {
        DataObject json = DataObject.empty();
        json.put("content", content);
        json.put("embeds", DataArray.fromCollection(embeds));
        json.put("components", DataArray.fromCollection(components));
        json.put("tts", tts);
        json.put("allowed_mentions", allowedMentions);
        if (files != null && !files.isEmpty())
        {
            DataArray attachments = DataArray.empty();
            json.put("attachments", attachments);
            for (int i = 0; i < files.size(); i++)
                attachments.add(files.get(i).toAttachmentData(i));
        }

        return json;
    }

    @Nonnull
    public synchronized List<FileUpload> getFiles()
    {
        return files;
    }

    @Override
    public synchronized void close()
    {
        files.forEach(IOUtil::silentClose);
    }
}
