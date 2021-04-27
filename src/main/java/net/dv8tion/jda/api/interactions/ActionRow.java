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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ActionRow implements SerializableData
{
    private final List<Component> components = new ArrayList<>();

    private ActionRow() {}

    @Nonnull
    public static ActionRow load(@Nonnull DataArray array)
    {
        Checks.notNull(array, "DataArray");
        ActionRow row = new ActionRow();
        array.stream(DataArray::getObject)
            .map(ButtonImpl::new)
            .forEach(row.components::add);
        return row;
    }

    @Nonnull
    public static ActionRow of(@Nonnull Collection<? extends Component> components)
    {
        Checks.noneNull(components, "Components");
        return of(components.toArray(new Component[0]));
    }

    @Nonnull
    public static ActionRow of(@Nonnull Component... components)
    {
        Checks.noneNull(components, "Components");
        Checks.check(components.length <= 5, "Can only have 5 components per action row!");
        Checks.check(components.length > 0, "Cannot have empty row!");
        ActionRow row = new ActionRow();
        Collections.addAll(row.components, components);
        return row;
    }

    @Nonnull
    public List<Component> getComponents()
    {
        return Collections.unmodifiableList(components);
    }

    @Nonnull
    public List<Button> getButtons()
    {
        return Collections.unmodifiableList(
            getComponents().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("type", 1)
                .put("components", DataArray.fromCollection(components));
    }
}
