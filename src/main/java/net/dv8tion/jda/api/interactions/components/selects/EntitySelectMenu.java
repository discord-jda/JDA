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

package net.dv8tion.jda.api.interactions.components.selects;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.component.concrete.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;

/**
 * Specialized {@link SelectMenu} for selecting Discord entities.
 *
 * <p>Unlike {@link StringSelectMenu}, these entity select menus do not support custom choices.
 * A user will get suggested inputs based on what they write into the select menu.
 *
 * <p>This is an interactive component and usually located within an {@link ActionRow ActionRow}.
 * One select menu fills up an entire action row by itself. You cannot have an action row with other components if a select menu is present in the same row.
 *
 * <p>The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected, and they will disappear when the client restarts or the message is reloaded.
 *
 * <p><b>Examples</b><br>
 * <pre>{@code
 * public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 *   if (!event.getName().equals("class")) return;
 *
 *   EntitySelectMenu menu = EntitySelectMenu.create("menu:class", SelectTarget.ROLE)
 *     .setPlaceholder("Choose your class") // shows the placeholder indicating what this menu is for
 *     .setRequiredRange(1, 1) // must select exactly one
 *     .build();
 *
 *   event.reply("Please pick your class below")
 *     .setEphemeral(true)
 *     .addActionRow(menu)
 *     .queue();
 * }
 * }</pre>
 *
 * @see SelectTarget
 * @see EntitySelectInteraction
 * @see StringSelectMenu
 */
public interface EntitySelectMenu extends SelectMenu
{
    @Nonnull
    @Override
    default EntitySelectMenu asDisabled()
    {
        return (EntitySelectMenu) SelectMenu.super.asDisabled();
    }

    @Nonnull
    @Override
    default EntitySelectMenu asEnabled()
    {
        return (EntitySelectMenu) SelectMenu.super.asEnabled();
    }

    @Nonnull
    @Override
    default EntitySelectMenu withDisabled(boolean disabled)
    {
        return createCopy().setDisabled(disabled).build();
    }

    /**
     * The {@link SelectTarget SelectTargets} supported by this menu.
     * <br>If the targets include {@link SelectTarget#CHANNEL}, then they are also filtered by {@link #getChannelTypes()}.
     *
     * <p>Modifying the returned {@link EnumSet} will not affect this menu.
     *
     * @return {@link EnumSet} of {@link SelectTarget}
     */
    @Nonnull
    EnumSet<SelectTarget> getEntityTypes();

    /**
     * The allowed {@link ChannelType ChannelTypes} for this menu.
     * <br>This is only relevant if the {@link SelectTarget SelectTargets} include {@link SelectTarget#CHANNEL}.
     * The returned set is empty if all types are supported, or {@link #getEntityTypes()} does not include {@link SelectTarget#CHANNEL}.
     *
     * <p>Modifying the returned {@link EnumSet} will not affect this menu.
     *
     * @return {@link EnumSet} of {@link ChannelType}
     */
    @Nonnull
    EnumSet<ChannelType> getChannelTypes();

    /**
     * Default selected values.
     * <br>These are shown until the user customizes the selected values,
     * which then fires a {@link EntitySelectInteractionEvent}.
     *
     * @return Immutable list of {@link DefaultValue default values}
     */
    @Nonnull
    @Unmodifiable
    List<DefaultValue> getDefaultValues();

    /**
     * Creates a new preconfigured {@link Builder} with the same settings used for this select menu.
     * <br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    @Override
    default Builder createCopy()
    {
        //noinspection ConstantConditions
        Builder builder = create(getId(), getEntityTypes());
        EnumSet<ChannelType> channelTypes = getChannelTypes();
        if (!channelTypes.isEmpty())
            builder.setChannelTypes(channelTypes);
        builder.setRequiredRange(getMinValues(), getMaxValues());
        builder.setPlaceholder(getPlaceholder());
        builder.setDisabled(isDisabled());
        builder.setDefaultValues(getDefaultValues());
        return builder;
    }

    /**
     * Creates a new {@link Builder} for a select menu with the provided custom id.
     *
     * @param  customId
     *         The id used to identify this menu with {@link ActionComponent#getId()} for component interactions
     * @param  types
     *         The supported {@link SelectTarget SelectTargets}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided id is null, empty, or longer than {@value ID_MAX_LENGTH} characters.</li>
     *             <li>If the provided types are null, empty, or invalid.</li>
     *         </ul>
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    static Builder create(@Nonnull String customId, @Nonnull Collection<SelectTarget> types)
    {
        return new Builder(customId).setEntityTypes(types);
    }

    /**
     * Creates a new {@link Builder} for a select menu with the provided custom id.
     *
     * @param  customId
     *         The id used to identify this menu with {@link ActionComponent#getId()} for component interactions
     * @param  type
     *         The first supported {@link SelectTarget}
     * @param  types
     *         Other supported {@link SelectTarget SelectTargets}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided id is null, empty, or longer than {@value ID_MAX_LENGTH} characters.</li>
     *             <li>If the provided types are null or invalid.</li>
     *         </ul>
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    static Builder create(@Nonnull String customId, @Nonnull SelectTarget type, @Nonnull SelectTarget... types)
    {
        Checks.notNull(type, "Type");
        Checks.noneNull(types, "Types");
        return create(customId, EnumSet.of(type, types));
    }

    /**
     * Supported entity types for a EntitySelectMenu.
     * <br>Note that some combinations are unsupported by Discord, due to the restrictive API design.
     *
     * <p>The only combination that is currently supported is {@link #USER} + {@link #ROLE} (often referred to as "mentionables").
     * Combinations such as {@link #ROLE} + {@link #CHANNEL} are currently not supported.
     */
    enum SelectTarget
    {
        USER,
        ROLE,
        CHANNEL
    }

    /**
     * Represents the default values used in {@link #getDefaultValues()}.
     * <br>The value is {@link #getType() typed} correspondingly to the menu {@link EntitySelectMenu#getEntityTypes() entity types}.
     *
     * <p>The value is represented by the {@link #getId() ID}, corresponding to the entity of that ID.
     */
    class DefaultValue implements ISnowflake, SerializableData
    {
        private final long id;
        private final SelectTarget type;

        protected DefaultValue(long id, @Nonnull SelectTarget type)
        {
            this.id = id;
            this.type = type;
        }

        /**
         * Parses the provided {@link DataObject} into the default value.
         *
         * @param  object
         *         The serialized default value, with a valid type and id
         *
         * @throws IllegalArgumentException
         *         If the provided object is invalid or missing required keys
         *
         * @return Parsed default value
         */
        @Nonnull
        public static DefaultValue fromData(@Nonnull DataObject object)
        {
            Checks.notNull(object, "DataObject");
            long id = object.getUnsignedLong("id");
            switch (object.getString("type"))
            {
            case "role":
                return role(id);
            case "user":
                return user(id);
            case "channel":
                return channel(id);
            }
            throw new IllegalArgumentException("Unknown value type '" + object.getString("type", null) + "'");
        }

        /**
         * Creates a default value of type {@link SelectTarget#USER} for the provided user.
         *
         * @param  user
         *         The corresponding user
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue from(@Nonnull UserSnowflake user)
        {
            Checks.notNull(user, "User");
            return user(user.getIdLong());
        }

        /**
         * Creates a default value of type {@link SelectTarget#ROLE} for the provided role.
         *
         * @param  role
         *         The corresponding role
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue from(@Nonnull Role role)
        {
            Checks.notNull(role, "Role");
            return role(role.getIdLong());
        }

        /**
         * Creates a default value of type {@link SelectTarget#CHANNEL} for the provided channel.
         *
         * @param  channel
         *         The corresponding channel
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue from(@Nonnull GuildChannel channel)
        {
            Checks.notNull(channel, "Channel");
            return channel(channel.getIdLong());
        }

        /**
         * Creates a default value of type {@link SelectTarget#ROLE} with the provided id.
         *
         * @param  id
         *         The role id
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue role(long id)
        {
            return new DefaultValue(id, SelectTarget.ROLE);
        }

        /**
         * Creates a default value of type {@link SelectTarget#ROLE} with the provided id.
         *
         * @param  id
         *         The role id
         *
         * @throws IllegalArgumentException
         *         If the provided id is not a valid snowflake
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue role(@Nonnull String id)
        {
            return new DefaultValue(MiscUtil.parseSnowflake(id), SelectTarget.ROLE);
        }

        /**
         * Creates a default value of type {@link SelectTarget#USER} with the provided id.
         *
         * @param  id
         *         The user id
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue user(long id)
        {
            return new DefaultValue(id, SelectTarget.USER);
        }

        /**
         * Creates a default value of type {@link SelectTarget#USER} with the provided id.
         *
         * @param  id
         *         The user id
         *
         * @throws IllegalArgumentException
         *         If the provided id is not a valid snowflake
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue user(@Nonnull String id)
        {
            return new DefaultValue(MiscUtil.parseSnowflake(id), SelectTarget.USER);
        }

        /**
         * Creates a default value of type {@link SelectTarget#CHANNEL} with the provided id.
         *
         * @param  id
         *         The channel id
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue channel(long id)
        {
            return new DefaultValue(id, SelectTarget.CHANNEL);
        }

        /**
         * Creates a default value of type {@link SelectTarget#CHANNEL} with the provided id.
         *
         * @param  id
         *         The channel id
         *
         * @throws IllegalArgumentException
         *         If the provided id is not a valid snowflake
         *
         * @return The default value
         */
        @Nonnull
        public static DefaultValue channel(@Nonnull String id)
        {
            return new DefaultValue(MiscUtil.parseSnowflake(id), SelectTarget.CHANNEL);
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Nonnull
        public SelectTarget getType()
        {
            return type;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            return DataObject.empty()
                    .put("type", type.name().toLowerCase(Locale.ROOT))
                    .put("id", getId());
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(type, id);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof DefaultValue))
                return false;
            DefaultValue other = (DefaultValue) obj;
            return id == other.id && type == other.type;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setType(type)
                    .toString();
        }
    }

    /**
     * A preconfigured builder for the creation of entity select menus.
     */
    class Builder extends SelectMenu.Builder<EntitySelectMenu, Builder>
    {
        protected Component.Type componentType;
        protected EnumSet<ChannelType> channelTypes = EnumSet.noneOf(ChannelType.class);
        protected List<DefaultValue> defaultValues = new ArrayList<>();

        protected Builder(@Nonnull String customId)
        {
            super(customId);
        }

        /**
         * The {@link SelectTarget SelectTargets} that should be supported by this menu.
         *
         * @param  types
         *         The supported {@link SelectTarget SelectTargets} (1-2)
         *
         * @throws IllegalArgumentException
         *         If the provided targets are null, empty, or invalid.
         *
         * @return The current Builder instance
         */
        @Nonnull
        public Builder setEntityTypes(@Nonnull Collection<SelectTarget> types)
        {
            Checks.notEmpty(types, "Types");
            Checks.noneNull(types, "Types");

            EnumSet<SelectTarget> set = Helpers.copyEnumSet(SelectTarget.class, types);
            if (set.size() == 1)
            {
                if (set.contains(SelectTarget.CHANNEL))
                    this.componentType = Component.Type.CHANNEL_SELECT;
                else if (set.contains(SelectTarget.ROLE))
                    this.componentType = Component.Type.ROLE_SELECT;
                else if (set.contains(SelectTarget.USER))
                    this.componentType = Component.Type.USER_SELECT;
            }
            else if (set.size() == 2)
            {
                if (set.contains(SelectTarget.USER) && set.contains(SelectTarget.ROLE))
                    this.componentType = Type.MENTIONABLE_SELECT;
                else
                    throw new IllegalArgumentException("The provided combination of select targets is not supported. Provided: " + set);
            }
            else
            {
                throw new IllegalArgumentException("The provided combination of select targets is not supported. Provided: " + set);
            }

            return this;
        }

        /**
         * The {@link SelectTarget SelectTargets} that should be supported by this menu.
         *
         * @param  type
         *         The first supported {@link SelectTarget}
         * @param  types
         *         Additional supported {@link SelectTarget SelectTargets}
         *
         * @throws IllegalArgumentException
         *         If the provided targets are null or invalid.
         *
         * @return The current Builder instance
         */
        @Nonnull
        public Builder setEntityTypes(@Nonnull SelectTarget type, @Nonnull SelectTarget... types)
        {
            Checks.notNull(type, "Type");
            Checks.noneNull(types, "Types");
            return setEntityTypes(EnumSet.of(type, types));
        }

        /**
         * The {@link ChannelType ChannelTypes} that should be supported by this menu.
         * <br>This is only relevant for menus that allow {@link SelectTarget#CHANNEL CHANNEL} targets.
         *
         * @param  types
         *         The supported {@link ChannelType ChannelTypes} (empty to allow all types)
         *
         * @throws IllegalArgumentException
         *         If the provided types are null or not guild types
         *
         * @return The current Builder instance
         */
        @Nonnull
        public Builder setChannelTypes(@Nonnull Collection<ChannelType> types)
        {
            Checks.noneNull(types, "Types");
            for (ChannelType type : types)
                Checks.check(type.isGuild(), "Only guild channel types are allowed! Provided: %s", type);
            this.channelTypes = Helpers.copyEnumSet(ChannelType.class, types);
            return this;
        }

        /**
         * The {@link ChannelType ChannelTypes} that should be supported by this menu.
         * <br>This is only relevant for menus that allow {@link SelectTarget#CHANNEL CHANNEL} targets.
         *
         * @param  types
         *         The supported {@link ChannelType ChannelTypes} (empty to allow all types)
         *
         * @throws IllegalArgumentException
         *         If the provided types are null or not guild types
         *
         * @return The current Builder instance
         */
        @Nonnull
        public Builder setChannelTypes(@Nonnull ChannelType... types)
        {
            return setChannelTypes(Arrays.asList(types));
        }

        /**
         * The {@link #getDefaultValues() default values} that will be shown to the user.
         *
         * @param  values
         *         The default values (up to {@value #OPTIONS_MAX_AMOUNT})
         *
         * @throws IllegalArgumentException
         *         If null is provided, more than {@value #OPTIONS_MAX_AMOUNT} values are provided,
         *         or any of the value types is incompatible with the configured {@link #setEntityTypes(Collection) entity types}.
         *
         * @return The current Builder instance
         */
        @Nonnull
        public Builder setDefaultValues(@Nonnull DefaultValue... values)
        {
            Checks.noneNull(values, "Default Values");
            return setDefaultValues(Arrays.asList(values));
        }

        /**
         * The {@link #getDefaultValues() default values} that will be shown to the user.
         *
         * @param  values
         *         The default values (up to {@value #OPTIONS_MAX_AMOUNT})
         *
         * @throws IllegalArgumentException
         *         If null is provided, more than {@value #OPTIONS_MAX_AMOUNT} values are provided,
         *         or any of the value types is incompatible with the configured {@link #setEntityTypes(Collection) entity types}.
         *
         * @return The current Builder instance
         */
        @Nonnull
        public Builder setDefaultValues(@Nonnull Collection<? extends DefaultValue> values)
        {
            Checks.noneNull(values, "Default Values");
            Checks.check(values.size() <= SelectMenu.OPTIONS_MAX_AMOUNT, "Cannot add more than %d default values to a select menu!", SelectMenu.OPTIONS_MAX_AMOUNT);

            for (DefaultValue value : values)
            {
                SelectTarget type = value.getType();
                String error = "The select menu supports types %s, but provided default value has type SelectTarget.%s!";

                switch (componentType)
                {
                case ROLE_SELECT:
                    Checks.check(type == SelectTarget.ROLE, error, "SelectTarget.ROLE", type);
                    break;
                case USER_SELECT:
                    Checks.check(type == SelectTarget.USER, error, "SelectTarget.USER", type);
                    break;
                case CHANNEL_SELECT:
                    Checks.check(type == SelectTarget.CHANNEL, error, "SelectTarget.CHANNEL", type);
                    break;
                case MENTIONABLE_SELECT:
                    Checks.check(type == SelectTarget.ROLE || type == SelectTarget.USER, error, "SelectTarget.ROLE and SelectTarget.USER", type);
                    break;
                }
            }

            this.defaultValues.clear();
            this.defaultValues.addAll(values);
            return this;
        }

        /**
         * Creates a new {@link EntitySelectMenu} instance if all requirements are satisfied.
         *
         * @throws IllegalArgumentException
         *         Throws if {@link #getMinValues()} is greater than {@link #getMaxValues()}
         *
         * @return The new {@link EntitySelectMenu} instance
         */
        @Nonnull
        @Override
        public EntitySelectMenu build()
        {
            Checks.check(minValues <= maxValues, "Min values cannot be greater than max values!");
            EnumSet<ChannelType> channelTypes = componentType == Type.CHANNEL_SELECT ? this.channelTypes : EnumSet.noneOf(ChannelType.class);
            List<DefaultValue> defaultValues = new ArrayList<>(this.defaultValues);
            return new EntitySelectMenuImpl(customId, uniqueId, placeholder, minValues, maxValues, disabled, componentType, channelTypes, defaultValues);
        }
    }
}
