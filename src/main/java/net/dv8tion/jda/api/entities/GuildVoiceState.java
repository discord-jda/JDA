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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the voice state of a {@link net.dv8tion.jda.api.entities.Member Member} in a
 * {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * @see Member#getVoiceState()
 */
public interface GuildVoiceState
{
    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this VoiceState
     *
     * @return The corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} muted themselves.
     *
     * @return The User's self-mute status
     */
    boolean isSelfMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} deafened themselves.
     *
     * @return The User's self-deaf status
     */
    boolean isSelfDeafened();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} is muted, either
     * by choice {@link #isSelfMuted()} or muted by an admin {@link #isGuildMuted()}
     *
     * @return the Member's mute status
     */
    boolean isMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} is deafened, either
     * by choice {@link #isSelfDeafened()} or deafened by an admin {@link #isGuildDeafened()}
     *
     * @return the Member's deaf status
     */
    boolean isDeafened();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} got muted by an Admin
     *
     * @return the Member's guild-mute status
     */
    boolean isGuildMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} got deafened by an Admin
     *
     * @return the Member's guild-deaf status
     */
    boolean isGuildDeafened();

    /**
     * Returns true if this {@link net.dv8tion.jda.api.entities.Member Member} is unable to speak because the
     * channel is actively suppressing audio communication. This occurs only in
     * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels} where the Member either doesn't have
     * {@link net.dv8tion.jda.api.Permission#VOICE_SPEAK Permission#VOICE_SPEAK} or if the channel is the
     * designated AFK channel.
     *
     * @return True, if this {@link net.dv8tion.jda.api.entities.Member Member's} audio is being suppressed.
     */
    boolean isSuppressed();

    /**
     * Returns true if this {@link net.dv8tion.jda.api.entities.Member Member} is currently streaming with Go Live.
     *
     * @return True, if this member is streaming
     */
    boolean isStream();

    /**
     * Returns the current {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} that the {@link net.dv8tion.jda.api.entities.Member Member}
     * is in. If the {@link net.dv8tion.jda.api.entities.Member Member} is currently not in a
     * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}, this returns null.
     *
     * @return The VoiceChannel that the Member is in, or null.
     */
    @Nullable
    VoiceChannel getChannel();

    /**
     * Returns the current {@link net.dv8tion.jda.api.entities.Guild Guild} of the {@link net.dv8tion.jda.api.entities.Member Member's}
     * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}. If the {@link net.dv8tion.jda.api.entities.Member Member} is currently
     * not in a {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}, this returns null
     *
     * @return the Member's Guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Member Member} corresponding to this GuildVoiceState instance
     * (Backreference)
     *
     * @return the Member that holds this GuildVoiceState
     */
    @Nonnull
    Member getMember();

    /**
     * Used to determine if the {@link net.dv8tion.jda.api.entities.Member Member} is currently in a {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     * in the {@link net.dv8tion.jda.api.entities.Guild Guild} returned from {@link #getGuild() getGuild()}.<br>
     * If this is {@code false}, {@link #getChannel() getChannel()} will return {@code null}.
     *
     * @return True, if the {@link net.dv8tion.jda.api.entities.Member Member} is currently in a {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     *         in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     */
    boolean inVoiceChannel();

    /**
     * The Session-Id for this VoiceState
     *
     * @return The Session-Id
     */
    @Nullable
    String getSessionId();
}
