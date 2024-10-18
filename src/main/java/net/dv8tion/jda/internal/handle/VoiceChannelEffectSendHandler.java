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

import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.VoiceChannelEffect;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.channel.VoiceChannelEffectSendEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;

public class VoiceChannelEffectSendHandler extends SocketHandler
{
    public VoiceChannelEffectSendHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long guildId = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);
        if (guild == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        final long channelId = content.getLong("channel_id");
        VoiceChannel channel = guild.getVoiceChannelById(channelId);
        if (channel == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            return null;
        }

        User user = api.getUserById(content.getString("user_id"));
        EmojiUnion emoji = content.optObject("emoji").map(EntityBuilder::createEmoji).orElse(null);
        VoiceChannelEffect.Animation animation = content.opt("animation_type")
                .map(rawAnimationType ->
                {
                    long animationId = content.getLong("animation_id");
                    VoiceChannelEffect.Animation.Type type = VoiceChannelEffect.Animation.Type.fromValue(Integer.parseInt(rawAnimationType.toString()));
                    return new VoiceChannelEffect.Animation(animationId, type);
                })
                .orElse(null);
        SoundboardSound soundboardSound = content.opt("sound_id")
                .map(soundId -> guild.getSoundboardSoundById(soundId.toString()))
                .orElse(null);

        VoiceChannelEffect effect = new VoiceChannelEffect(channel, user, emoji, animation, soundboardSound);

        api.handleEvent(new VoiceChannelEffectSendEvent(api, responseNumber, effect));

        return null;
    }
}
