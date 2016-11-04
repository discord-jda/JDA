/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.exceptions.AccountTypeException;

/**
 * Represents the currently logged in account.
 */
public interface SelfUser extends User
{

    /**
     * The status of this account's verification.<br>
     * (Have you accepted the verification email)
     *
     * @return
     *      boolean specifying whether or not this account is verified.
     */
    boolean isVerified();

    /**
     * If true, this account is protected by Multi-Factor authorizaiton.<br>
     * If this is a Client account, then this describes the MFA status of the Client account.<br>
     * If this is a Bot account, then this describes the MFA status of the Client account that owns this Bot.
     *
     * @return
     *      boolean specifying whether or not this account has MFA protecting it.
     */
    boolean isMfaEnabled();

    /**
     * Used to get the email of the currently logged in account.<br>
     * <b>NOTE:</b> this is a {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT} method only!
     *
     * @return
     *      The email of the currently logged in account.
     * @throws AccountTypeException
     *      If this method is called when {@link net.dv8tion.jda.core.JDA#getAccountType() JDA#getAccountType()} does not return
     *      {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}. E.g: If the logged in account isn't a Client account!
     */
    String getEmail() throws AccountTypeException;

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
