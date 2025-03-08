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

package net.dv8tion.jda.internal.components.selects;

import net.dv8tion.jda.api.components.selects.SelectOption;
import net.dv8tion.jda.api.components.selects.StringSelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StringSelectMenuImpl extends SelectMenuImpl implements StringSelectMenu
{
    private final List<SelectOption> options;

    public StringSelectMenuImpl(DataObject data)
    {
        super(data);
        this.options = parseOptions(data.getArray("options"));
    }

    public StringSelectMenuImpl(String id, int uniqueId, String placeholder, int minValues, int maxValues, boolean disabled, List<SelectOption> options)
    {
        super(id, uniqueId, placeholder, minValues, maxValues, disabled);
        this.options = options;
    }

    private static List<SelectOption> parseOptions(DataArray array)
    {
        List<SelectOption> options = new ArrayList<>(array.length());
        array.stream(DataArray::getObject)
                .map(SelectOption::fromData)
                .forEach(options::add);
        return options;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.STRING_SELECT;
    }

    @Nonnull
    @Override
    public List<SelectOption> getOptions()
    {
        return Collections.unmodifiableList(options);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return super.toData()
                .put("type", Type.STRING_SELECT.getKey())
                .put("options", DataArray.fromCollection(options));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, placeholder, minValues, maxValues, disabled, options);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof StringSelectMenu))
            return false;
        StringSelectMenu other = (StringSelectMenu) obj;
        return Objects.equals(id, other.getId())
                && Objects.equals(placeholder, other.getPlaceholder())
                && minValues == other.getMinValues()
                && maxValues == other.getMaxValues()
                && disabled == other.isDisabled()
                && Objects.equals(options, other.getOptions());
    }
}
