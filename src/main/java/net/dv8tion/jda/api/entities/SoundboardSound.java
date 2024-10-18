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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.SoundboardSoundManager;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a soundboard sound, can be used in voice channels if they are {@link #isAvailable() available}.
 *
 * @see JDA#retrieveDefaultSoundboardSounds()
 * @see Guild#getSoundboardSounds()
 * @see <a href="https://discord.com/developers/docs/resources/soundboard" target="_blank">Discord's Soundboard Docs</a>
 */
public interface SoundboardSound extends ISnowflake
{
    /** Template for {@link #getUrl()}.*/
    String SOUND_URL = "https://cdn.discordapp.com/soundboard-sounds/%s";

    /**
     * Returns the {@link JDA} instance related to this SoundboardSound.
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Returns the URL to the sound asset.
     *
     * @return A String representing this sound's asset
     */
    @Nonnull
    default String getUrl()
    {
        return String.format(SOUND_URL, getId());
    }

    /**
     * The name of this sound.
     *
     * @return The name of this sound
     */
    @Nonnull
    String getName();

    /**
     * The volume of this sound, from 0 to 1.
     *
     * @return The volume of this sound, from 0 to 1
     */
    double getVolume();

    /**
     * The emoji of this sound, or {@code null} if none is set.
     *
     * @return The emoji of this sound, or {@code null}
     */
    @Nullable
    EmojiUnion getEmoji();

    /**
     * The guild this sound is from.
     *
     * @return The guild this sound is from, or {@code null} if this is a default soundboard sound
     */
    @Nullable
    Guild getGuild();

    /**
     * Whether this sound can be used, may be {@code false} due to loss of server boosts.
     *
     * @return {@code true} if the sound can be used, {@code false} otherwise
     */
    boolean isAvailable();

    /**
     * The user which created this sound.
     *
     * <p>This is present only if the {@link Guild#getSelfMember() self member} has
     * the {@link Permission#CREATE_GUILD_EXPRESSIONS CREATE_GUILD_EXPRESSIONS}
     * or {@link Permission#MANAGE_GUILD_EXPRESSIONS MANAGE_GUILD_EXPRESSIONS} permission.
     *
     * @return The user which created this sound, or {@code null}
     */
    @Nullable
    User getUser();

    /**
     * Sends this sound to the specified voice channel.
     * <br>You must be connected to the voice channel to use this method,
     * as well as be able to speak and hear.
     *
     * <p>The returned {@link RestAction} can encounter the following {@link ErrorResponse ErrorResponses}:
     * <ul>
     *     <li>{@link ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The sound cannot be sent due to a permission discrepancy</li>
     *     <li>{@link ErrorResponse#CANNOT_SEND_VOICE_EFFECT CANNOT_SEND_VOICE_EFFECT}
     *     <br>The sound cannot be sent as the bot is either muted, deafened or suppressed</li>
     *     <li>{@link ErrorResponse#UNKNOWN_SOUND UNKNOWN_SOUND}
     *     <br>The sound was deleted</li>
     * </ul>
     *
     * @param  channel
     *         The channel to send this sound on
     *
     * @throws InsufficientPermissionException
     *         If the bot does not have the following permissions:
     *         <ul>
     *             <li>{@link Permission#VOICE_SPEAK VOICE_SPEAK}, {@link Permission#VOICE_USE_SOUNDBOARD VOICE_USE_SOUNDBOARD}</li>
     *             <li>When used in other guilds, {@link Permission#VOICE_USE_EXTERNAL_SOUNDS VOICE_USE_EXTERNAL_SOUNDS} permission</li>
     *         </ul>
     * @throws IllegalArgumentException
     *         If the provided channel is {@code null}
     * @throws IllegalStateException
     *         <ul>
     *             <li>If {@link GatewayIntent#GUILD_VOICE_STATES} is not enabled</li>
     *             <li>If the sound is not {@link #isAvailable() available}</li>
     *             <li>If the bot is not connected to the specified channel</li>
     *             <li>If the bot is deafened, muted or suppressed in the target guild</li>
     *         </ul>
     *
     * @return {@link RestAction} - Type: {@link Void}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> sendTo(VoiceChannel channel);

    /**
     * Deletes this soundboard sound.
     *
     * <p>The returned {@link RestAction} can encounter the following {@link ErrorResponse ErrorResponses}:
     * <ul>
     *     <li>{@link ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The sound cannot be deleted due to a permission discrepancy</li>
     *     <li>{@link ErrorResponse#UNKNOWN_SOUND UNKNOWN_SOUND}
     *     <br>The sound was deleted</li>
     * </ul>
     *
     * @throws InsufficientPermissionException
     *         <ul>
     *             <li>If the bot does not own this sound and does not have {@link Permission#MANAGE_GUILD_EXPRESSIONS}</li>
     *             <li>If the bot owns this sound and does not have {@link Permission#MANAGE_GUILD_EXPRESSIONS} or {@link Permission#CREATE_GUILD_EXPRESSIONS}</li>
     *         </ul>
     *
     * @return {@link RestAction} - Type: {@link Void}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> delete();

    /**
     * The {@link SoundboardSoundManager} for this soundboard sound, in which you can modify values.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link RestAction#queue()}.
     *
     * <p>The returned {@link RestAction} can encounter the following {@link ErrorResponse ErrorResponses}:
     * <ul>
     *     <li>{@link ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The sound cannot be edited due to a permission discrepancy</li>
     *     <li>{@link ErrorResponse#UNKNOWN_SOUND UNKNOWN_SOUND}
     *     <br>The sound was deleted</li>
     * </ul>
     *
     * @throws InsufficientPermissionException
     *         <ul>
     *             <li>If the bot does not own this sound and does not have {@link Permission#MANAGE_GUILD_EXPRESSIONS}</li>
     *             <li>If the bot owns this sound and does not have {@link Permission#MANAGE_GUILD_EXPRESSIONS} or {@link Permission#CREATE_GUILD_EXPRESSIONS}</li>
     *         </ul>
     *
     * @return The SoundboardSoundManager of this soundboard sound
     */
    @Nonnull
    @CheckReturnValue
    SoundboardSoundManager getManager();
}
