/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;

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
        /*
        {
          "data" : {
            "name" : "ban",
            "options" : [ {
              "name" : "target",
              "value" : "<@!240254129333731328>"
            }, {
              "name" : "reason",
              "value" : "test"
            } ],
            "id" : 772445588758986773
          },
          "guild_id" : 163772719836430337,
          "member" : {
            "joined_at" : "2016-03-27T22:14:19.172000+00:00",
            "nick" : "minn",
            "premium_since" : null,
            "permissions" : "2147483647",
            "roles" : [ 708018860359417907, 708018861135626342 ],
            "deaf" : false,
            "mute" : false,
            "user" : {
              "avatar" : "cdd91cdb0447e439ec242f669bc9a364",
              "id" : 86699011792191488,
              "public_flags" : 512,
              "discriminator" : "6688",
              "username" : "Minn"
            },
            "is_pending" : false
          },
          "id" : 775701966428831764,
          "type" : 2,
          "channel_id" : 279710874984382464,
          "token" : "aW50ZXJhY3Rpb246Nzc1NzAxOTY2NDI4ODMxNzY0OnRXZkdBdTUwUWl1VWx6UGtCN3FjNWE4RFFDM3ZjMlE5WENtRks0dUZmQmlqaENUQUdKRUpzU05tNE1xQlhYQkxPM2FhOEcyeFJJZGNrTlYxZDFKRzUwVXdyMDQ0VEdoWTlzczU4SnRIREpFWVMya3ExczhBOWFLUzhEVjZ2UHJL"
        }
        */
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> RECEIVED COMMAND INPUT");
//        JDAImpl.LOG.info("Received event");
        long guildId = content.getUnsignedLong("guild_id", 0);
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;

        // TODO: What about private channels? Will the member just only contain a user?
        Guild guild = api.getGuildById(guildId);
        long channelId = content.getUnsignedLong("channel_id");
        MessageChannel channel = guild.getTextChannelById(channelId); // TODO: Direct messages
        long interactionId = content.getUnsignedLong("id");
        int type = content.getInt("type");
        String commandToken = content.getString("token");

        DataObject data = content.getObject("data");
        long commandId = data.getUnsignedLong("id");
        String commandName = data.getString("name");
        DataArray options = data.optArray("options").orElseGet(DataArray::empty);

        Member member = api.getEntityBuilder().createMember((GuildImpl) guild, content.getObject("member"));
        api.getEntityBuilder().updateMemberCache((MemberImpl) member);

//        JDAImpl.LOG.info("Sending event");
        api.handleEvent(
            new SlashCommandEvent(api, responseNumber,
                commandToken, interactionId, guild, member,
                member.getUser(), channel, commandName, commandId,
                options.stream(DataArray::getObject).collect(Collectors.toList())));
        return null;
    }
}
