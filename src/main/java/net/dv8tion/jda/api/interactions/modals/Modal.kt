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
package net.dv8tion.jda.api.interactions.modals

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ActionRow.Companion.of
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.interactions.modal.ModalImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.util.*
import java.util.function.Predicate
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a Discord Modal
 *
 *
 * Replying to an interaction with a modal will open an interactive popout on the User's Discord client.
 * This is similar to the ban modal where you can input a ban reason.
 *
 *
 * **Example**<br></br>
 * <pre>`public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event)
 * {
 * if (event.getName().equals("modmail"))
 * {
 * TextInput subject = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
 * .setPlaceholder("Subject of this ticket")
 * .setMinLength(10)
 * .setMaxLength(100) // or setRequiredRange(10, 100)
 * .build();
 *
 * TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
 * .setPlaceholder("Your concerns go here")
 * .setMinLength(30)
 * .setMaxLength(1000)
 * .build();
 *
 * Modal modal = Modal.create("modmail", "Modmail")
 * .addComponents(ActionRow.of(subject), ActionRow.of(body))
 * .build();
 *
 * event.replyModal(modal).queue();
 * }
 * }`</pre>
 *
 *
 * **Only a maximum of 5 component layouts can be included in a Modal, and only [TextInputs][net.dv8tion.jda.api.interactions.components.text.TextInput] are allowed at this time.**
 * You can check whether a component is supported via [net.dv8tion.jda.api.interactions.components.Component.Type.isModalCompatible].
 *
 * @see ModalInteractionEvent
 */
interface Modal : SerializableData {
    @get:Nonnull
    val id: String?

    @get:Nonnull
    val title: String?

    @get:Deprecated("Use {@link #getComponents()} instead")
    @get:ReplaceWith("getComponents()")
    @get:ForRemoval
    @get:Nonnull
    val actionRows: List<ActionRow>?
        /**
         * A List of [ActionRows][net.dv8tion.jda.api.interactions.components.ActionRow] that this modal contains.
         *
         * @return List of ActionRows
         *
         */
        get() = components.stream()
            .filter { obj: LayoutComponent? -> ActionRow::class.java.isInstance(obj) }
            .map { obj: LayoutComponent? -> ActionRow::class.java.cast(obj) }
            .collect(Helpers.toUnmodifiableList())

    @get:Nonnull
    val components: List<LayoutComponent?>

    /**
     * Creates a new preconfigured [Modal.Builder] with the same settings used for this modal.
     * <br></br>This can be useful to create an updated version of this modal without needing to rebuild it from scratch.
     *
     * @return The [Modal.Builder] used to create the modal
     */
    @Nonnull
    fun createCopy(): Builder? {
        return Builder(id, title)
            .addComponents(components)
    }

    /**
     * A preconfigured builder for the creation of modals.
     */
    class Builder(@Nonnull customId: String?, @Nonnull title: String?) {
        private val components: MutableList<LayoutComponent?> = ArrayList(MAX_COMPONENTS)

        /**
         * Returns the custom id
         *
         * @return the id
         */
        @get:Nonnull
        var id: String? = null
            private set

        /**
         * Returns the title
         *
         * @return the title
         */
        @get:Nonnull
        var title: String? = null
            private set

        init {
            setId(customId)
            setTitle(title)
        }

        /**
         * Sets the custom id for this modal.
         *
         * @param  customId
         * Custom id
         *
         * @throws IllegalArgumentException
         * If the provided id is null, blank, or is longer than {@value #MAX_ID_LENGTH} characters.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setId(@Nonnull customId: String?): Builder {
            Checks.notBlank(customId, "ID")
            Checks.notLonger(customId, MAX_ID_LENGTH, "ID")
            id = customId
            return this
        }

        /**
         * Sets the title for this modal.
         *
         * @param  title
         * The title
         *
         * @throws IllegalArgumentException
         * If the provided title is null, blank or longer than {@value #MAX_TITLE_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setTitle(@Nonnull title: String?): Builder {
            Checks.notBlank(title, "Title")
            Checks.notLonger(title, MAX_TITLE_LENGTH, "Title")
            this.title = title
            return this
        }

        /**
         * Adds ActionRows to this modal
         *
         * @param  actionRows
         * ActionRows to add to the modal, up to 5
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the provided ActionRows are null
         *  * If any of the provided ActionRows' components are not compatible with Modals
         *
         *
         * @return The same builder instance for chaining
         *
         * @see ActionRow.isModalCompatible
         */
        @Nonnull
        @ForRemoval
        @Deprecated("")
        @ReplaceWith("addComponents(actionRows)")
        fun addActionRows(@Nonnull vararg actionRows: ActionRow?): Builder {
            return addComponents(*actionRows)
        }

        /**
         * Adds ActionRows to this modal
         *
         * @param  actionRows
         * ActionRows to add to the modal, up to 5
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the provided ActionRows are null
         *  * If any of the provided ActionRows' components are not compatible with Modals
         *
         *
         * @return The same builder instance for chaining
         *
         * @see ActionRow.isModalCompatible
         */
        @Nonnull
        @ForRemoval
        @Deprecated("")
        @ReplaceWith("addComponents(actionRows)")
        fun addActionRows(@Nonnull actionRows: Collection<ActionRow?>?): Builder {
            return addComponents(actionRows)
        }

        /**
         * Adds [LayoutComponents][LayoutComponent] to this modal
         *
         * @param  components
         * [LayoutComponents][LayoutComponent] to add to the modal, up to {@value MAX_COMPONENTS} total
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the provided layouts are null
         *  * If any of the provided components are not compatible with Modals
         *
         *
         * @return The same builder instance for chaining
         *
         * @see LayoutComponent.isModalCompatible
         */
        @Nonnull
        fun addComponents(@Nonnull vararg components: LayoutComponent?): Builder {
            Checks.noneNull(components, "Action Rows")
            return addComponents(Arrays.asList(*components))
        }

        /**
         * Adds [LayoutComponents][LayoutComponent] to this modal
         *
         * @param  components
         * [LayoutComponents][LayoutComponent] to add to the modal, up to {@value MAX_COMPONENTS} total
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the provided layouts are null
         *  * If any of the provided components are not compatible with Modals
         *
         *
         * @return The same builder instance for chaining
         *
         * @see LayoutComponent.isModalCompatible
         */
        @Nonnull
        fun addComponents(@Nonnull components: Collection<LayoutComponent?>?): Builder {
            Checks.noneNull(components, "Components")
            Checks.checkComponents("Some components are incompatible with Modals",
                components,
                Predicate { component: Component -> component.type!!.isModalCompatible() })
            this.components.addAll(components!!)
            return this
        }

        /**
         * Adds an ActionRow to this modal
         *
         * @param  components
         * The components to add
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the provided ItemComponents are null, or an invalid number of components are provided
         *  * If any of the provided ItemComponents are not compatible with Modals
         *
         *
         * @return Same builder for chaining convenience
         *
         * @see ItemComponent.isModalCompatible
         */
        @Nonnull
        fun addActionRow(@Nonnull components: Collection<ItemComponent?>?): Builder {
            return addComponents(of(components))
        }

        /**
         * Adds an ActionRow to this modal
         *
         * @param  components
         * The components to add
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the provided ItemComponents are null, or an invalid number of components are provided
         *  * If any of the provided ItemComponents are not compatible with Modals
         *
         *
         * @return Same builder for chaining convenience
         *
         * @see ItemComponent.isModalCompatible
         */
        @Nonnull
        fun addActionRow(@Nonnull vararg components: ItemComponent?): Builder {
            return addComponents(of(*components))
        }

        @get:ReplaceWith("getComponents()")
        @get:Deprecated("")
        @get:ForRemoval
        @get:Nonnull
        val actionRows: List<ActionRow>
            /**
             * Returns an immutable list of all ActionRow components
             *
             * @return An immutable list of all ActionRow components
             */
            get() = components.stream()
                .filter { obj: LayoutComponent? -> ActionRow::class.java.isInstance(obj) }
                .map { obj: LayoutComponent? -> ActionRow::class.java.cast(obj) }
                .collect(Helpers.toUnmodifiableList())

        /**
         * Returns a modifiable list of all components
         *
         * @return A modifiable list of all components
         */
        @Nonnull
        fun getComponents(): List<LayoutComponent?> {
            return components
        }

        /**
         * Builds and returns the [Modal]
         *
         * @throws IllegalArgumentException
         *
         *  * If no components are added
         *  * If more than {@value MAX_COMPONENTS} component layouts are added
         *
         *
         * @return A Modal
         */
        @Nonnull
        fun build(): Modal {
            Checks.check(!components.isEmpty(), "Cannot make a modal without components!")
            Checks.check(components.size <= MAX_COMPONENTS, "Cannot make a modal with more than 5 components!")
            return ModalImpl(id, title, components)
        }
    }

    companion object {
        /**
         * Creates a new Modal. You must add at least one component to a modal before building it.
         *
         * @param  customId
         * The custom id for this modal
         * @param  title
         * The title for this modal
         *
         * @throws IllegalArgumentException
         *
         *  * If the provided customId or title are null, empty, or blank
         *  * If the provided customId is longer than {@value MAX_ID_LENGTH} characters
         *  * If the provided title is longer than {@value #MAX_TITLE_LENGTH} characters
         *
         *
         * @return [Builder] instance to customize this modal further
         */
        @Nonnull
        @CheckReturnValue
        fun create(@Nonnull customId: String?, @Nonnull title: String?): Builder? {
            return Builder(customId, title)
        }

        /**
         * The maximum amount of components a Modal can have. ({@value})
         */
        const val MAX_COMPONENTS = 5

        /**
         * The maximum length a modal custom id can have. ({@value})
         */
        const val MAX_ID_LENGTH = 100

        /**
         * The maximum length a modal title can have. ({@value})
         */
        const val MAX_TITLE_LENGTH = 45
    }
}
