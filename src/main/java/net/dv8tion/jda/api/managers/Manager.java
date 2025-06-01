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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.managers.ManagerBase;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Top-level abstraction for managers.
 *
 * @param <M> The manager type
 */
public interface Manager<M extends Manager<M>> extends AuditableRestAction<Void>
{
    /**
     * Enables internal checks for missing permissions
     * <br>When this is disabled the chances of hitting a
     * {@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS ErrorResponse.MISSING_PERMISSIONS} is increased significantly,
     * otherwise JDA will check permissions and cancel the execution using
     * {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException}.
     * <br><b>Default: true</b>
     *
     * @param enable
     *        True, if JDA should perform permissions checks internally
     *
     * @see   #isPermissionChecksEnabled()
     */
    static void setPermissionChecksEnabled(boolean enable)
    {
        ManagerBase.setPermissionChecksEnabled(enable);
    }

    /**
     * Whether internal checks for missing permissions are enabled
     * <br>When this is disabled the chances of hitting a
     * {@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS ErrorResponse.MISSING_PERMISSIONS} is increased significantly,
     * otherwise JDA will check permissions and cancel the execution using
     * {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException}.
     *
     * @return True, if internal permission checks are enabled
     *
     * @see    #setPermissionChecksEnabled(boolean)
     */
    static boolean isPermissionChecksEnabled()
    {
        return ManagerBase.isPermissionChecksEnabled();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    M setCheck(@Nonnull BooleanSupplier checks);

    @Nonnull
    @Override
    @CheckReturnValue
    M timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    @CheckReturnValue
    M deadline(long timestamp);

    @Nonnull
    @CheckReturnValue
    M reset(long fields);

    @Nonnull
    @CheckReturnValue
    M reset(long... fields);

    /**
     * Resets all fields for this Manager
     *
     * @return The current Manager with all settings reset to default
     */
    @Nonnull
    @CheckReturnValue
    M reset();
}
