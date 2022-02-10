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

package net.dv8tion.jda.api.interactions.components.text;

import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.internal.interactions.component.ModalImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Discord Modal
 *
 * Replying to an interaction with a modal will cause a form window to pop up on the User's client.
 *
 * <b>Only a maximum of 5 components can be included in a Modal, and only {@link net.dv8tion.jda.api.interactions.components.text.TextInput TextInputs} are allowed.</b>
 *
 * @see net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent
 */
public interface Modal extends ActionComponent
{
    /**
     * The custom id of this modal
     *
     * @return The custom id of this modal
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

    @Nonnull
    @Override
    default ActionComponent withDisabled(boolean disabled)
    {
        throw new UnsupportedOperationException("Modals cannot be disabled!");
    }

    /**
     * Creates a new Modal.
     *
     * @param customId The custom id for this modal
     *
     * @throws IllegalArgumentException
     *         If the provided customId or title are null
     *
     * @return {@link Builder Builder} instance to customize this modal further
     */
    @Nonnull
    @CheckReturnValue
    static Modal.Builder create(@Nonnull String customId, @Nonnull String title)
    {
        Checks.notNull(customId, "Custom ID");
        Checks.notNull(title, "Title");
        return new Modal.Builder(customId).setTitle(title);
    }

    class Builder
    {
        private String id;
        private String title;
        private final List<ActionRow> components = new ArrayList<>();

        protected Builder(@Nonnull String customId)
        {
            setId(customId);
        }

        /**
         * Sets the custom id for this modal.
         *
         * @param customId Custom id
         *
         * @return The same builder instance for chaining
         */
        public Builder setId(@Nonnull String customId)
        {
            this.id = customId;
            return this;
        }

        /**
         * Sets the title for this modal.
         *
         * @param title The title
         *
         * @return The same builder instance for chaining
         */
        public Builder setTitle(String title)
        {
            this.title = title;
            return this;
        }

        /**
         * Adds ActionRows to this modal
         *
         * @param actionRows Vararg of ActionRows
         *
         * @throws IllegalArgumentException
         *         If any of the provided ActionRows are null
         *
         * @return The same builder instance for chaining
         */
        public Builder addActionRows(@NonNull ActionRow... actionRows)
        {
            Checks.noneNull(actionRows, "Action Rows");
            Collections.addAll(this.components, actionRows);
            return this;
        }

        /**
         * Adds components to this modal
         *
         * @param actionRows Collection of ActionRows
         *
         * @throws IllegalArgumentException
         *         If any of the provided ActionRows are null
         *
         * @return The same builder instance for chaining
         */
        public Builder addActionRows(@NonNull Collection<? extends ActionRow> actionRows)
        {
            Checks.noneNull(actionRows, "Components");
            this.components.addAll(actionRows);
            return this;
        }

        /**
         * Returns a list of all components
         *
         * @return A list of components
         */
        public List<ActionRow> getComponents()
        {
            return Collections.unmodifiableList(components);
        }

        /**
         * Returns the title
         *
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }

        /**
         * Returns the custom id
         *
         * @return the id
         */
        public String getId()
        {
            return id;
        }

        /**
         * Builds and returns the {@link Modal}
         *
         * @throws IllegalArgumentException
         *         If the id is null
         *         If the title is null
         *         If the components are empty
         *         If there are more than 5 components
         *
         * @return A Modal
         */
        public Modal build()
        {
            Checks.check(id != null, "Custom ID cannot be null!");
            Checks.check(title != null, "Title cannot be null!");
            Checks.check(!components.isEmpty(), "Cannot make a modal with no components!");
            Checks.check(components.size() <= 5, "Cannot make a modal with more than 5 components!");

            return new ModalImpl(id, title, components);
        }
    }
}
