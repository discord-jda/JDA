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

package net.dv8tion.jda.api.interactions.commands.build;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public class SubcommandData extends BaseCommand<CommandData> implements SerializableData
{
    public SubcommandData(@Nonnull String name, @Nonnull String description)
    {
        super(name, description);
    }

    @Nonnull
    public SubcommandData addOption(@Nonnull OptionData data)
    {
        Checks.notNull(data, "Option");
        options.add(data);
        return this;
    }

    @Nonnull
    public SubcommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOption(new OptionData(type, name, description).setRequired(required));
    }

    @Nonnull
    public SubcommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return addOption(type, name, description, false);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return super.toData().put("type", OptionType.SUB_COMMAND.getKey());
    }

    @Nonnull
    public static SubcommandData load(@Nonnull DataObject json)
    {
        String name = json.getString("name");
        String description = json.getString("description");
        SubcommandData sub = new SubcommandData(name, description);
        json.optArray("options").ifPresent(arr ->
                arr.stream(DataArray::getObject)
                        .map(OptionData::load)
                        .forEach(sub::addOption)
        );
        return sub;
    }
}
