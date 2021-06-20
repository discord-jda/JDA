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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Stage Channel, also known as Radio Channel.
 *
 * <p>This is a more advanced version of a {@link VoiceChannel}
 * that can be used to host events with speakers and listeners.
 */
public interface StageChannel extends VoiceChannel
{
    @Nullable
    StageInstance getStageInstance();

    @Nonnull
    @CheckReturnValue
    StageInstanceAction createStageInstance(@Nonnull String topic);

    // TODO: createStageInstance() aka "go live"

    /**
     * Whether this member is considered a moderator for this stage channel.
     * <br>Moderators can modify the {@link #getStageInstance() Stage Instance} and promote speakers.
     *
     * <p>A member is considered a stage moderator if they have these permissions:
     * <ul>
     *     <li>{@link Permission#MANAGE_CHANNEL}</li>
     *     <li>{@link Permission#VOICE_MUTE_OTHERS}</li>
     *     <li>{@link Permission#VOICE_MOVE_OTHERS}</li>
     * </ul>
     *
     * @param  member
     *         The member to check
     *
     * @throws IllegalArgumentException
     *         If the provided member is null or not from this guild
     *
     * @return True, if the provided member is a stage moderator
     */
    default boolean isModerator(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return member.hasPermission(this, Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS);
    }
}
