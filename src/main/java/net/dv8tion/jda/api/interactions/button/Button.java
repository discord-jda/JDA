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

package net.dv8tion.jda.api.interactions.button;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.Component;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Button extends Component
{
    @Nonnull
    String getLabel();

    @Nonnull
    ButtonStyle getStyle();

    @Nullable
    String getUrl();

    @Nullable
    Emoji getEmoji();

    boolean isDisabled();

    @Nonnull
    default Button asDisabled()
    {
        return new ButtonImpl(getId(), getLabel(), getStyle(), getUrl(), true, getEmoji());
    }

    @Nonnull
    default Button asEnabled()
    {
        return new ButtonImpl(getId(), getLabel(), getStyle(), getUrl(), false, getEmoji());
    }

    @Nonnull
    default Button withEmoji(@Nullable Emoji emoji)
    {
        return new ButtonImpl(getId(), getLabel(), getStyle(), getUrl(), isDisabled(), emoji);
    }

    @Nonnull
    static Button primary(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(id, label, ButtonStyle.PRIMARY, false, null);
    }

    @Nonnull
    static Button secondary(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(id, label, ButtonStyle.SECONDARY, false, null);
    }

    @Nonnull
    static Button success(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(id, label, ButtonStyle.SUCCESS, false, null);
    }

    @Nonnull
    static Button danger(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(id, label, ButtonStyle.DANGER, false, null);
    }

    @Nonnull
    static Button link(@Nonnull String url, @Nonnull String label)
    {
        Checks.notEmpty(url, "URL");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(null, label, ButtonStyle.LINK, url, false, null);
    }

    @Nonnull
    static Button of(@Nonnull ButtonStyle style, @Nonnull String idOrUrl, @Nonnull String label)
    {
        Checks.notNull(style, "Style");
        Checks.notEmpty(idOrUrl, "Id or URL");
        Checks.notNull(label, "Label");
        if (style == ButtonStyle.LINK)
            return link(idOrUrl, label);
        return new ButtonImpl(idOrUrl, label, style, false, null);
    }
}
