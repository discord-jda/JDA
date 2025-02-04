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

import net.dv8tion.jda.api.exceptions.LocalizationException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;

import javax.annotation.Nonnull;
import java.util.List;
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

    public void localizeCommand(CommandData commandData)
    {
        final TranslationContext ctx = new TranslationContext();
        ctx.withKey(commandData.getName(), () ->
        {
            ctx.trySetTranslation(commandData.getNameLocalizations(), "name");
            if (commandData.getType() == Command.Type.SLASH)
            {
                final SlashCommandData slashCommandData = (SlashCommandData) commandData;
                ctx.trySetTranslation(slashCommandData.getDescriptionLocalizations(), "description");

                localizeOptions(ctx, slashCommandData.getOptions());
                localizeSubcommands(ctx, slashCommandData.getSubcommands());
                ctx.forEach(slashCommandData.getSubcommandGroups(), SubcommandGroupData::getName, subcommandGroup ->
                {
                    ctx.trySetTranslation(subcommandGroup.getNameLocalizations(), "name");
                    ctx.trySetTranslation(subcommandGroup.getDescriptionLocalizations(), "description");

                    localizeSubcommands(ctx, subcommandGroup.getSubcommands());
                });
            }
        });
    }

    private static void localizeSubcommands(TranslationContext ctx, List<SubcommandData> subcommands)
    {
        ctx.forEach(subcommands, SubcommandData::getName, subcommand ->
        {
            ctx.trySetTranslation(subcommand.getNameLocalizations(), "name");
            ctx.trySetTranslation(subcommand.getDescriptionLocalizations(), "description");
            localizeOptions(ctx, subcommand.getOptions());
        });
    }

    private static void localizeOptions(TranslationContext ctx, List<OptionData> options)
    {
        // <my.command.path>.options.<option_name>.(name|description)
        ctx.withKey("options", () ->
            ctx.forEach(options, OptionData::getName, option ->
            {
                ctx.trySetTranslation(option.getNameLocalizations(), "name");
                ctx.trySetTranslation(option.getDescriptionLocalizations(), "description");

                // <my.command.path>.options.<option_name>.choices.<choice_name>.name
                ctx.withKey("choices", () ->
                    ctx.forEach(option.getChoices(),
                            Command.Choice::getName,
                            choice -> ctx.trySetTranslation(choice.getNameLocalizations(), "name")
                    )
                );
            })
        );
    }

    private class TranslationContext
    {
        private final Stack<String> keyComponents = new Stack<>();

        private <E> void forEach(List<E> list, Function<E, String> keyExtractor, Consumer<E> consumer)
        {
            for (E e : list)
                withKey(keyExtractor.apply(e), () -> consumer.accept(e));
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
                throw new LocalizationException("Unable to set translations from '" + localizationFunction.getClass().getName() + "' with key '" + key + "'", e);
            }
        }
    }
}
