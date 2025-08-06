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

package net.dv8tion.jda.internal.interactions.command.localization;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.attributes.IDescribedCommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;

/*
 * Utility class which maps user-provided translations (from a resource bundle for example) to the command data as well as everything contained in it.
 * This class essentially wraps a {@link LocalizationFunction} to ask the localization function for translations based on command definitions defined in code.
 * The real brain of this system lies in the localization function.
 * The localization function is where the developer can define <i>how</i> to get translations for various parts of commands.
 * The LocalizationMapper is effectively just the context organizer between command definitions and getting their translations.
 *
 * <p>You can find a prebuilt localization function that uses {@link java.util.ResourceBundle ResourceBundles} at {@link ResourceBundleLocalizationFunction}.
 *
 * @see LocalizationFunction
 * @see ResourceBundleLocalizationFunction
 */
public class LocalizationMapper
{
    private final LocalizationFunction localizationFunction;

    private LocalizationMapper(LocalizationFunction localizationFunction) {
        this.localizationFunction = localizationFunction;
    }

    /*
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

    public void localizeCommand(CommandData commandData, DataArray optionArray)
    {
        final TranslationContext ctx = new TranslationContext();
        ctx.withKey(commandData.getName(), () ->
        {
            ctx.trySetTranslation(commandData.getNameLocalizations(), "name");
            if (commandData instanceof IDescribedCommandData)
            {
                final IDescribedCommandData describedCommandData = (IDescribedCommandData) commandData;
                ctx.trySetTranslation(describedCommandData.getDescriptionLocalizations(), "description");
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
                //Puts "choices" between the option name and the choice name
                // This makes it more distinguishable in tree structures
                ctx.withKey("choices", () -> localizeOptionArray(obj.getArray("choices"), ctx));
        });
    }

    private class TranslationContext
    {
        private final Stack<String> keyComponents = new Stack<>();

        private void forObjects(DataArray source, Function<DataObject, String> keyExtractor, Consumer<DataObject> consumer)
        {
            for (int i = 0; i < source.length(); i++)
            {
                final DataObject item = source.getObject(i);
                final Runnable runnable = () ->
                {
                    final String key = keyExtractor.apply(item);
                    keyComponents.push(key);
                    consumer.accept(item);
                    keyComponents.pop();
                };

                //We need to differentiate subcommands/groups from options before inserting the "options" separator
                final OptionType type = OptionType.fromKey(item.getInt("type", -1)); //-1 when the object isn't an option
                final boolean isOption = type != OptionType.SUB_COMMAND && type != OptionType.SUB_COMMAND_GROUP && type != OptionType.UNKNOWN;
                if (isOption) {
                    //At this point the key should look like "path.to.command",
                    // we can insert "options", and the keyExtractor would give option names

                    //Put "options" between the command name and the option name
                    // This makes it more distinguishable in tree structures
                    withKey("options", runnable);
                } else {
                    runnable.run();
                }
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
            try
            {
                final Map<DiscordLocale, String> data = localizationFunction.apply(key);
                localizationMap.setTranslations(data);
            }
            catch (Exception e)
            {
                throw new RuntimeException("An uncaught exception occurred while using a LocalizationFunction, localization key: '" + key + "'", e);
            }
        }

        private void trySetTranslation(DataObject localizationMap, String finalComponent)
        {
            final String key = getKey(finalComponent);
            try
            {
                final Map<DiscordLocale, String> data = localizationFunction.apply(key);
                data.forEach((locale, localizedValue) ->
                {
                    Checks.check(locale != DiscordLocale.UNKNOWN, "Localization function returned a map with an 'UNKNOWN' DiscordLocale");

                    localizationMap.put(locale.getLocale(), localizedValue);
                });
            }
            catch (Exception e)
            {
                throw new RuntimeException("An uncaught exception occurred while using a LocalizationFunction, localization key: '" + key + "'", e);
            }
        }
    }
}
