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

package net.dv8tion.jda.api.interactions.commands.localization;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class which maps user-provided translations (from a resource bundle for example) to the command data as well as everything contained in it
 * <p>You can find default localization functions in {@link ResourceBundleLocalizationFunction}
 *
 * @see CommandData#setLocalizationMapper(LocalizationMapper)
 * @see ResourceBundleLocalizationFunction
 */
public class LocalizationMapper
{
    private final LocalizationFunction localizationFunction;

    private LocalizationMapper(LocalizationFunction localizationFunction) {
        this.localizationFunction = localizationFunction;
    }

    /**
     * Creates a new {@link LocalizationMapper} from the given {@link LocalizationFunction}
     *
     * @param  localizationFunction
     *         The {@link LocalizationFunction} to use
     *
     * @return The {@link LocalizationMapper} instance
     *
     * @see ResourceBundleLocalizationFunction
     */
    @Nonnull
    public static LocalizationMapper fromFunction(@Nonnull LocalizationFunction localizationFunction) {
        return new LocalizationMapper(localizationFunction);
    }

    private class TranslationContext
    {
        private final Stack<String> keyComponents = new Stack<>();

        private void forObjects(DataArray source, Function<DataObject, String> keyExtractor, Consumer<DataObject> consumer)
        {
            for (int i = 0; i < source.length(); i++)
            {
                final DataObject item = source.getObject(i);
                final String key = keyExtractor.apply(item);
                keyComponents.push(key);
                consumer.accept(item);
                keyComponents.pop();
            }
        }

        private void withKey(String key, Runnable runnable)
        {
            keyComponents.push(key);
            runnable.run();
            keyComponents.pop();
        }

        private String getKey(String finalComponent)
        {
            final StringJoiner joiner = new StringJoiner(".");
            for (String keyComponent : keyComponents)
                joiner.add(keyComponent.replace(" ", "_")); //Context commands can have spaces, we need to replace them
            joiner.add(finalComponent.replace(" ", "_"));
            return joiner.toString().toLowerCase();
        }

        private void trySetTranslation(LocalizationMap localizationMap, String finalComponent)
        {
            final String key = getKey(finalComponent);
            final Map<DiscordLocale, String> data = localizationFunction.apply(key);
            localizationMap.setTranslations(data);
        }

        private void trySetTranslation(DataObject localizationMap, String finalComponent)
        {
            final String key = getKey(finalComponent);
            final Map<DiscordLocale, String> data = localizationFunction.apply(key);
            data.forEach((locale, localizedValue) ->
            {
                Checks.check(locale != DiscordLocale.UNKNOWN, "Localization function returned a map with an 'UNKNOWN' DiscordLocale");

                localizationMap.put(locale.getLocale(), localizedValue);
            });
        }
    }

    //Internal
    public void localizeCommand(CommandData commandData, DataArray optionArray)
    {
        final TranslationContext ctx = new TranslationContext();
        ctx.withKey(commandData.getName(), () ->
        {
            ctx.trySetTranslation(commandData.getNameLocalizations(), "name");
            if (commandData instanceof SlashCommandData)
            {
                final SlashCommandData slashCommandData = (SlashCommandData) commandData;
                ctx.trySetTranslation(slashCommandData.getDescriptionLocalizations(), "description");
                localizeOptionArray(optionArray, ctx);
            }
        });
    }

    private void localizeOptionArray(DataArray optionArray, TranslationContext ctx)
    {
        ctx.forObjects(optionArray, o -> o.getString("name"), obj ->
        {
            if (obj.hasKey("name_localizations"))
                ctx.trySetTranslation(obj.getObject("name_localizations"), "name");
            if (obj.hasKey("description_localizations"))
                ctx.trySetTranslation(obj.getObject("description_localizations"), "description");
            if (obj.hasKey("options"))
                localizeOptionArray(obj.getArray("options"), ctx);
            if (obj.hasKey("choices"))
                localizeOptionArray(obj.getArray("choices"), ctx);
        });
    }
}