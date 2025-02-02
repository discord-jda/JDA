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

package net.dv8tion.jda.internal.entities.channel.mixin.concrete;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IAgeRestrictedChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IWebhookContainerMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.AudioChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public interface VoiceChannelMixin<T extends VoiceChannelMixin<T>>
        extends VoiceChannel,
        GuildMessageChannelMixin<T>,
        AudioChannelMixin<T>,
        IWebhookContainerMixin<T>,
        IAgeRestrictedChannelMixin<T>,
        ISlowmodeChannelMixin<T>
{
    @Override
    default boolean canTalk(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return member.hasPermission(this, Permission.MESSAGE_SEND);
    }

    @Nonnull
    @Override
    default ChannelAction<VoiceChannel> createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");

        ChannelAction<VoiceChannel> action = guild.createVoiceChannel(getName())
                .setBitrate(getBitrate())
                .setUserlimit(getUserLimit());

        if (getRegionRaw() != null)
            action.setRegion(Region.fromKey(getRegionRaw()));

        if (guild.equals(getGuild()))
        {
            Category parent = getParentCategory();
            if (parent != null)
                action.setParent(parent);
            for (PermissionOverride o : getPermissionOverrideMap().valueCollection())
            {
                if (o.isMemberOverride())
                    action.addMemberPermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                else
                    action.addRolePermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
            }
        }
        return action;
    }

    T setStatus(String status);
}
