package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import javax.annotation.Nonnull;
import java.util.*;

public class DefaultLocalizationFunction implements LocalizationFunction
{
    private DefaultLocalizationFunction(Set<Bundle> bundles)
    {
        this.bundles = bundles;
    }

    @Nonnull
    @Override
    public Map<DiscordLocale, String> apply(@Nonnull String localizationKey)
    {
        final Map<DiscordLocale, String> map = new HashMap<>();
        for (Bundle bundle : bundles)
        {
            final ResourceBundle resourceBundle = bundle.resourceBundle;
            if (resourceBundle.containsKey(localizationKey))
                map.put(DiscordLocale.from(bundle.targetLocale), resourceBundle.getString(localizationKey));
        }

        return map;
    }

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

    private final Set<Bundle> bundles;

    @Nonnull
    public static Builder fromBundle(@Nonnull Locale locale, @Nonnull ResourceBundle resourceBundle)
    {
        return new Builder()
                .addBundle(resourceBundle, locale);
    }

    @Nonnull
    public static Builder fromBundles(@Nonnull String baseName, @Nonnull Locale... locales)
    {
        return new Builder().addBundles(baseName, locales);
    }

    @Nonnull
    public static Builder empty()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final Set<Bundle> bundles = new HashSet<>();

        @Nonnull
        public Builder addBundle(@Nonnull ResourceBundle resourceBundle, @Nonnull Locale locale)
        {
            bundles.add(new Bundle(locale, resourceBundle));
            return this;
        }

        @Nonnull
        public Builder addBundles(@Nonnull String baseName, @Nonnull Locale... locales)
        {
            for (Locale locale : locales)
            {
                final ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, locale);
                bundles.add(new Bundle(locale, resourceBundle));
            }
            return this;
        }

        @Nonnull
        public DefaultLocalizationFunction build()
        {
            return new DefaultLocalizationFunction(bundles);
        }
    }
}
