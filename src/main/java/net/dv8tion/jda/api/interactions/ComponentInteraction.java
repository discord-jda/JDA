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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateAction;
import net.dv8tion.jda.internal.requests.restaction.interactions.UpdateActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ComponentInteraction extends Interaction
{
    @Nonnull
    String getComponentId();

    @Nullable
    Component getComponent();

    @Nullable
    Message getMessage();

    long getMessageIdLong();

    @Nonnull
    default String getMessageId()
    {
        return Long.toUnsignedString(getMessageIdLong());
    }

    @Nonnull
    Component.Type getComponentType();

    @Nonnull
    @Override
    MessageChannel getChannel();

    @Nonnull
    @CheckReturnValue
    UpdateAction deferEdit();

    @Nonnull
    @CheckReturnValue
    default UpdateAction editMessage(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        UpdateActionImpl action = (UpdateActionImpl) deferEdit();
        return action.applyMessage(message);
    }

    @Nonnull
    @CheckReturnValue
    default UpdateAction editMessage(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return deferEdit().setContent(content);
    }

    @Nonnull
    @CheckReturnValue
    default UpdateAction editMessage(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        Checks.notNull(embed, "MessageEmbed");
        Checks.noneNull(embeds, "MessageEmbed");
        return deferEdit().setEmbeds(embeds);
    }

    @Nonnull
    @CheckReturnValue
    default UpdateAction editMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return editMessage(String.format(format, args));
    }

}
