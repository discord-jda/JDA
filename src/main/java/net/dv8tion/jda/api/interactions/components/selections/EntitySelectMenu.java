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

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.select.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.interactions.component.select.StringSelectMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a select menu in a message.
 * <br>This is an interactive component and usually located within an {@link net.dv8tion.jda.api.interactions.components.ActionRow ActionRow}.
 * One select menu fills up an entire action row by itself. You cannot have an action row with other components if a select menu is present in the same row.
 *
 * <p>The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected, and they will disappear when the client restarts or the message is reloaded.
 *
 * <p>There are two types of select menus within JDA. Entity select menus, which are used for channels, users and roles, and string select menus which are used for custom options.
 *
 * <p><b>Examples</b><br>
 * <pre>{@code
 * public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 *   if (!event.getName().equals("friend")) return;
 *
 *   SelectMenu menu = SelectMenu.create("menu:friend")
 *     .setPlaceholder("Select a friend") // shows the placeholder indicating what this menu is for
 *     .setRequireRange(1, 1) // only one can be selected
 *     .build();
 *
 *   event.reply("Please select a friend below")
 *     .setEphemeral(true)
 *     .addActionRow(menu)
 *     .queue();
 * }
 * }</pre>
 *
 * @see StringSelectMenuInteraction
 */
public interface EntitySelectMenu extends ActionComponent
{
    /**
     * The maximum length a select menu id can have
     */
    int ID_MAX_LENGTH = 100;

    /**
     * The maximum length a select menu placeholder can have
     */
    int PLACEHOLDER_MAX_LENGTH = 100;

    /**
     * Placeholder which is displayed when no selections have been made yet.
     *
     * @return The placeholder or null
     */
    @Nullable
    String getPlaceholder();

    /**
     * The channel filters controlling what channels are shown.
     *
     * @return the channel filters
     */
    EnumSet<ChannelType> getChannelTypes();

    /**
     * The minimum amount of values a user has to select.
     *
     * @return The min values
     */
    int getMinValues();

    /**
     * The maximum amount of values a user can select at once.
     *
     * @return The max values
     */
    int getMaxValues();

    @Nonnull
    @Override
    @CheckReturnValue
    default EntitySelectMenu asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default EntitySelectMenu asEnabled()
    {
        return withDisabled(false);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default EntitySelectMenu withDisabled(boolean disabled)
    {
        return createCopy().setDisabled(disabled).build();
    }

    /**
     * Creates a new preconfigured {@link Builder} with the same settings used for this select menu.
     * <br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    default Builder createCopy()
    {
        EnumSet<SelectType> selectTypes;

        Type componentType = getType();
        if (componentType == Type.MENTIONABLE_SELECT_MENU) selectTypes = EnumSet.of(SelectType.ROLE, SelectType.USER);
        else if (componentType == Type.CHANNEL_SELECT_MENU) selectTypes = EnumSet.of(SelectType.CHANNEL);
        else if (componentType == Type.ROLE_SELECT_MENU) selectTypes = EnumSet.of(SelectType.ROLE);
        else if (componentType == Type.USER_SELECT_MENU) selectTypes = EnumSet.of(SelectType.USER);
        else throw new IllegalStateException("Unknown component type: " + componentType);

        Builder builder = create(getId(), selectTypes);
        builder.setRequiredRange(getMinValues(), getMaxValues());
        builder.setPlaceholder(getPlaceholder());
        builder.setDisabled(isDisabled());
        return builder;
    }

    /**
     * Creates a new {@link Builder} for a select menu with the provided custom id.
     *
     * @param  customId
     *         The id used to identify this menu with {@link ActionComponent#getId()} for component interactions
     *
     * @param  type
     *         The type combination of select menu to create
     *
     * @throws IllegalArgumentException
     *         If the provided id is null, empty, or longer than {@value ID_MAX_LENGTH} characters
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    static Builder create(@Nonnull String customId, @Nonnull EnumSet<SelectType> type)
    {
        return new Builder(customId, type);
    }

    /**
     * Inverse function for {@link #toData()} which parses the serialized select menu data.
     * <br>Returns a {@link Builder} which allows for further configuration.
     *
     * @param  data
     *         The serialized select menu data
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the data representation is invalid
     * @throws IllegalArgumentException
     *         If some part of the data has an invalid length or null is provided
     *
     * @return The parsed SelectMenu Builder instance
     */
    @Nonnull
    @CheckReturnValue
    static Builder fromData(@Nonnull DataObject data)
    {
        return new EntitySelectMenuImpl(data).createCopy();
    }

    /**
     * Represents an EntitySelectMenu type
     */
    enum SelectType {
        ROLE(5),
        USER(6),
        CHANNEL(8),
        ;

        private final int key;

        SelectType(int key) { this.key = key; }

        public int getKey() { return key; }
    }

    /**
     * A preconfigured builder for the creation of select menus.
     */
    class Builder
    {
        private String customId;
        private String placeholder;
        private EnumSet<SelectType> type;
        private EnumSet<ChannelType> channelFilters;
        private int minValues = 1, maxValues = 1;
        private boolean disabled = false;

        protected Builder(@Nonnull String customId, @Nonnull EnumSet<SelectType> types)
        {
            setId(customId);
            setType(types);
        }

        /**
         * Change the custom id used to identify the select menu.
         *
         * @param  customId
         *         The new custom id to use
         *
         * @throws IllegalArgumentException
         *         If the provided id is null, empty, or longer than {@value ID_MAX_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setId(@Nonnull String customId)
        {
            Checks.notEmpty(customId, "Component ID");
            Checks.notLonger(customId, ID_MAX_LENGTH, "Component ID");
            this.customId = customId;
            return this;
        }

        /**
         * Changes the type(s) used in the select menu.
         *
         * @param  type
         *         The new type(s) to use
         *
         * @throws IllegalArgumentException
         *         If the provided type(s) is empty, or if the combination provided is invalid.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setType(@Nonnull EnumSet<SelectType> type)
        {
            Checks.notEmpty(type, "Select Type");
            boolean isSupportedCombination = true;

            // Check to make sure that if the size is over 1, that the only combination can be ROLE, USER, CHANNEL
            if (type.size() > 1)
                isSupportedCombination = type.contains(SelectType.ROLE) && type.contains(SelectType.CHANNEL);
            else
                isSupportedCombination = false;

            Checks.check(isSupportedCombination, "That select menu combination is not supported!");

            this.type = type;
            return this;
        }

        @Nonnull
        public Builder setChannelFilters(@Nonnull EnumSet<ChannelType> channelFilters)
        {
            Checks.notEmpty(channelFilters, "Channel Filters");
            if (!getType().contains(SelectType.CHANNEL))
                throw new IllegalArgumentException("Cannot set channel filters on a select menu that does not contain the CHANNEL type!");

            Checks.check(getType().size() == 1 && !getType().contains(SelectType.CHANNEL), "Cannot set channel filters on a select menu that does not contain the CHANNEL type!");
            this.channelFilters = channelFilters;
            return this;
        }

        /**
         * Configure the placeholder which is displayed when no selections have been made yet.
         *
         * @param  placeholder
         *         The placeholder or null
         *
         * @throws IllegalArgumentException
         *         If the provided placeholder is empty or longer than {@value PLACEHOLDER_MAX_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setPlaceholder(@Nullable String placeholder)
        {
            if (placeholder != null)
            {
                Checks.notEmpty(placeholder, "Placeholder");
                Checks.notLonger(placeholder, PLACEHOLDER_MAX_LENGTH, "Placeholder");
            }
            this.placeholder = placeholder;
            return this;
        }

        /**
         * The minimum amount of values a user has to select.
         * <br>Default: {@code 1}
         *
         * @param  minValues
         *         The min values
         *
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMinValues(int minValues)
        {
            Checks.notNegative(minValues, "Min Values");
            this.minValues = minValues;
            return this;
        }

        /**
         * The maximum amount of values a user can select.
         * <br>Default: {@code 1}
         *
         * @param  maxValues
         *         The max values
         *
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMaxValues(int maxValues)
        {
            Checks.positive(maxValues, "Max Values");
            this.maxValues = maxValues;
            return this;
        }

        /**
         * The minimum and maximum amount of values a user can select.
         * <br>Default: {@code 1} for both
         *
         * @param  min
         *         The min values
         * @param  max
         *         The max values
         *
         * @throws IllegalArgumentException
         *         If the provided amount is not a valid range ({@code 0 <= min <= max})
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setRequiredRange(int min, int max)
        {
            Checks.check(min <= max, "Min Values should be less than or equal to Max Values! Provided: [%d, %d]", min, max);
            return setMinValues(min).setMaxValues(max);
        }

        /**
         * Configure whether this select menu should be disabled.
         * <br>Default: {@code false}
         *
         * @param  disabled
         *         Whether this menu is disabled
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setDisabled(boolean disabled)
        {
            this.disabled = disabled;
            return this;
        }

        /**
         * The custom id used to identify the select menu.
         *
         * @return The custom id
         */
        @Nonnull
        public String getId()
        {
            return customId;
        }

        /**
         * The type(s) used in the select menu.
         *
         * @return The type(s)
         */
        @Nonnull
        public EnumSet<SelectType> getType() { return type; }

        /**
         * The allowed channels used in the select menu.
         *
         * @return The allowed channels
         */
        @Nonnull
        public EnumSet<ChannelType> getChannelFilters() { return channelFilters; }

        /**
         * Placeholder which is displayed when no selections have been made yet.
         *
         * @return The placeholder or null
         */
        @Nullable
        public String getPlaceholder()
        {
            return placeholder;
        }

        /**
         * The minimum amount of values a user has to select.
         *
         * @return The min values
         */
        public int getMinValues()
        {
            return minValues;
        }

        /**
         * The maximum amount of values a user can select at once.
         *
         * @return The max values
         */
        public int getMaxValues()
        {
            return maxValues;
        }

        /**
         * Whether the menu is disabled
         *
         * @return True if this menu is disabled
         */
        public boolean isDisabled()
        {
            return disabled;
        }

        /**
         * Creates a new {@link EntitySelectMenu} instance if all requirements are satisfied.
         *
         * @return The new {@link EntitySelectMenu} instance
         */
        @Nonnull
        public EntitySelectMenu build()
        {
            Checks.check(minValues <= maxValues, "Min values cannot be greater than max values!");

            int typeCode;

            if (type.size() == 1)
                typeCode = type.iterator().next().getKey();
            else if (type.size() == 3 && type.contains(SelectType.ROLE) && type.contains(SelectType.CHANNEL))
                typeCode = 7;
            else
                throw new IllegalArgumentException("Invalid select menu type combination!");

            return new EntitySelectMenuImpl(customId, placeholder, minValues, maxValues, disabled, Component.Type.fromKey(typeCode), channelFilters);
        }
    }
}
