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
package net.dv8tion.jda.api.managers

import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.internal.managers.ManagerBase
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Top-level abstraction for managers.
 *
 * @param <M> The manager type
</M> */
interface Manager<M : Manager<M>?> : AuditableRestAction<Void?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): AuditableRestAction<Void?>
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): AuditableRestAction<Void?>
    @Nonnull
    override fun deadline(timestamp: Long): AuditableRestAction<Void?>

    @Nonnull
    @CheckReturnValue
    fun reset(fields: Long): M

    @Nonnull
    @CheckReturnValue
    fun reset(vararg fields: Long): M

    /**
     * Resets all fields for this Manager
     *
     * @return The current Manager with all settings reset to default
     */
    @Nonnull
    @CheckReturnValue
    fun reset(): M

    companion object {
        var isPermissionChecksEnabled: Boolean
            /**
             * Whether internal checks for missing permissions are enabled
             * <br></br>When this is disabled the chances of hitting a
             * [ErrorResponse.MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS] is increased significantly,
             * otherwise JDA will check permissions and cancel the execution using
             * [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException].
             *
             * @return True, if internal permission checks are enabled
             *
             * @see .setPermissionChecksEnabled
             */
            get() = ManagerBase.isPermissionChecksEnabled()
            /**
             * Enables internal checks for missing permissions
             * <br></br>When this is disabled the chances of hitting a
             * [ErrorResponse.MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS] is increased significantly,
             * otherwise JDA will check permissions and cancel the execution using
             * [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException].
             * <br></br>**Default: true**
             *
             * @param enable
             * True, if JDA should perform permissions checks internally
             *
             * @see .isPermissionChecksEnabled
             */
            set(enable) {
                ManagerBase.setPermissionChecksEnabled(enable)
            }
    }
}
