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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * Facade for an {@link net.dv8tion.jda.core.managers.AccountManagerUpdatable AccountManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class AccountManager
{
    protected final AccountManagerUpdatable updatable;

    /**
     * Creates a new AccountManager instance
     *
     * @param selfUser
     *        The {@link net.dv8tion.jda.core.entities.SelfUser SelfUser} to manage
     */
    public AccountManager(SelfUser selfUser)
    {
        this.updatable = new AccountManagerUpdatable(selfUser);
    }


    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this AccountManager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return updatable.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.SelfUser SelfUser} that will be
     * modified by this AccountManager.
     * <br>This represents the currently logged in account.
     *
     * @return The corresponding SelfUser
     */
    public SelfUser getSelfUser()
    {
        return updatable.getSelfUser();
    }

    /**
     * Sets the username for the currently logged in account
     * <br>More information can be found {@link AccountManagerUpdatable#getNameField() here}!
     *
     * <p><b>Client-Accounts ({@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}) require the
     * current password to be updated. See {@link #setName(String, String)}</b>
     *
     * @param  name
     *         The new username
     *
     * @throws IllegalArgumentException
     *         If the provided name is:
     *         <ul>
     *             <li>Equal to {@code null}</li>
     *             <li>Less than {@code 2} or more than {@code 32} characters in length</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         The update RestAction that will set the provided name.
     *         <br>See {@link net.dv8tion.jda.core.managers.AccountManagerUpdatable#update(String) #update()} for more information
     */
    public RestAction<Void> setName(String name)
    {
        return setName(name, null);
    }

    /**
     * Sets the username for the currently logged in account
     * <br>More information can be found {@link AccountManagerUpdatable#getNameField() here}!
     *
     * @param  name
     *         The new username
     * @param  currentPassword
     *         The current password for the represented account,
     *         this is only required for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     *
     * @throws IllegalArgumentException
     *         If this is action is performed on an account with the type {@link net.dv8tion.jda.core.AccountType#CLIENT CLIENT}
     *         and the provided password is {@code null} or empty
     *         <br>If the provided name is:
     *         <ul>
     *             <li>Equal to {@code null}</li>
     *             <li>Less than 2 or more than 32 characters in length</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         The update RestAction that will set the provided name.
     *         <br>See {@link net.dv8tion.jda.core.managers.AccountManagerUpdatable#update(String) #update()} for more information
     */
    public RestAction<Void> setName(String name, String currentPassword)
    {
        return updatable.getNameField().setValue(name).update(currentPassword);
    }

    /**
     * Sets the avatar for the currently logged in account
     * <br>More information can be found {@link AccountManagerUpdatable#getAvatarField() here}!
     *
     * <p><b>Client-Accounts ({@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}) require the
     * current password to be updated. See {@link #setAvatar(net.dv8tion.jda.core.entities.Icon, String) #setAvatar(Icon, String)}</b>
     *
     * @param  avatar
     *         An {@link net.dv8tion.jda.core.entities.Icon Icon} instance representing
     *         the new Avatar for the current account, {@code null} to reset the avatar to the default avatar.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         The update RestAction that will set the provided icon as the new avatar or reset the avatar.
     *         <br>See {@link net.dv8tion.jda.core.managers.AccountManagerUpdatable#update(String) #update()} for more information
     */
    public RestAction<Void> setAvatar(Icon avatar)
    {
        return setAvatar(avatar, null);
    }

    /**
     * Sets the avatar for the currently logged in account
     * <br>More information can be found {@link AccountManagerUpdatable#getAvatarField() here}!
     *
     * @param  avatar
     *         An {@link net.dv8tion.jda.core.entities.Icon Icon} instance representing
     *         the new Avatar for the current account, {@code null} to reset the avatar to the default avatar.
     * @param  currentPassword
     *         The current password for the represented account,
     *         this is only required for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     *
     * @throws IllegalArgumentException
     *         If the provided {@code currentPassword} is {@code null} or empty and the currently
     *         logged in account is from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         The update RestAction that will set the provided icon as the new avatar or reset the avatar.
     *         <br>See {@link net.dv8tion.jda.core.managers.AccountManagerUpdatable#update(String) #update()} for more information
     */
    public RestAction<Void> setAvatar(Icon avatar, String currentPassword)
    {
        return updatable.getAvatarField().setValue(avatar).update(currentPassword);
    }

    /**
     * Sets the email for the currently logged in client account.
     * <br>More information can be found {@link AccountManagerUpdatable#getEmailField() here}!
     *
     * @param  email
     *         The new email
     * @param  currentPassword
     *         The <b>valid</b> current password for the represented account
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT}
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code currentPassword} or the provided {@code email} is {@code null} or empty
     *             <li>If the provided {@code email} is not valid.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         The update RestAction that will set the provided email.
     *         <br>See {@link net.dv8tion.jda.core.managers.AccountManagerUpdatable#update(String) #update()} for more information
     */
    public RestAction<Void> setEmail(String email, String currentPassword)
    {
        return updatable.getEmailField().setValue(email).update(currentPassword);
    }

    /**
     * Sets the password for the currently logged in client account.
     * <br>If the new password is equal to the current password this does nothing.
     * <br>More information can be found {@link AccountManagerUpdatable#getPasswordField() here}!
     *
     * @param  newPassword
     *         The new password for the currently logged in account
     * @param  currentPassword
     *         The <b>valid</b> current password for the represented account
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT}
     * @throws IllegalArgumentException
     *         If any of the provided passwords are {@code null} or empty
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         The update RestAction that will set the provided email.
     *         <br>See {@link net.dv8tion.jda.core.managers.AccountManagerUpdatable#update(String) #update()} for more information
     */
    public RestAction<Void> setPassword(String newPassword, String currentPassword)
    {
        return updatable.getPasswordField().setValue(newPassword).update(currentPassword);
    }
}
