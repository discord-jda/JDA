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
import java.util.List;
import java.util.stream.Collectors;

public class SubcommandGroupData extends OptionData implements SerializableData
{
    private final DataArray options = DataArray.empty();

    public SubcommandGroupData(String name, String description)
    {
        super(OptionType.SUB_COMMAND_GROUP, name, description);
    }

    @Nonnull
    public List<SubcommandData> getSubcommands()
    {
        return options.stream(DataArray::getObject)
                .map(SubcommandData::load)
                .collect(Collectors.toList());
    }

    @Nonnull
    public SubcommandGroupData addSubcommand(@Nonnull SubcommandData data)
    {
        Checks.notNull(data, "Subcommand");
        options.add(data);
        return this;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return super.toData().put("options", options);
    }

    @Nonnull
    public static SubcommandGroupData load(@Nonnull DataObject json)
    {
        String name = json.getString("name");
        String description = json.getString("description");
        SubcommandGroupData group = new SubcommandGroupData(name, description);
        json.optArray("options").ifPresent(arr ->
                arr.stream(DataArray::getObject)
                        .map(SubcommandData::load)
                        .forEach(group::addSubcommand)
        );
        return group;
    }
}
