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
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A default implementation for {@link LocalizationFunction}
 * <p>
 * This implementation supports Java's {@link ResourceBundle} to be used as a localization source
 *
 * <p>
 * You can look at a complete localization example <a href="https://github.com/DV8FromTheWorld/JDA/blob/master/src/examples/java/LocalizationExample.java" target="_blank">here</a>
 */
public class ResourceBundleLocalizationFunction implements LocalizationFunction
{
    private final Set<Bundle> bundles;

    private ResourceBundleLocalizationFunction(Set<Bundle> bundles)
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
                map.put(bundle.targetLocale, resourceBundle.getString(localizationKey));
        }

        return map;
    }

    /**
     * Provides a {@link ResourceBundleLocalizationFunction} builder with the provided bundle having the specified name and locale.
     *
     * @param  resourceBundle
     *         The resource bundle to get the localized strings from
     *
     * @param  locale
     *         The locale of the resources
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the resource bundle is null</li>
     *             <li>If the locale is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *         </ul>
     *
     * @return The new builder
     */
    @Nonnull
    public static Builder fromBundle(@Nonnull ResourceBundle resourceBundle, @Nonnull DiscordLocale locale)
    {
        return new Builder()
                .addBundle(resourceBundle, locale);
    }

    /**
     * Provides a {@link ResourceBundleLocalizationFunction} builder with the provided bundles.
     * <br>This will insert the resource bundles with the specified name, with each specified locale.
     *
     * @param  baseName
     *         The base name of the resource bundle, for example, the base name of {@code "MyBundle_fr_FR.properties"} would be {@code "MyBundle"}
     *
     * @param  locales
     *         The locales to get from the resource bundle
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the base name is null</li>
     *             <li>If the locales or one of the locale is null</li>
     *             <li>If one of the locale is {@link DiscordLocale#UNKNOWN}</li>
     *         </ul>
     *
     * @return The new builder
     */
    @Nonnull
    public static Builder fromBundles(@Nonnull String baseName, @Nonnull DiscordLocale... locales)
    {
        return new Builder().addBundles(baseName, locales);
    }

    /**
     * Provides an empty {@link ResourceBundleLocalizationFunction} builder.
     *
     * @return The empty builder
     */
    @Nonnull
    public static Builder empty()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final Set<Bundle> bundles = new HashSet<>();

        /**
         * Adds a resource bundle to this builder
         *
         * @param  resourceBundle
         *         The {@link ResourceBundle} to get the localized strings from
         *
         * @param  locale
         *         The {@link DiscordLocale} of the resources
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the resource bundle is null</li>
         *             <li>If the locale is null</li>
         *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
         *         </ul>
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder addBundle(@Nonnull ResourceBundle resourceBundle, @Nonnull DiscordLocale locale)
        {
            Checks.notNull(resourceBundle, "Resource bundle");
            Checks.notNull(locale, "Locale");
            Checks.check(locale != DiscordLocale.UNKNOWN,"Cannot use UNKNOWN DiscordLocale");

            bundles.add(new Bundle(locale, resourceBundle));
            return this;
        }

        /**
         * Adds a resource bundle to this builder
         * <br>This will insert the resource bundles with the specified name, with each specified locale.
         *
         * @param  baseName
         *         The base name of the resource bundle, for example, the base name of {@code "MyBundle_fr_FR.properties"} would be {@code "MyBundle"}
         *
         * @param  locales
         *         The locales to get from the resource bundle
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the base name is null</li>
         *             <li>If the locales or one of the locale is null</li>
         *             <li>If one of the locale is {@link DiscordLocale#UNKNOWN}</li>
         *         </ul>
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder addBundles(@Nonnull String baseName, @Nonnull DiscordLocale... locales)
        {
            Checks.notNull(baseName, "Base name");
            Checks.noneNull(locales, "Locale");

            for (DiscordLocale locale : locales)
            {
                Checks.check(locale != DiscordLocale.UNKNOWN,"Cannot use UNKNOWN DiscordLocale");

                final ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, Locale.forLanguageTag(locale.getLocale()));
                bundles.add(new Bundle(locale, resourceBundle));
            }
            return this;
        }

        /**
         * Builds the resource bundle localization function.
         *
         * @return The new {@link ResourceBundleLocalizationFunction}
         */
        @Nonnull
        public ResourceBundleLocalizationFunction build()
        {
            return new ResourceBundleLocalizationFunction(bundles);
        }
    }

    private static final class Bundle
    {
        private final DiscordLocale targetLocale;
        private final ResourceBundle resourceBundle;

        public Bundle(DiscordLocale targetLocale, ResourceBundle resourceBundle)
        {
            this.targetLocale = targetLocale;
            this.resourceBundle = resourceBundle;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof Bundle)) return false;

            Bundle bundle = (Bundle) o;

            if (!targetLocale.equals(bundle.targetLocale)) return false;
            return resourceBundle.equals(bundle.resourceBundle);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(targetLocale, resourceBundle);
        }
    }
}
