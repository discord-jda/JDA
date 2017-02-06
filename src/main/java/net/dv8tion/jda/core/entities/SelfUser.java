/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.core.managers.AccountManagerUpdatable;

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
     * The {@link net.dv8tion.jda.core.managers.AccountManager AccountManager}
     * for the currently logged in account.
     *
     * <p>This can be used to atomically set account fields (like avatar/username)
     *
     * @return An AccountManager instance for the current account
     */
    AccountManager getManager();

    /**
     * The {@link net.dv8tion.jda.core.managers.AccountManagerUpdatable AccountManagerUpdatable}
     * for the currently logged in account.
     *
     * <p>This can be used to bulk update account fields (like avatar/username)
     *
     * @return An AccountManagerUpdatable instance for the current account
     */
    AccountManagerUpdatable getManagerUpdatable();

//    /**
//     * Creates a OAuth invite-link used to invite bot-accounts.<br>
//     * This is literally just a shortcut to
//     * {@link net.dv8tion.jda.utils.ApplicationUtil#getAuthInvite(net.dv8tion.jda.JDA, net.dv8tion.jda.Permission...) ApplicationUtil.getAuthInvite(JDA, Permission...)}
//     *
//     * @param perms
//     *      Possibly empty list of Permissions that should be requested via invite
//     * @return
//     *      The link used to invite the bot
//     */
//    String getAuthUrl(Permission... perms);
}
