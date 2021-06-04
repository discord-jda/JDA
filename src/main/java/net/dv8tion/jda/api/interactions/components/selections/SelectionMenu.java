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

package net.dv8tion.jda.api.interactions.components.selections;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface SelectionMenu extends Component
{
    // TODO: Documentation

    @Nullable
    String getPlaceholder();

    int getMinValues();

    int getMaxValues();

    @Nonnull
    List<SelectOption> getOptions();

    class Builder
    {
        private String customId;
        private String placeholder;
        private int minValues = 1, maxValues = 1;
        private final List<SelectOption> options = new ArrayList<>();

        public Builder(@Nonnull String customId)
        {
            setId(customId);
        }

        @Nonnull
        public Builder setId(@Nonnull String customId)
        {
            Checks.notEmpty(customId, "Component ID");
            Checks.notLonger(customId, 100, "Component ID");
            this.customId = customId;
            return this;
        }

        @Nonnull
        public Builder setPlaceholder(@Nullable String placeholder)
        {
            if (placeholder != null)
            {
                Checks.notEmpty(placeholder, "Placeholder");
                Checks.notLonger(placeholder, 100, "Placeholder");
            }
            this.placeholder = placeholder;
            return this;
        }

        @Nonnull
        public Builder setMinValues(int minValues)
        {
            Checks.notNegative(minValues, "Min Values");
            this.minValues = minValues;
            return this;
        }

        @Nonnull
        public Builder setMaxValues(int maxValues)
        {
            Checks.notNegative(maxValues, "Max Values");
            this.maxValues = maxValues;
            return this;
        }

        @Nonnull
        public Builder setRequiredRange(int min, int max)
        {
            Checks.check(min <= max, "Min Values should be less than or equal to Max Values! Provided: [%d, %d]", min, max);
            this.minValues = min;
            this.maxValues = max;
            return this;
        }

        @Nonnull
        public Builder addOptions(@Nonnull SelectOption... options)
        {
            Checks.noneNull(options, "Options");
            Checks.check(this.options.size() + options.length <= 25, "Cannot have more than 25 options for a selection menu!");
            Collections.addAll(this.options, options);
            return this;
        }

        @Nonnull
        public Builder addOptions(@Nonnull Collection<? extends SelectOption> options)
        {
            Checks.noneNull(options, "Options");
            Checks.check(this.options.size() + options.size() <= 25, "Cannot have more than 25 options for a selection menu!");
            this.options.addAll(options);
            return this;
        }

        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value)
        {
            return addOptions(new SelectOption(label, value));
        }

        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nonnull Emoji emoji)
        {
            return addOptions(new SelectOption(label, value).setEmoji(emoji));
        }

        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nonnull String description)
        {
            return addOptions(new SelectOption(label, value).setDescription(description));
        }

        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nullable String description, @Nullable Emoji emoji)
        {
            return addOptions(new SelectOption(label, value)
                .setDescription(description)
                .setEmoji(emoji));
        }

        @Nonnull
        public SelectionMenu build()
        {
            Checks.check(maxValues <= options.size(), "The max values should be less than or equal to the amount of available options");
            return new SelectionMenuImpl(customId, placeholder, minValues, maxValues, options);
        }
    }

}
