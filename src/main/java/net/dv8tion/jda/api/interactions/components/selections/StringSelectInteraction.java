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

import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Component Interaction for a {@link StringSelectMenu}.
 *
 * @see StringSelectInteractionEvent
 */
public interface StringSelectInteraction extends SelectMenuInteraction<String, StringSelectMenu>
{
    /**
     * The selected values.
     * <br>These are defined in the individual {@link SelectOption SelectOptions}.
     *
     * @return {@link List} of {@link SelectOption#getValue()}
     */
    @Nonnull
    @Unmodifiable
    List<String> getValues();

    /**
     * This resolves the selected {@link #getValues() values} to the representative {@link SelectOption SelectOption} instances.
     * <br>It is recommended to check {@link #getValues()} directly instead of using the options.
     *
     * @return Immutable {@link List} of the selected options
     */
    @Nonnull
    @Unmodifiable
    default List<SelectOption> getSelectedOptions()
    {
        StringSelectMenu menu = getComponent();
        List<String> values = getValues();
        return menu.getOptions()
                .stream()
                .filter(it -> values.contains(it.getValue()))
                .collect(Helpers.toUnmodifiableList());
    }
}
