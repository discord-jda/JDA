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
package net.dv8tion.jda.api.interactions.components.selections

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.components.*
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.interactions.component.EntitySelectMenuImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.internal.utils.Helpers
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Specialized [SelectMenu] for selecting Discord entities.
 *
 *
 * Unlike [StringSelectMenu], these entity select menus do not support custom choices.
 * A user will get suggested inputs based on what they write into the select menu.
 *
 *
 * This is an interactive component and usually located within an [ActionRow][net.dv8tion.jda.api.interactions.components.ActionRow].
 * One select menu fills up an entire action row by itself. You cannot have an action row with other components if a select menu is present in the same row.
 *
 *
 * The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected, and they will disappear when the client restarts or the message is reloaded.
 *
 *
 * **Examples**<br></br>
 * <pre>`public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 * if (!event.getName().equals("class")) return;
 *
 * EntitySelectMenu menu = EntitySelectMenu.create("menu:class", SelectTarget.ROLE)
 * .setPlaceholder("Choose your class") // shows the placeholder indicating what this menu is for
 * .setRequiredRange(1, 1) // must select exactly one
 * .build();
 *
 * event.reply("Please pick your class below")
 * .setEphemeral(true)
 * .addActionRow(menu)
 * .queue();
 * }
`</pre> *
 *
 * @see SelectTarget
 *
 * @see EntitySelectInteraction
 *
 * @see StringSelectMenu
 */
interface EntitySelectMenu : SelectMenu {
    @Nonnull
    override fun asDisabled(): EntitySelectMenu {
        return super.asDisabled() as EntitySelectMenu
    }

    @Nonnull
    override fun asEnabled(): EntitySelectMenu {
        return super.asEnabled() as EntitySelectMenu
    }

    @Nonnull
    override fun withDisabled(disabled: Boolean): EntitySelectMenu {
        return createCopy().setDisabled(disabled)!!.build()
    }

    @get:Nonnull
    val entityTypes: EnumSet<SelectTarget?>?

    @JvmField
    @get:Nonnull
    val channelTypes: EnumSet<ChannelType?>

    @JvmField
    @get:Nonnull
    val defaultValues: List<DefaultValue?>

    /**
     * Creates a new preconfigured [Builder] with the same settings used for this select menu.
     * <br></br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The [Builder] used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    fun createCopy(): Builder {
        val builder = create(id, entityTypes)
        val channelTypes = channelTypes
        if (!channelTypes.isEmpty()) builder.setChannelTypes(channelTypes)
        builder.setRequiredRange(getMinValues(), getMaxValues())
        builder.setPlaceholder(getPlaceholder())
        builder.setDisabled(isDisabled)
        builder.setDefaultValues(defaultValues)
        return builder
    }

    /**
     * Supported entity types for a EntitySelectMenu.
     * <br></br>Note that some combinations are unsupported by Discord, due to the restrictive API design.
     *
     *
     * The only combination that is currently supported is [.USER] + [.ROLE] (often referred to as "mentionables").
     * Combinations such as [.ROLE] + [.CHANNEL] are currently not supported.
     */
    enum class SelectTarget {
        USER,
        ROLE,
        CHANNEL
    }

    /**
     * Represents the default values used in [.getDefaultValues].
     * <br></br>The value is [typed][.getType] correspondingly to the menu [entity types][EntitySelectMenu.getEntityTypes].
     *
     *
     * The value is represented by the [ID][.getId], corresponding to the entity of that ID.
     */
    class DefaultValue protected constructor(
        override val idLong: Long, @get:Nonnull
        @param:Nonnull val type: SelectTarget
    ) : ISnowflake, SerializableData {

        @Nonnull
        override fun toData(): DataObject {
            return DataObject.empty()
                .put("type", type.name.lowercase())
                .put("id", idLong)
        }

        override fun hashCode(): Int {
            return Objects.hash(type, idLong)
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) return true
            if (obj !is DefaultValue) return false
            val other = obj
            return idLong == other.idLong && type == other.type
        }

        override fun toString(): String {
            return EntityString(this)
                .setType(type)
                .toString()
        }

        companion object {
            /**
             * Parses the provided [DataObject] into the default value.
             *
             * @param  object
             * The serialized default value, with a valid type and id
             *
             * @throws IllegalArgumentException
             * If the provided object is invalid or missing required keys
             *
             * @return Parsed default value
             */
            @JvmStatic
            @Nonnull
            fun fromData(@Nonnull `object`: DataObject): DefaultValue {
                Checks.notNull(`object`, "DataObject")
                val id = `object`.getUnsignedLong("id")
                when (`object`.getString("type")) {
                    "role" -> return role(id)
                    "user" -> return user(id)
                    "channel" -> return channel(id)
                }
                throw IllegalArgumentException("Unknown value type '" + `object`.getString("type", null) + "'")
            }

            /**
             * Creates a default value of type [SelectTarget.USER] for the provided user.
             *
             * @param  user
             * The corresponding user
             *
             * @throws IllegalArgumentException
             * If null is provided
             *
             * @return The default value
             */
            @Nonnull
            fun from(@Nonnull user: UserSnowflake): DefaultValue {
                Checks.notNull(user, "User")
                return user(user.idLong)
            }

            /**
             * Creates a default value of type [SelectTarget.ROLE] for the provided role.
             *
             * @param  role
             * The corresponding role
             *
             * @throws IllegalArgumentException
             * If null is provided
             *
             * @return The default value
             */
            @Nonnull
            fun from(@Nonnull role: Role): DefaultValue {
                Checks.notNull(role, "Role")
                return role(role.idLong)
            }

            /**
             * Creates a default value of type [SelectTarget.CHANNEL] for the provided channel.
             *
             * @param  channel
             * The corresponding channel
             *
             * @throws IllegalArgumentException
             * If null is provided
             *
             * @return The default value
             */
            @Nonnull
            fun from(@Nonnull channel: GuildChannel): DefaultValue {
                Checks.notNull(channel, "Channel")
                return channel(channel.idLong)
            }

            /**
             * Creates a default value of type [SelectTarget.ROLE] with the provided id.
             *
             * @param  id
             * The role id
             *
             * @return The default value
             */
            @Nonnull
            fun role(id: Long): DefaultValue {
                return DefaultValue(id, SelectTarget.ROLE)
            }

            /**
             * Creates a default value of type [SelectTarget.ROLE] with the provided id.
             *
             * @param  id
             * The role id
             *
             * @throws IllegalArgumentException
             * If the provided id is not a valid snowflake
             *
             * @return The default value
             */
            @JvmStatic
            @Nonnull
            fun role(@Nonnull id: String?): DefaultValue {
                return DefaultValue(MiscUtil.parseSnowflake(id), SelectTarget.ROLE)
            }

            /**
             * Creates a default value of type [SelectTarget.USER] with the provided id.
             *
             * @param  id
             * The role id
             *
             * @return The default value
             */
            @Nonnull
            fun user(id: Long): DefaultValue {
                return DefaultValue(id, SelectTarget.USER)
            }

            /**
             * Creates a default value of type [SelectTarget.USER] with the provided id.
             *
             * @param  id
             * The role id
             *
             * @throws IllegalArgumentException
             * If the provided id is not a valid snowflake
             *
             * @return The default value
             */
            @JvmStatic
            @Nonnull
            fun user(@Nonnull id: String?): DefaultValue {
                return DefaultValue(MiscUtil.parseSnowflake(id), SelectTarget.USER)
            }

            /**
             * Creates a default value of type [SelectTarget.CHANNEL] with the provided id.
             *
             * @param  id
             * The role id
             *
             * @return The default value
             */
            @Nonnull
            fun channel(id: Long): DefaultValue {
                return DefaultValue(id, SelectTarget.CHANNEL)
            }

            /**
             * Creates a default value of type [SelectTarget.CHANNEL] with the provided id.
             *
             * @param  id
             * The role id
             *
             * @throws IllegalArgumentException
             * If the provided id is not a valid snowflake
             *
             * @return The default value
             */
            @JvmStatic
            @Nonnull
            fun channel(@Nonnull id: String?): DefaultValue {
                return DefaultValue(MiscUtil.parseSnowflake(id), SelectTarget.CHANNEL)
            }
        }
    }

    /**
     * A preconfigured builder for the creation of entity select menus.
     */
    class Builder(@Nonnull customId: String?) : SelectMenu.Builder<EntitySelectMenu, Builder?>(customId) {
        protected var componentType: Component.Type? = null
        protected var channelTypes = EnumSet.noneOf(ChannelType::class.java)
        protected var defaultValues: MutableList<DefaultValue?> = ArrayList()

        /**
         * The [SelectTargets][SelectTarget] that should be supported by this menu.
         *
         * @param  types
         * The supported [SelectTargets][SelectTarget] (1-2)
         *
         * @throws IllegalArgumentException
         * If the provided targets are null, empty, or invalid.
         *
         * @return The current Builder instance
         */
        @Nonnull
        fun setEntityTypes(@Nonnull types: Collection<SelectTarget?>?): Builder {
            Checks.notEmpty(types, "Types")
            Checks.noneNull(types, "Types")
            val set = Helpers.copyEnumSet(
                SelectTarget::class.java, types
            )
            if (set.size == 1) {
                if (set.contains(SelectTarget.CHANNEL)) componentType =
                    Component.Type.CHANNEL_SELECT else if (set.contains(SelectTarget.ROLE)) componentType =
                    Component.Type.ROLE_SELECT else if (set.contains(SelectTarget.USER)) componentType =
                    Component.Type.USER_SELECT
            } else if (set.size == 2) {
                if (set.contains(SelectTarget.USER) && set.contains(SelectTarget.ROLE)) componentType =
                    Component.Type.MENTIONABLE_SELECT else throw IllegalArgumentException(
                    "The provided combination of select targets is not supported. Provided: $set"
                )
            } else {
                throw IllegalArgumentException("The provided combination of select targets is not supported. Provided: $set")
            }
            return this
        }

        /**
         * The [SelectTargets][SelectTarget] that should be supported by this menu.
         *
         * @param  type
         * The first supported [SelectTarget]
         * @param  types
         * Additional supported [SelectTargets][SelectTarget]
         *
         * @throws IllegalArgumentException
         * If the provided targets are null or invalid.
         *
         * @return The current Builder instance
         */
        @Nonnull
        fun setEntityTypes(@Nonnull type: SelectTarget?, @Nonnull vararg types: SelectTarget?): Builder {
            Checks.notNull(type, "Type")
            Checks.noneNull(types, "Types")
            return setEntityTypes(EnumSet.of(type, *types))
        }

        /**
         * The [ChannelTypes][ChannelType] that should be supported by this menu.
         * <br></br>This is only relevant for menus that allow [CHANNEL][SelectTarget.CHANNEL] targets.
         *
         * @param  types
         * The supported [ChannelTypes][ChannelType] (empty to allow all types)
         *
         * @throws IllegalArgumentException
         * If the provided types are null or not guild types
         *
         * @return The current Builder instance
         */
        @Nonnull
        fun setChannelTypes(@Nonnull types: Collection<ChannelType?>): Builder {
            Checks.noneNull(types, "Types")
            for (type in types) Checks.check(type!!.isGuild, "Only guild channel types are allowed! Provided: %s", type)
            channelTypes = Helpers.copyEnumSet(ChannelType::class.java, types)
            return this
        }

        /**
         * The [ChannelTypes][ChannelType] that should be supported by this menu.
         * <br></br>This is only relevant for menus that allow [CHANNEL][SelectTarget.CHANNEL] targets.
         *
         * @param  types
         * The supported [ChannelTypes][ChannelType] (empty to allow all types)
         *
         * @throws IllegalArgumentException
         * If the provided types are null or not guild types
         *
         * @return The current Builder instance
         */
        @Nonnull
        fun setChannelTypes(@Nonnull vararg types: ChannelType?): Builder {
            return setChannelTypes(Arrays.asList(*types))
        }

        /**
         * The [default values][.getDefaultValues] that will be shown to the user.
         *
         * @param  values
         * The default values (up to {@value #OPTIONS_MAX_AMOUNT})
         *
         * @throws IllegalArgumentException
         * If null is provided, more than {@value #OPTIONS_MAX_AMOUNT} values are provided,
         * or any of the value types is incompatible with the configured [entity types][.setEntityTypes].
         *
         * @return The current Builder instance
         */
        @Nonnull
        fun setDefaultValues(@Nonnull vararg values: DefaultValue?): Builder {
            Checks.noneNull(values, "Default Values")
            return setDefaultValues(Arrays.asList(*values))
        }

        /**
         * The [default values][.getDefaultValues] that will be shown to the user.
         *
         * @param  values
         * The default values (up to {@value #OPTIONS_MAX_AMOUNT})
         *
         * @throws IllegalArgumentException
         * If null is provided, more than {@value #OPTIONS_MAX_AMOUNT} values are provided,
         * or any of the value types is incompatible with the configured [entity types][.setEntityTypes].
         *
         * @return The current Builder instance
         */
        @Nonnull
        fun setDefaultValues(@Nonnull values: Collection<DefaultValue?>): Builder {
            Checks.noneNull(values, "Default Values")
            Checks.check(
                values.size <= SelectMenu.Companion.OPTIONS_MAX_AMOUNT,
                "Cannot add more than %d default values to a select menu!",
                SelectMenu.Companion.OPTIONS_MAX_AMOUNT
            )
            for (value in values) {
                val type = value!!.type
                val error = "The select menu supports types %s, but provided default value has type SelectTarget.%s!"
                when (componentType) {
                    Component.Type.ROLE_SELECT -> Checks.check(
                        type == SelectTarget.ROLE,
                        error,
                        "SelectTarget.ROLE",
                        type
                    )

                    Component.Type.USER_SELECT -> Checks.check(
                        type == SelectTarget.USER,
                        error,
                        "SelectTarget.USER",
                        type
                    )

                    Component.Type.CHANNEL_SELECT -> Checks.check(
                        type == SelectTarget.CHANNEL,
                        error,
                        "SelectTarget.CHANNEL",
                        type
                    )

                    Component.Type.MENTIONABLE_SELECT -> Checks.check(
                        type == SelectTarget.ROLE || type == SelectTarget.USER,
                        error,
                        "SelectTarget.ROLE and SelectTarget.USER",
                        type
                    )
                }
            }
            defaultValues.clear()
            defaultValues.addAll(values)
            return this
        }

        /**
         * Creates a new [EntitySelectMenu] instance if all requirements are satisfied.
         *
         * @throws IllegalArgumentException
         * Throws if [.getMinValues] is greater than [.getMaxValues]
         *
         * @return The new [EntitySelectMenu] instance
         */
        @Nonnull
        override fun build(): EntitySelectMenu {
            Checks.check(minValues <= maxValues, "Min values cannot be greater than max values!")
            val channelTypes = if (componentType == Component.Type.CHANNEL_SELECT) channelTypes else EnumSet.noneOf(
                ChannelType::class.java
            )
            val defaultValues: List<DefaultValue?> = ArrayList(
                defaultValues
            )
            return EntitySelectMenuImpl(
                customId,
                placeholder,
                minValues,
                maxValues,
                disabled,
                componentType,
                channelTypes,
                defaultValues
            )
        }
    }

    companion object {
        /**
         * Creates a new [Builder] for a select menu with the provided custom id.
         *
         * @param  customId
         * The id used to identify this menu with [ActionComponent.getId] for component interactions
         * @param  types
         * The supported [SelectTargets][SelectTarget]
         *
         * @throws IllegalArgumentException
         *
         *  * If the provided id is null, empty, or longer than {@value ID_MAX_LENGTH} characters.
         *  * If the provided types are null, empty, or invalid.
         *
         *
         * @return The [Builder] used to create the select menu
         */
        @Nonnull
        @CheckReturnValue
        fun create(@Nonnull customId: String?, @Nonnull types: Collection<SelectTarget?>?): Builder {
            return Builder(customId).setEntityTypes(types)
        }

        /**
         * Creates a new [Builder] for a select menu with the provided custom id.
         *
         * @param  customId
         * The id used to identify this menu with [ActionComponent.getId] for component interactions
         * @param  type
         * The first supported [SelectTarget]
         * @param  types
         * Other supported [SelectTargets][SelectTarget]
         *
         * @throws IllegalArgumentException
         *
         *  * If the provided id is null, empty, or longer than {@value ID_MAX_LENGTH} characters.
         *  * If the provided types are null or invalid.
         *
         *
         * @return The [Builder] used to create the select menu
         */
        @JvmStatic
        @Nonnull
        @CheckReturnValue
        fun create(
            @Nonnull customId: String?,
            @Nonnull type: SelectTarget?,
            @Nonnull vararg types: SelectTarget?
        ): Builder? {
            Checks.notNull(type, "Type")
            Checks.noneNull(types, "Types")
            return create(customId, EnumSet.of(type, *types))
        }
    }
}
