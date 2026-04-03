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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IAgeRestrictedChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IWebhookContainerMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.AudioChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface VoiceChannelMixin<T extends VoiceChannelMixin<T>>
        extends VoiceChannel,
                GuildMessageChannelMixin<T>,
                AudioChannelMixin<T>,
                IWebhookContainerMixin<T>,
                IAgeRestrictedChannelMixin<T>,
                ISlowmodeChannelMixin<T> {
    @Override
    default boolean canTalk(@Nonnull Member member) {
        Checks.notNull(member, "Member");
        return member.hasPermission(this, Permission.MESSAGE_SEND);
    }

    @Nonnull
    @Override
    default ChannelAction<VoiceChannel> createCopy(@Nonnull Guild guild) {
        Checks.notNull(guild, "Guild");

        ChannelAction<VoiceChannel> action =
                guild.createVoiceChannel(getName()).setBitrate(getBitrate()).setUserlimit(getUserLimit());

        if (getRegionRaw() != null) {
            action.setRegion(Region.fromKey(getRegionRaw()));
        }

        if (guild.equals(getGuild())) {
            Category parent = getParentCategory();
            if (parent != null) {
                action.setParent(parent);
            }
            for (PermissionOverride o : getPermissionOverrideMap().valueCollection()) {
                if (o.isMemberOverride()) {
                    action.addMemberPermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                } else {
                    action.addRolePermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                }
            }
        }
        return action;
    }

    @Nonnull
    @Override
    default RestAction<Void> sendSoundboardSound(
            @Nonnull SoundboardSoundSnowflake sound, @Nullable String sourceGuildId) {
        Checks.notNull(sound, "Sound");
        if (sourceGuildId != null) {
            Checks.isSnowflake(sourceGuildId, "Source guild ID");
        }

        // Check speak permissions
        Guild targetGuild = this.getGuild();
        if (!targetGuild.getSelfMember().hasPermission(this, Permission.VOICE_SPEAK)) {
            throw new InsufficientPermissionException(this, Permission.VOICE_SPEAK);
        }
        if (!targetGuild.getSelfMember().hasPermission(this, Permission.VOICE_USE_SOUNDBOARD)) {
            throw new InsufficientPermissionException(this, Permission.VOICE_USE_SOUNDBOARD);
        }

        // Check voice state, self member's voice state should always be cached, but guard just in case
        GuildVoiceState voiceState = targetGuild.getSelfMember().getVoiceState();
        if (voiceState != null) {
            if (!this.equals(voiceState.getChannel())) {
                throw new IllegalStateException(
                        "You must be connected to the voice channel you want to send the sound effect to");
            }
            if (voiceState.isSuppressed()) {
                throw new IllegalStateException("You cannot send sound effects while you are being suppressed");
            }
            if (voiceState.isDeafened()) {
                throw new IllegalStateException("You cannot send sound effects while you are deafened");
            }
            if (voiceState.isMuted()) {
                throw new IllegalStateException("You cannot send sound effects while you are muted");
            }
        }

        // Send
        DataObject data = DataObject.empty().put("sound_id", getId());
        if (sourceGuildId != null) {
            data.put("source_guild_id", sourceGuildId);
        }

        return new RestActionImpl<>(getJDA(), Route.SoundboardSounds.SEND_SOUNDBOARD_SOUND.compile(this.getId()), data);
    }

    T setStatus(String status);
}
