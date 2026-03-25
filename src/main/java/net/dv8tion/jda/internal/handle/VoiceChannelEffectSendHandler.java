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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.guild.voice.VoiceChannelEffectSendEvent;
import net.dv8tion.jda.api.events.guild.voice.VoiceChannelEffectSendEvent.AnimationType;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class VoiceChannelEffectSendHandler extends SocketHandler {
    public VoiceChannelEffectSendHandler(JDAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getUnsignedLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }

        Guild guild = getJDA().getGuildById(guildId);
        if (guild == null) {
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Caching VOICE_CHANNEL_EFFECT_SEND for uncached guild. ID: {}", guildId);
            return null;
        }

        long channelId = content.getUnsignedLong("channel_id");
        GuildChannel guildChannel = guild.getGuildChannelById(channelId);
        if (guildChannel == null) {
            getJDA().getEventCache()
                    .cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Caching VOICE_CHANNEL_EFFECT_SEND for uncached channel. ID: {}", channelId);
            return null;
        }
        if (!(guildChannel instanceof AudioChannelUnion)) {
            WebSocketClient.LOG.debug("Dropping VOICE_CHANNEL_EFFECT_SEND for non-audio channel. ID: {}", channelId);
            return null;
        }
        AudioChannelUnion channel = (AudioChannelUnion) guildChannel;

        long userId = content.getUnsignedLong("user_id");
        Member member = guild.getMemberById(userId);
        if (member == null) {
            WebSocketClient.LOG.debug("Dropping VOICE_CHANNEL_EFFECT_SEND for unknown member. User ID: {}", userId);
            return null;
        }

        EmojiUnion emoji =
                content.optObject("emoji").map(EntityBuilder::createEmoji).orElse(null);

        AnimationType animationType =
                content.isNull("animation_type") ? null : AnimationType.fromKey(content.getInt("animation_type"));

        long animationId = content.getLong("animation_id", 0);
        long soundId = content.getUnsignedLong("sound_id", 0);
        double soundVolume = content.getDouble("sound_volume", 0);

        api.handleEvent(new VoiceChannelEffectSendEvent(
                api, responseNumber, member, channel, emoji, animationType, animationId, soundId, soundVolume));
        return null;
    }
}
