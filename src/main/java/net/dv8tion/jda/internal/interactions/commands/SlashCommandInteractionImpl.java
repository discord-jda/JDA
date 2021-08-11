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

package net.dv8tion.jda.internal.interactions.commands;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.interactions.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SlashCommandInteractionImpl extends CommandInteractionImpl implements SlashCommandInteraction
{
    private final List<OptionMapping> options = new ArrayList<>();
    private String subcommand;
    private String group;

    public SlashCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        DataObject commandData = data.getObject("data");

        DataArray options = commandData.optArray("options").orElseGet(DataArray::empty);

        if (options.length() == 1)
        {
            DataObject option = options.getObject(0);
            switch (OptionType.fromKey(option.getInt("type")))
            {
                case SUB_COMMAND_GROUP:
                    group = option.getString("name");
                    options = option.getArray("options");
                    option = options.getObject(0);
                case SUB_COMMAND:
                    subcommand = option.getString("name");
                    options = option.optArray("options").orElseGet(DataArray::empty); // Flatten options
                    break;
            }
        }

        parseOptions(options);
    }

    private void parseOptions(DataArray options)
    {
        options.stream(DataArray::getObject)
            .map(json -> new OptionMapping(json, resolved))
            .forEach(this.options::add);
    }

    @Override
    public String getSubcommandName()
    {
        return subcommand;
    }

    @Override
    public String getSubcommandGroup()
    {
        return group;
    }

    @Nonnull
    @Override
    public List<OptionMapping> getOptions()
    {
        return options;
    }
}
