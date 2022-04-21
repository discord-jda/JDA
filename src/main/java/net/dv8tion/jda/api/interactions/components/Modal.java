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

package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.ModalInteraction;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.component.ModalImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a Discord Modal
 *
 * <p>Replying to an interaction with a modal will open an interactive popout on the User's Discord client.
 * This is similar to the ban modal where you can input a ban reason.
  *
  * <h2>Example</h2>
 * <pre>{@code
 * public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event)
 * {
 *     if (event.getName().equals("modmail"))
 *     {
 *         TextInput subject = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
 *                 .setPlaceholder("Subject of this ticket")
 *                 .setMinLength(10)
 *                 .setMaxLength(100) // or setRequiredRange(10, 100)
 *                 .build();
 *
 *         TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
 *                 .setPlaceholder("Your concerns go here")
 *                 .setMinLength(30)
 *                 .setMaxLength(1000)
 *                 .build();
 *
 *         Modal modal = Modal.create("modmail", "Modmail")
 *                 .addActionRows(ActionRow.of(subject), ActionRow.of(body))
 *                 .build();
 *
 *         event.replyModal(modal).queue();
 *     }
 * }}</pre>
 *
 * <p><b>Only a maximum of 5 component layouts can be included in a Modal, and only {@link net.dv8tion.jda.api.interactions.components.text.TextInput TextInputs} are allowed at this time.</b>
 * You can check whether a component is supported via {@link net.dv8tion.jda.api.interactions.components.Component.Type#isModalCompatible}.
 *
 * @see    ModalInteractionEvent
 */
public interface Modal extends SerializableData
{
    /**
     * The maximum amount of components a Modal can have. ({@value})
     */
    int MAX_COMPONENTS = 5;

    /**
     * The maximum length a modal custom id can have. ({@value})
     */
    int MAX_ID_LENGTH = 100;

    /**
     * The maximum length a modal title can have. ({@value})
     */
    int MAX_TITLE_LENGTH = 45;

    /**
     * The custom id of this modal
     *
     * @return The custom id of this modal
     *
     * @see    ModalInteraction#getModalId()
     */
    @Nonnull
    String getId();

    /**
     * The title of this modal
     *
     * @return The title of this modal
     */
    @Nonnull
    String getTitle();

    /**
     * A List of {@link net.dv8tion.jda.api.interactions.components.ActionRow ActionRows} that this modal contains.
     *
     * @return List of ActionRows
     */
    @Nonnull
    List<ActionRow> getActionRows();

    /**
     * Creates a new preconfigured {@link Modal.Builder} with the same settings used for this modal.
     * <br>This can be useful to create an updated version of this modal without needing to rebuild it from scratch.
     *
     * @return The {@link Modal.Builder} used to create the modal
     */
    @Nonnull
    default Modal.Builder createCopy()
    {
        return new Builder(getId(), getTitle())
                .addActionRows(getActionRows());
    }

    /**
     * Creates a new Modal. You must add at least one component to a modal before building it.
     *
     * @param  customId 
     *         The custom id for this modal
     * @param  title
     *         The title for this modal
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided customId or title are null, empty, or blank</li>
     *             <li>If the provided customId is longer than {@value MAX_ID_LENGTH} characters</li>
     *             <li>If the provided title is longer than {@value #MAX_TITLE_LENGTH} characters</li>
     *         </ul>
     *
     * @return {@link Builder Builder} instance to customize this modal further
     */
    @Nonnull
    @CheckReturnValue
    static Modal.Builder create(@Nonnull String customId, @Nonnull String title)
    {
        return new Modal.Builder(customId, title);
    }

    /**
     * A preconfigured builder for the creation of modals.
     */
    class Builder
    {
        private final List<ActionRow> components = new ArrayList<>(5);
        private String id;
        private String title;

        protected Builder(@Nonnull String customId, @Nonnull String title)
        {
            setId(customId);
            setTitle(title);
        }

        /**
         * Sets the custom id for this modal.
         *
         * @param  customId
         *         Custom id
         *
         * @throws IllegalArgumentException
         *         If the provided id is null, blank, or is longer than {@value #MAX_ID_LENGTH} characters.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setId(@Nonnull String customId)
        {
            Checks.notBlank(customId, "ID");
            Checks.notLonger(customId, MAX_ID_LENGTH, "ID");
            this.id = customId;
            return this;
        }

        /**
         * Sets the title for this modal.
         *
         * @param  title 
         *         The title
         *
         * @throws IllegalArgumentException
         *         If the provided title is null, blank or longer than {@value #MAX_TITLE_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setTitle(@Nonnull String title)
        {
            Checks.notBlank(title, "Title");
            Checks.notLonger(title, MAX_TITLE_LENGTH, "Title");
            this.title = title;
            return this;
        }

        /**
         * Adds ActionRows to this modal
         *
         * @param  actionRows 
         *         ActionRows to add to the modal, up to 5
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If any of the provided ActionRows are null</li>
         *             <li>If any of the provided ActionRows' components are not compatible with Modals</li>
         *         </ul>
         *
         * @return The same builder instance for chaining
         *
         * @see    ActionRow#isModalCompatible()
         */
        @Nonnull
        public Builder addActionRows(@Nonnull ActionRow... actionRows)
        {
            Checks.noneNull(actionRows, "Action Rows");
            return addActionRows(Arrays.asList(actionRows));
        }

        /**
         * Adds ActionRows to this modal
         *
         * @param  actionRows 
         *         ActionRows to add to the modal, up to 5
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If any of the provided ActionRows are null</li>
         *             <li>If any of the provided ActionRows' components are not compatible with Modals</li>
         *         </ul>
         *
         * @return The same builder instance for chaining
         *
         * @see    ActionRow#isModalCompatible()
         */
        @Nonnull
        public Builder addActionRows(@Nonnull Collection<? extends ActionRow> actionRows)
        {
            Checks.noneNull(actionRows, "Components");

            Checks.checkComponents("Some components are incompatible with Modals",
                actionRows,
                component -> component.getType().isModalCompatible());

            this.components.addAll(actionRows);
            return this;
        }

        /**
         * Adds an ActionRow to this modal
         *
         * @param  components
         *         The components to add
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If any of the provided ItemComponents are null, or an invalid number of components are provided</li>
         *             <li>If any of the provided ItemComponents are not compatible with Modals</li>
         *         </ul>
         *
         * @return Same builder for chaining convenience
         *
         * @see    ItemComponent#isModalCompatible()
         */
        @Nonnull
        public Builder addActionRow(@Nonnull Collection<? extends ItemComponent> components)
        {
            return addActionRows(ActionRow.of(components));
        }

        /**
         * Adds an ActionRow to this modal
         *
         * @param  components
         *         The components to add
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If any of the provided ItemComponents are null, or an invalid number of components are provided</li>
         *             <li>If any of the provided ItemComponents are not compatible with Modals</li>
         *         </ul>
         *
         * @return Same builder for chaining convenience
         *
         * @see    ItemComponent#isModalCompatible()
         */
        @Nonnull
        public Builder addActionRow(@Nonnull ItemComponent... components)
        {
            return addActionRows(ActionRow.of(components));
        }

        /**
         * Returns a modifiable list of all components
         *
         * @return A modifiable list of all components
         */
        @Nonnull
        public List<ActionRow> getActionRows()
        {
            return components;
        }

        /**
         * Returns the title
         *
         * @return the title
         */
        @Nonnull
        public String getTitle()
        {
            return title;
        }

        /**
         * Returns the custom id
         *
         * @return the id
         */
        @Nonnull
        public String getId()
        {
            return id;
        }

        /**
         * Builds and returns the {@link Modal}
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the components are empty</li>
         *             <li>If there are more than 5 components</li>
         *         </ul>
         *
         * @return A Modal
         */
        @Nonnull
        public Modal build()
        {
            Checks.check(!components.isEmpty(), "Cannot make a modal without components!");
            Checks.check(components.size() <= MAX_COMPONENTS, "Cannot make a modal with more than 5 components!");

            return new ModalImpl(id, title, components);
        }
    }
}
