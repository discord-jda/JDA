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

import javax.annotation.Nullable;
import java.util.Locale;

//TODO docs
public enum DiscordLocale
{
    UK("en-GB","English, UK","English, UK"),
    US("en-US","English, US","English, US"),
    FRENCH("fr","French","Français");
    //TODO add langs

    private final String locale;
    private final String languageName;
    private final String nativeName;

    DiscordLocale(String locale, String languageName, String nativeName)
    {
        this.locale = locale;
        this.languageName = languageName;
        this.nativeName = nativeName;
    }

    //TODO docs
    public String getLocale()
    {
        return locale;
    }

    //TODO docs
    public String getLanguageName()
    {
        return languageName;
    }

    //TODO docs
    public String getNativeName()
    {
        return nativeName;
    }

    //TODO docs
    @Nullable
    public static DiscordLocale from(String localeTag) {
        for (DiscordLocale discordLocale : values())
        {
            if (discordLocale.locale.equals(localeTag)) {
                return discordLocale;
            }
        }

        return null;
    }

    //TODO docs
    @Nullable
    public static DiscordLocale from(Locale locale) {
        return from(locale.toLanguageTag());
    }
}
