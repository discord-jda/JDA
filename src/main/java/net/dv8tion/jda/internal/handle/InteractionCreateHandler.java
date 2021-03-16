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

package net.dv8tion.jda.internal.handle;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InteractionCreateHandler extends SocketHandler
{
    public InteractionCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        int type = content.getInt("type");
        if (type != 2)
            return null;
        long guildId = content.getUnsignedLong("guild_id", 0);
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;

        EntityBuilder entityBuilder = api.getEntityBuilder();

        boolean isGuild = guildId != 0L;
        GuildImpl guild = (GuildImpl) api.getGuildById(guildId);
        long channelId = content.getUnsignedLong("channel_id");
        MessageChannel channel = api.getTextChannelById(channelId);
        User user = null;
        if (!isGuild)
        {
            channel = api.getPrivateChannelById(channelId);
            if (channel == null)
                channel = new PrivateChannelImpl(channelId, user = entityBuilder.createUser(content.getObject("user")));
            else
                user = ((PrivateChannel) channel).getUser();
        }
        if (channel == null || (isGuild && guild == null))
            return null;

        long interactionId = content.getUnsignedLong("id");
        String commandToken = content.getString("token");

        DataObject data = content.getObject("data");
        long commandId = data.getUnsignedLong("id");
        String commandName = data.getString("name");
        DataArray options = data.optArray("options").orElseGet(DataArray::empty);

        Member member = isGuild ? entityBuilder.createMember(guild, content.getObject("member")) : null;
        if (member != null)
        {
            entityBuilder.updateMemberCache((MemberImpl) member);
            user = member.getUser();
        }

        // Resolve all necessary option values to actual types
        DataObject resolveJson = data.optObject("resolved").orElseGet(DataObject::empty);
        TLongObjectMap<Object> resolved = new TLongObjectHashMap<>();
        resolveJson.optObject("users").ifPresent(users ->
            users.keys().forEach(userId -> {
                DataObject userJson = users.getObject(userId);
                UserImpl userArg = entityBuilder.createUser(userJson);
                resolved.put(userArg.getIdLong(), userArg);
            })
        );
        if (isGuild) // Technically these can function in DMs too ...
        {
            resolveJson.optObject("members").ifPresent(members ->
                members.keys().forEach(memberId -> {
                    DataObject userJson = resolveJson.getObject("users").getObject(memberId);
                    DataObject memberJson = members.getObject(memberId);
                    memberJson.put("user", userJson);
                    MemberImpl optionMember = entityBuilder.createMember(guild, memberJson);
                    entityBuilder.updateMemberCache(optionMember);
                    resolved.put(optionMember.getIdLong(), optionMember); // This basically upgrades user to member
                })
            );
            resolveJson.optObject("roles").ifPresent(roles ->
                roles.keys()
                     .stream()
                     .map(guild::getRoleById)// TODO: What if its not from this guild?
                     .filter(Objects::nonNull)
                     .forEach(role -> resolved.put(role.getIdLong(), role))
            );
            resolveJson.optObject("channels").ifPresent(channels -> {
                channels.keys().forEach(id -> {
                    ISnowflake channelObj = api.getGuildChannelById(id);
                    if (channelObj == null)
                        channelObj = api.getPrivateChannelById(id);
                    if (channelObj != null)
                        resolved.put(channelObj.getIdLong(), channelObj);
                });
            }); // TODO: Handle sharding, private channels, and channels the bot isn't in somehow
        }

        // Create option POJO including resolved entities
        List<SlashCommandEvent.OptionData> optionList = options.stream(DataArray::getObject)
               .map(json -> new SlashCommandEvent.OptionData(json, resolved))
               .collect(Collectors.toList());

        api.handleEvent(
            new SlashCommandEvent(api, responseNumber,
                commandToken, interactionId, guild, member,
                user, channel, commandName, commandId,
                optionList));
        return null;
    }
}
