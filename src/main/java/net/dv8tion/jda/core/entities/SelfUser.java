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

import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.managers.AccountManager;

/**
 * Represents the currently logged in account.
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
     * <br><b>NOTE:</b> this is a {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @throws AccountTypeException
     *         If this method is called when {@link net.dv8tion.jda.core.JDA#getAccountType() JDA#getAccountType()} does not return
     *         {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     *
     * @return The email of the currently logged in account.
     */
    String getEmail() throws AccountTypeException;

    /**
     * Shows whether there has ever been a mobile app connected to this account.
     * <br><b>NOTE:</b> this is a {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @throws AccountTypeException
     *         If this method is called when {@link net.dv8tion.jda.core.JDA#getAccountType() JDA#getAccountType()} does not return
     *         {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     *
     * @return {@code true} if the account is linked with a mobile app, otherwise {@code false}
     */
    boolean isMobile() throws AccountTypeException;

    /**
     * The Discord Nitro status of this account.
     * <br><b>NOTE:</b> this is a {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @throws AccountTypeException
     *         If this method is called when {@link net.dv8tion.jda.core.JDA#getAccountType() JDA#getAccountType()} does not return
     *         {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     *
     * @return The Discord Nitro status of the currently logged in account.
     */
    boolean isNitro() throws AccountTypeException;

    /**
     * Used to get the phone number of the currently logged in account if a phone number has been attached to it.
     * <br><b>NOTE:</b> this is a {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @throws AccountTypeException
     *         If this method is called when {@link net.dv8tion.jda.core.JDA#getAccountType() JDA#getAccountType()} does not return
     *         {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     *
     * @return The phone of the currently logged in account or null if there's no number associated
     */
    String getPhoneNumber() throws AccountTypeException;

    /**
     * Returns the maximum size for files that can be uploaded with this account.
     * <br>Returns {@value net.dv8tion.jda.core.entities.Message#MAX_FILE_SIZE} for bots and non-nitro client accounts
     * and {@value net.dv8tion.jda.core.entities.Message#MAX_FILE_SIZE_NITRO} for client accounts with a active nitro subscription.
     * 
     * @return The maximum size for files that can be uploaded with this account
     * 
     * @see net.dv8tion.jda.core.entities.Message#MAX_FILE_SIZE
     * @see net.dv8tion.jda.core.entities.Message#MAX_FILE_SIZE_NITRO
     */
    long getAllowedFileSize();

    /**
     * The {@link net.dv8tion.jda.core.managers.AccountManager AccountManager}
     * for the currently logged in account.
     * <br>This can be used to atomically set account fields (like avatar/username)
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction.queue()}.
     *
     * @return An AccountManager instance for the current account
     */
    AccountManager getManager();
}
