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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Channel with a modifiable voice status.
 * <br>This can be used to indicate what is going on to people outside the channel.
 */
public interface IVoiceStatusChannel extends Channel
{
    /** The maximum length of a voice status {@value} */
    int MAX_STATUS_LENGTH = 500;

    /**
     * The current voice channel status.
     * <br>This can be configured by users who are connected
     * and have the {@link net.dv8tion.jda.api.Permission#VOICE_SET_STATUS set voice channel status} permission.
     *
     * @return The current voice channel status, or empty string if unset
     */
    @Nonnull
    String getStatus();

    /**
     * Change the current voice channel status.
     * <br>This can be configured by users who are connected
     * and have the {@link net.dv8tion.jda.api.Permission#VOICE_SET_STATUS set voice channel status} permission.
     *
     * @param  status
     *         The new status, or empty to unset
     *
     * @throws IllegalArgumentException
     *         If the status is null or longer than {@value #MAX_STATUS_LENGTH} characters
     * @throws net.dv8tion.jda.api.exceptions.MissingAccessException
     *         If the currently logged in account does not have {@link Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} in this channel
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If the currently logged in account is <b>not connected</b> and does not have the {@link Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission.</li>
     *             <li>If the currently logged in account is <b>connected</b> and does not have the {@link Permission#VOICE_SET_STATUS VOICE_SET_STATUS} permission.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return {@link AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> modifyStatus(@Nonnull String status);
}
