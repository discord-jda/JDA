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
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.internal.interactions.component.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

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
     * Creates a new preconfigured {@link Builder} with the same settings used for this select menu.
     * <br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
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
        return builder;
    }

    /**
     * Creates a new {@link StringSelectMenu.Builder} for a select menu with the provided custom id.
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
     * Creates a new {@link StringSelectMenu.Builder} for a select menu with the provided custom id.
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
     *             <li>If the provided types are null, empty, or invalid.</li>
     *         </ul>
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    static Builder create(@Nonnull String customId, @Nonnull SelectTarget type, @Nonnull SelectTarget... types)
    {
        return create(customId, EnumSet.of(type, types));
    }

    /**
     * Supported entity types for a EntitySelectMenu.
     * <br>Note that some combinations are unsupported by Discord, due to the restrictive API design.
     */
    enum SelectTarget
    {
        USER,
        ROLE,
        CHANNEL
    }

    /**
     * A preconfigured builder for the creation of entity select menus.
     */
    class Builder extends SelectMenu.Builder<EntitySelectMenu, Builder>
    {
        protected Component.Type componentType;
        protected EnumSet<ChannelType> channelTypes = EnumSet.noneOf(ChannelType.class);

        protected Builder(@Nonnull String customId)
        {
            super(customId);
        }

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

        @Nonnull
        public Builder setEntityTypes(@Nonnull SelectTarget type, @Nonnull SelectTarget... types)
        {
            Checks.notNull(type, "Type");
            Checks.noneNull(types, "Types");
            return setEntityTypes(EnumSet.of(type, types));
        }

        @Nonnull
        public Builder setChannelTypes(@Nonnull Collection<ChannelType> types)
        {
            Checks.noneNull(types, "Types");
            for (ChannelType type : types)
                Checks.check(type.isGuild(), "Only guild channel types are allowed! Provided: %s", type);
            this.channelTypes = Helpers.copyEnumSet(ChannelType.class, types);
            return this;
        }

        @Nonnull
        public Builder setChannelTypes(@Nonnull ChannelType... types)
        {
            return setChannelTypes(Arrays.asList(types));
        }

        @Nonnull
        @Override
        public EntitySelectMenu build()
        {
            Checks.check(minValues <= maxValues, "Min values cannot be greater than max values!");
            EnumSet<ChannelType> channelTypes = componentType == Type.CHANNEL_SELECT ? this.channelTypes : EnumSet.noneOf(ChannelType.class);
            return new EntitySelectMenuImpl(customId, placeholder, minValues, maxValues, disabled, componentType, channelTypes);
        }
    }
}
