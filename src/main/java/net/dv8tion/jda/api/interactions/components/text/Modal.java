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
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Modal extends ActionComponent
{
    @NotNull
    String getId();

    @NotNull
    String getTitle();

    @NotNull
    List<ActionRow> getComponents();

    @NotNull
    @Override
    default ActionComponent withDisabled(boolean disabled)
    {
        throw new UnsupportedOperationException("Modals cannot be disabled!");
    }

    @NotNull
    @CheckReturnValue
    static Modal.Builder create(@NotNull String customId)
    {
        Checks.notNull(customId, "Custom ID");
        return new Modal.Builder(customId);
    }

    class Builder
    {
        private String id;
        private String title;
        private final List<ActionRow> components = new ArrayList<>();

        protected Builder(@NotNull String customId)
        {
            setId(customId);
        }

        public Builder setId(@NotNull String customId)
        {
            this.id = customId;
            return this;
        }

        public Builder setTitle(String title)
        {
            this.title = title;
            return this;
        }

        public Builder addComponents(ActionRow... components)
        {
            Checks.noneNull(components, "Components");
            Collections.addAll(this.components, components);
            return this;
        }

        public Builder addComponents(Collection<? extends ActionRow> components)
        {
            Checks.noneNull(components, "Components");
            this.components.addAll(components);
            return this;
        }

        public List<ActionRow> getComponents()
        {
            return Collections.unmodifiableList(components);
        }

        public String getTitle()
        {
            return title;
        }

        public String getId()
        {
            return id;
        }

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
