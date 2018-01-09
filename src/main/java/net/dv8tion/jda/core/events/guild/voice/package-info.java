/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Events indicating the {@link net.dv8tion.jda.core.entities.GuildVoiceState GuildVoiceState} updates
 * for one of the {@link net.dv8tion.jda.core.entities.Guild Guild}'s {@link net.dv8tion.jda.core.entities.Member Members}.
 * <br>Every update is an extensions of the {@link net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent GenericGuildVoiceEvent}
 * and has specifications for explicit voice state features such as mute/deafen
 */
package net.dv8tion.jda.core.events.guild.voice;
