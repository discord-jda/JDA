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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

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
        long guildId = content.getUnsignedLong("guild_id", 0);
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;

        // TODO: What about private channels?
        GuildImpl guild = (GuildImpl) api.getGuildById(guildId);
        long channelId = content.getUnsignedLong("channel_id");
        MessageChannel channel = guild.getTextChannelById(channelId); // TODO: Direct messages
        long interactionId = content.getUnsignedLong("id");
        int type = content.getInt("type");
        String commandToken = content.getString("token");

        DataObject data = content.getObject("data");
        long commandId = data.getUnsignedLong("id");
        String commandName = data.getString("name");
        DataArray options = data.optArray("options").orElseGet(DataArray::empty);

        EntityBuilder entityBuilder = api.getEntityBuilder();
        Member member = entityBuilder.createMember(guild, content.getObject("member"));
        entityBuilder.updateMemberCache((MemberImpl) member);

        // Resolve all necessary option values to actual types
        DataObject resolveJson = data.optObject("resolved").orElseGet(DataObject::empty);
        TLongObjectMap<Object> resolved = new TLongObjectHashMap<>();
        resolveJson.optObject("users").ifPresent(users ->
            users.keys().forEach(userId -> {
                DataObject userJson = users.getObject(userId);
                UserImpl user = entityBuilder.createUser(userJson);
                resolved.put(user.getIdLong(), user);
            })
        );
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
                 .map(guild::getRoleById)
                 .filter(Objects::nonNull)
                 .forEach(role -> resolved.put(role.getIdLong(), role))
        );
        resolveJson.optObject("channels").ifPresent(channels -> {}); // TODO

        // Create option POJO including resolved entities
        List<SlashCommandEvent.OptionData> optionList = options.stream(DataArray::getObject)
               .map(json -> new SlashCommandEvent.OptionData(json, resolved))
               .collect(Collectors.toList());

        api.handleEvent(
            new SlashCommandEvent(api, responseNumber,
                commandToken, interactionId, guild, member,
                member.getUser(), channel, commandName, commandId,
                optionList));
        return null;
    }
}
