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

package net.dv8tion.jda.test.events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.VoiceChannelEffectSendEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.channel.concrete.VoiceChannelImpl;
import net.dv8tion.jda.internal.handle.VoiceChannelEffectSendHandler;
import net.dv8tion.jda.test.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class VoiceChannelEffectSendHandlerTest extends AbstractSocketHandlerTest {
    @Mock
    protected VoiceChannelImpl voiceChannel;

    @Mock
    protected Member member;

    @BeforeEach
    final void setupVoiceContext() {
        when(guild.getGuildChannelById(eq(Constants.CHANNEL_ID))).thenReturn(voiceChannel);
        when(guild.getMemberById(eq(Constants.MINN_USER_ID))).thenReturn(member);
        when(member.getGuild()).thenReturn(guild);
    }

    @Test
    void testSoundboardEffect() {
        VoiceChannelEffectSendHandler handler = new VoiceChannelEffectSendHandler(jda);

        assertThatEvent(VoiceChannelEffectSendEvent.class)
                .hasGetterWithValueEqualTo(VoiceChannelEffectSendEvent::isSoundboard, true)
                .hasGetterWithValueEqualTo(VoiceChannelEffectSendEvent::getSoundId, 1106714396018884649L)
                .hasGetterWithValueEqualTo(VoiceChannelEffectSendEvent::getSoundVolume, 1.0)
                .hasGetterWithValueEqualTo(VoiceChannelEffectSendEvent::getMember, member)
                .isFiredBy(() -> {
                    handler.handle(
                            random.nextLong(),
                            event(
                                    "VOICE_CHANNEL_EFFECT_SEND",
                                    DataObject.empty()
                                            .put("guild_id", Constants.GUILD_ID)
                                            .put("channel_id", Constants.CHANNEL_ID)
                                            .put("user_id", Constants.MINN_USER_ID)
                                            .put("sound_id", 1106714396018884649L)
                                            .put("sound_volume", 1.0)));
                });
    }

    @Test
    void testEmojiReactionEffect() {
        VoiceChannelEffectSendHandler handler = new VoiceChannelEffectSendHandler(jda);

        assertThatEvent(VoiceChannelEffectSendEvent.class)
                .hasGetterWithValueEqualTo(VoiceChannelEffectSendEvent::isSoundboard, false)
                .hasGetterWithValueEqualTo(VoiceChannelEffectSendEvent::getSoundId, 0L)
                .hasGetterWithValueEqualTo(VoiceChannelEffectSendEvent::getMember, member)
                .isFiredBy(() -> {
                    handler.handle(
                            random.nextLong(),
                            event(
                                    "VOICE_CHANNEL_EFFECT_SEND",
                                    DataObject.empty()
                                            .put("guild_id", Constants.GUILD_ID)
                                            .put("channel_id", Constants.CHANNEL_ID)
                                            .put("user_id", Constants.MINN_USER_ID)
                                            .put(
                                                    "emoji",
                                                    DataObject.empty()
                                                            .put("name", "\uD83D\uDE02")
                                                            .putNull("id"))
                                            .put("animation_type", 1)
                                            .put("animation_id", 12345)));
                });
    }

    @Test
    void testUnknownMemberDropsEvent() {
        VoiceChannelEffectSendHandler handler = new VoiceChannelEffectSendHandler(jda);

        // member not cached for this user
        when(guild.getMemberById(eq(Constants.BUTLER_USER_ID))).thenReturn(null);

        handler.handle(
                random.nextLong(),
                event(
                        "VOICE_CHANNEL_EFFECT_SEND",
                        DataObject.empty()
                                .put("guild_id", Constants.GUILD_ID)
                                .put("channel_id", Constants.CHANNEL_ID)
                                .put("user_id", Constants.BUTLER_USER_ID)
                                .put("sound_id", 1)));

        verify(jda, never()).handleEvent(any());
    }
}
