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

package net.dv8tion.jda.core.entities;

/**
 * Represents the voice state of a {@link net.dv8tion.jda.core.entities.Member Member} in a
 * {@link net.dv8tion.jda.core.entities.Guild Guild}.
 */
public interface GuildVoiceState extends VoiceState
{
    /**
     * Returns whether the {@link net.dv8tion.jda.core.entities.Member Member} is muted, either
     * by choice {@link #isSelfMuted()} or deafened by an admin {@link #isGuildMuted()}
     *
     * @return the Member's mute status
     */
    boolean isMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.core.entities.Member Member} is deafened, either
     * by choice {@link #isSelfDeafened()} or deafened by an admin {@link #isGuildDeafened()}
     *
     * @return the Member's deaf status
     */
    boolean isDeafened();

    /**
     * Returns whether the {@link net.dv8tion.jda.core.entities.Member Member} got muted by an Admin
     *
     * @return the Member's guild-mute status
     */
    boolean isGuildMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.core.entities.Member Member} got deafened by an Admin
     *
     * @return the Member's guild-deaf status
     */
    boolean isGuildDeafened();

    /**
     * Returns true if this {@link net.dv8tion.jda.core.entities.Member Member} is unable to speak because the
     * channel is actively suppressing audio communication. This occurs only in
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} where the Member either doesn't have
     * {@link net.dv8tion.jda.core.Permission#VOICE_SPEAK Permission#VOICE_SPEAK} or if the channel is the
     * designated AFK channel.
     *
     * @return True, if this {@link net.dv8tion.jda.core.entities.Member Member's} audio is being suppressed.
     */
    boolean isSuppressed();

    /**
     * Returns the current {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that the {@link net.dv8tion.jda.core.entities.Member Member}
     * is in. If the {@link net.dv8tion.jda.core.entities.Member Member} is currently not in a
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}, this returns null.
     *
     * @return The VoiceChannel that the Member is in, or null.
     */
    VoiceChannel getChannel();

    /**
     * Returns the current {@link net.dv8tion.jda.core.entities.Guild Guild} of the {@link net.dv8tion.jda.core.entities.Member Member's}
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}. If the {@link net.dv8tion.jda.core.entities.Member Member} is currently
     * not in a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}, this returns null
     *
     * @return the Member's Guild
     */
    Guild getGuild();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Member Member} corresponding to this GuildVoiceState instance
     * (Backreference)
     *
     * @return the Member that holds this GuildVoiceState
     */
    Member getMember();

    /**
     * Used to determine if the {@link net.dv8tion.jda.core.entities.Member Member} is currently in a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * in the {@link net.dv8tion.jda.core.entities.Guild Guild} returned from {@link #getGuild() getGuild()}.<br>
     * If this is {@code false}, {@link #getChannel() getChannel()} will return {@code null}.
     *
     * @return True, if the {@link net.dv8tion.jda.core.entities.Member Member} is currently in a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     *         in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     */
    boolean inVoiceChannel();
}
