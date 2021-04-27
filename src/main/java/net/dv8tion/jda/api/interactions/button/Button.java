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
    Style getStyle();

    @Nullable
    String getUrl();

    boolean isDisabled();

    @Nonnull
    default Button disable()
    {
        return new ButtonImpl(getId(), getLabel(), getStyle(), getUrl(), true);
    }

    @Nonnull
    default Button enable()
    {
        return new ButtonImpl(getId(), getLabel(), getStyle(), getUrl(), false);
    }

    @Nonnull
    static Button primary(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(id, label, Style.PRIMARY, false);
    }

    @Nonnull
    static Button secondary(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(id, label, Style.SECONDARY, false);
    }


    @Nonnull
    static Button success(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(id, label, Style.SUCCESS, false);
    }


    @Nonnull
    static Button danger(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(id, label, Style.DANGER, false);
    }


    @Nonnull
    static Button link(@Nonnull String url, @Nonnull String label)
    {
        Checks.notEmpty(url, "URL");
        Checks.notEmpty(label, "Label");
        return new ButtonImpl(null, label, Style.LINK, url, false);
    }

    enum Style
    {
        UNKNOWN(-1),
        PRIMARY(1),
        SECONDARY(2),
        SUCCESS(3),
        DANGER(4),
        LINK(5),
        ;

        private final int key;

        Style(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        @Nonnull
        public static Style fromKey(int key)
        {
            for (Style style : values())
            {
                if (style.key == key)
                    return style;
            }
            return UNKNOWN;
        }
    }

// Only really relevant for internals
//    enum Type
//    {
//        ACTION_ROW(1), BUTTON(2);
//        private final int key;
//
//        Type(int key)
//        {
//            this.key = key;
//        }
//    }
}
