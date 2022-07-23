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
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface MessageRequest<R extends MessageRequest<R>> extends AllowedMentions<R>
{
    @Nonnull
    R setContent(@Nullable String content);

    @Nonnull
    String getContent();

    @Nonnull
    R setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    @Nonnull
    default R setEmbeds(@Nonnull MessageEmbed... embeds)
    {
        return setEmbeds(Arrays.asList(embeds));
    }

    @Nonnull
    List<? extends MessageEmbed> getEmbeds();

    @Nonnull
    R setComponents(@Nonnull Collection<? extends LayoutComponent> layouts);

    @Nonnull
    default R setComponents(@Nonnull LayoutComponent... components)
    {
        return setComponents(Arrays.asList(components));
    }

    @Nonnull
    default R setActionRow(@Nonnull Collection<? extends ItemComponent> components)
    {
        return setComponents(ActionRow.of(components));
    }

    @Nonnull
    default R setActionRow(@Nonnull ItemComponent... components)
    {
        return setComponents(ActionRow.of(components));
    }

    @Nonnull
    List<? extends LayoutComponent> getComponents();

    @Nonnull
    R setFiles(@Nullable Collection<? extends FileUpload> files);

    @Nonnull
    default R setFiles(@Nonnull FileUpload... files)
    {
        Checks.noneNull(files, "Files");
        return setFiles(Arrays.asList(files));
    }

    // Returns attachment interface for abstraction purposes, however you can only abstract the setter to allow FileUploads

    @Nonnull
    List<? extends AttachedFile> getAttachments();

    @Nonnull
    R applyMessage(@Nonnull Message message);
}
