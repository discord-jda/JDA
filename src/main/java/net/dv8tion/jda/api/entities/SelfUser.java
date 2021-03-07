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

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.AccountTypeException;
import net.dv8tion.jda.api.managers.AccountManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the currently logged in account.
 *
 * @see JDA#getSelfUser()
 */
public interface SelfUser extends User
{

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
     * Used to get the email of the currently logged in account.
     * <br><b>NOTE:</b> this is a {@link net.dv8tion.jda.api.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @throws AccountTypeException
     *         If this method is called when {@link net.dv8tion.jda.api.JDA#getAccountType() JDA#getAccountType()} does not return
     *         {@link net.dv8tion.jda.api.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     *
     * @return The email of the currently logged in account.
     *
     * @deprecated This is no longer supported
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    default String getEmail()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Shows whether there has ever been a mobile app connected to this account.
     * <br><b>NOTE:</b> this is a {@link net.dv8tion.jda.api.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @throws AccountTypeException
     *         If this method is called when {@link net.dv8tion.jda.api.JDA#getAccountType() JDA#getAccountType()} does not return
     *         {@link net.dv8tion.jda.api.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     *
     * @return {@code true} if the account is linked with a mobile app, otherwise {@code false}
     *
     * @deprecated This is no longer supported
     */
    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    default boolean isMobile()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * The Discord Nitro status of this account.
     * <br><b>NOTE:</b> this is a {@link net.dv8tion.jda.api.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @throws AccountTypeException
     *         If this method is called when {@link net.dv8tion.jda.api.JDA#getAccountType() JDA#getAccountType()} does not return
     *         {@link net.dv8tion.jda.api.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     *
     * @return The Discord Nitro status of the currently logged in account.
     *
     * @deprecated This is no longer supported
     */
    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    default boolean isNitro()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Used to get the phone number of the currently logged in account if a phone number has been attached to it.
     * <br><b>NOTE:</b> this is a {@link net.dv8tion.jda.api.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @throws AccountTypeException
     *         If this method is called when {@link net.dv8tion.jda.api.JDA#getAccountType() JDA#getAccountType()} does not return
     *         {@link net.dv8tion.jda.api.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     *
     * @return The phone of the currently logged in account or null if there's no number associated
     *
     * @deprecated This is no longer supported
     */
    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    @Nullable
    default String getPhoneNumber()
    {
        throw new UnsupportedOperationException();
    }

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
