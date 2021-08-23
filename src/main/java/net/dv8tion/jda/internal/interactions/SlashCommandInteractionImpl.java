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

package net.dv8tion.jda.internal.interactions;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SlashCommandInteractionImpl extends CommandInteractionImpl implements SlashCommandInteraction
{
    private List<OptionMapping> options;
    private TLongObjectMap<Object> resolved;
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
    protected void parseResolved(JDAImpl jda, DataObject resolveJson)
    {
        options = new ArrayList<>();
        resolved = new TLongObjectHashMap<>();
        EntityBuilder entityBuilder = jda.getEntityBuilder();

        resolveJson.optObject("users").ifPresent(users ->
            users.keys().forEach(userId -> {
                DataObject userJson = users.getObject(userId);
                UserImpl userArg = entityBuilder.createUser(userJson);
                resolved.put(userArg.getIdLong(), userArg);
            })
        );

        if (guild != null) // Technically these can function in DMs too ...
        {
            resolveJson.optObject("members").ifPresent(members ->
                members.keys().forEach(memberId -> {
                    DataObject userJson = resolveJson.getObject("users").getObject(memberId);
                    DataObject memberJson = members.getObject(memberId);
                    memberJson.put("user", userJson);
                    MemberImpl optionMember = entityBuilder.createMember((GuildImpl) guild, memberJson);
                    entityBuilder.updateMemberCache(optionMember);
                    resolved.put(optionMember.getIdLong(), optionMember); // This basically upgrades user to member
                })
            );
            resolveJson.optObject("roles").ifPresent(roles ->
                roles.keys()
                    .stream()
                    .map(guild::getRoleById)
                    .filter(Objects::nonNull)
                    .forEach(role -> resolved.put(role.getIdLong(), role))
            );
            resolveJson.optObject("channels").ifPresent(channels -> {
                channels.keys().forEach(id -> {
                    ISnowflake channelObj = jda.getGuildChannelById(id);
                    if (channelObj != null)
                        resolved.put(channelObj.getIdLong(), channelObj);
                });
            });
        }
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
