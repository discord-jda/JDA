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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.entities;

import net.dv8tion.jda.Permission;

/**
 * Represents the currently logged in account.
 */
public interface SelfInfo extends User
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
     * Creates a OAuth invite-link used to invite bot-accounts.<br>
     * This is literally just a shortcut to
     * {@link net.dv8tion.jda.utils.ApplicationUtil#getAuthInvite(net.dv8tion.jda.JDA, net.dv8tion.jda.Permission...) ApplicationUtil.getAuthInvite(JDA, Permission...)}
     *
     * @param perms
     *      Possibly empty list of Permissions that should be requested via invite
     * @return
     *      The link used to invite the bot
     */
    String getAuthUrl(Permission... perms);
}
