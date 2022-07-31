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

package net.dv8tion.jda.internal.utils.localization;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.command.localization.UnmodifiableLocalizationMap;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LocalizationUtils
{
    public static final Logger LOG = JDALogger.getLog(LocalizationUtils.class);

    @Nonnull
    public static Map<DiscordLocale, String> mapFromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "Data");

        final Map<DiscordLocale, String> map = new HashMap<>();

        for (String key : data.keys())
        {
            final DiscordLocale locale = DiscordLocale.from(key);
            if (locale == DiscordLocale.UNKNOWN)
            {
                LOG.debug("Discord provided an unknown locale, locale tag: {}", key);
                continue;
            }

            map.put(locale, data.getString(key));
        }

        return map;
    }

    @Nonnull
    public static Map<DiscordLocale, String> mapFromProperty(@Nonnull DataObject json, @Nonnull String localizationProperty)
    {
        return json.optObject(localizationProperty)
                .map(LocalizationUtils::mapFromData)
                .orElse(Collections.emptyMap());
    }

    @Nonnull
    public static LocalizationMap unmodifiableFromProperty(@Nonnull DataObject json, @Nonnull String localizationProperty)
    {
        return new UnmodifiableLocalizationMap(mapFromProperty(json, localizationProperty));
    }
}
