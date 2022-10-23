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
import net.dv8tion.jda.api.managers.AccountManager;

import javax.annotation.Nonnull;

/**
 * Represents the currently logged in account.
 *
 * @see JDA#getSelfUser()
 */
public interface SelfUser extends User
{
    /**
     * The associated application id for the bot account.
     * <br>For most bots this is identical to the user id.
     *
     * @return The application id
     */
    long getApplicationIdLong();

    /**
     * The associated application id for the bot account.
     * <br>For most bots this is identical to the user id.
     *
     * @return The application id
     */
    @Nonnull
    default String getApplicationId()
    {
        return Long.toUnsignedString(getApplicationIdLong());
    }


    /**
     * The status of this account's verification.
     * (Have you accepted the verification email)
     *
     * @return True, if this account is verified.
     */
    boolean isVerified();

    /**
     * If true, this account is protected by Multi-Factor authorization.
     * <br>If this is a Client account, then this describes the MFA status of the Client account.
     * <br>If this is a Bot account, then this describes the MFA status of the Client account that owns this Bot.
     *
     * @return True, if this account has MFA protecting it.
     */
    boolean isMfaEnabled();

    /**
     * Returns the maximum size for files that can be uploaded with this account.
     * <br>Returns {@value net.dv8tion.jda.api.entities.Message#MAX_FILE_SIZE} for bots.
     * 
     * @return The maximum size for files that can be uploaded with this account
     * 
     * @see net.dv8tion.jda.api.entities.Message#MAX_FILE_SIZE
     */
    long getAllowedFileSize();

    /**
     * The {@link AccountManager AccountManager}
     * for the currently logged in account.
     * <br>This can be used to atomically set account fields (like avatar/username)
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @return An AccountManager instance for the current account
     */
    @Nonnull
    AccountManager getManager();
}
