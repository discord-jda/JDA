package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.interactions.LocalizationMap;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class LocalizationMapper
{
    private static final class Bundle
    {
        private final Locale targetLocale;
        private final ResourceBundle resourceBundle;

        public Bundle(Locale targetLocale, ResourceBundle resourceBundle)
        {
            this.targetLocale = targetLocale;
            this.resourceBundle = resourceBundle;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Bundle bundle = (Bundle) o;

            if (!targetLocale.equals(bundle.targetLocale)) return false;
            return resourceBundle.equals(bundle.resourceBundle);
        }

        @Override
        public int hashCode()
        {
            int result = targetLocale.hashCode();
            result = 31 * result + resourceBundle.hashCode();
            return result;
        }
    }

    private final Set<Bundle> bundles = new HashSet<>();

    private LocalizationMapper() {}

    @Nonnull
    public static LocalizationMapper fromBundle(@Nonnull Locale locale, @Nonnull ResourceBundle resourceBundle)
    {
        return new LocalizationMapper()
                .addBundle(resourceBundle, locale);
    }

    @Nonnull
    public static LocalizationMapper empty()
    {
        return new LocalizationMapper();
    }

    @Nonnull
    public LocalizationMapper addBundle(@Nonnull ResourceBundle resourceBundle, @Nonnull Locale locale)
    {
        bundles.add(new Bundle(locale, resourceBundle));
        return this;
    }

    @Nonnull
    public LocalizationMapper addBundles(@Nonnull String baseName, @Nonnull Locale... locales)
    {
        for (Locale locale : locales)
        {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, locale);
            bundles.add(new Bundle(locale, resourceBundle));
        }
        return this;
    }

    private static class TranslationContext
    {
        private final Bundle bundle;
        private final Stack<String> keyComponents = new Stack<>();

        private TranslationContext(Bundle bundle)
        {
            this.bundle = bundle;
        }

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
            return joiner.toString();
        }

        private void trySetTranslation(LocalizationMap localizationMap, String finalComponent)
        {
            final String key = getKey(finalComponent);
            final ResourceBundle resourceBundle = bundle.resourceBundle;
            if (resourceBundle.containsKey(key))
                localizationMap.setTranslation(resourceBundle.getString(key), bundle.targetLocale);
        }

        private void trySetTranslation(DataObject localizationMap, String finalComponent)
        {
            final String key = getKey(finalComponent);
            final ResourceBundle resourceBundle = bundle.resourceBundle;
            if (resourceBundle.containsKey(key))
                localizationMap.put(bundle.targetLocale.toLanguageTag(), resourceBundle.getString(key));
        }
    }

    public void localizeCommand(CommandData commandData, DataArray optionArray)
    {
        for (Bundle bundle : bundles)
        {
            final TranslationContext ctx = new TranslationContext(bundle);

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
