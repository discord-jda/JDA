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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;

/**
 * Manager providing functionality to update one or more fields for the logged in account.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("Minn")
 *        .setAvatar(null)
 *        .queue();
 * manager.reset(AccountManager.NAME | AccountManager.AVATAR)
 *        .setName("DV8FromTheWorld")
 *        .setAvatar(icon)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.core.JDA#getSelfUser() JDA.getSelfUser()
 * @see net.dv8tion.jda.core.entities.SelfUser#getManager()
 */
public class AccountManager extends ManagerBase
{
    /** Used to reset the name field */
    public static final long NAME = 0x1;
    /** Used to reset the avatar field */
    public static final long AVATAR = 0x2;
    /** Used to reset the email field */
    public static final long EMAIL = 0x4;
    /** Used to reset the password field */
    public static final long PASSWORD = 0x8;

    protected final SelfUser selfUser;

    protected String currentPassword;

    protected String name;
    protected Icon avatar;
    protected String email;
    protected String password;

    /**
     * Creates a new AccountManager instance
     *
     * @param selfUser
     *        The {@link net.dv8tion.jda.core.entities.SelfUser SelfUser} to manage
     */
    public AccountManager(SelfUser selfUser)
    {
        super(selfUser.getJDA(), Route.Self.MODIFY_SELF.compile());
        this.selfUser = selfUser;
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
        return selfUser;
    }

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(AccountManager.NAME | AccountManager.AVATAR);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #AVATAR}</li>
     *     <li>{@link #EMAIL}</li>
     *     <li>{@link #PASSWORD}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return AccountManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public AccountManager reset(long fields)
    {
        super.reset(fields);
        if ((fields & AVATAR) == AVATAR)
            avatar = null;
        return this;
    }

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(AccountManager.NAME, AccountManager.AVATAR);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #AVATAR}</li>
     *     <li>{@link #EMAIL}</li>
     *     <li>{@link #PASSWORD}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return AccountManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public AccountManager reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    /**
     * Resets all fields for this manager.
     *
     * @return AccountManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public AccountManager reset()
    {
        super.reset();
        avatar = null;
        return this;
    }

    /**
     * Sets the username for the currently logged in account
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
     * @return AccountManager for chaining convenience
     */
    @CheckReturnValue
    public AccountManager setName(String name)
    {
        return setName(name, null);
    }

    /**
     * Sets the username for the currently logged in account
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
     * @return AccountManager for chaining convenience
     */
    @CheckReturnValue
    public AccountManager setName(String name, String currentPassword)
    {
        Checks.notBlank(name, "Name");
        Checks.check(name.length() >= 2 && name.length() <= 32, "Name must be between 2-32 characters long");
        this.currentPassword = currentPassword;
        this.name = name;
        set |= NAME;
        return this;
    }

    /**
     * Sets the avatar for the currently logged in account
     *
     * <p><b>Client-Accounts ({@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}) require the
     * current password to be updated. See {@link #setAvatar(net.dv8tion.jda.core.entities.Icon, String) #setAvatar(Icon, String)}</b>
     *
     * @param  avatar
     *         An {@link net.dv8tion.jda.core.entities.Icon Icon} instance representing
     *         the new Avatar for the current account, {@code null} to reset the avatar to the default avatar.
     *
     * @return AccountManager for chaining convenience
     */
    @CheckReturnValue
    public AccountManager setAvatar(Icon avatar)
    {
        return setAvatar(avatar, null);
    }

    /**
     * Sets the avatar for the currently logged in account
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
     * @return AccountManager for chaining convenience
     */
    @CheckReturnValue
    public AccountManager setAvatar(Icon avatar, String currentPassword)
    {
        this.currentPassword = currentPassword;
        this.avatar = avatar;
        set |= AVATAR;
        return this;
    }

    /**
     * Sets the email for the currently logged in client account.
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
     * @return AccountManager for chaining convenience
     */
    @CheckReturnValue
    public AccountManager setEmail(String email, String currentPassword)
    {
        Checks.notNull(email, "email");
        this.currentPassword = currentPassword;
        this.email = email;
        set |= EMAIL;
        return this;
    }

    /**
     * Sets the password for the currently logged in client account.
     * <br>If the new password is equal to the current password this does nothing.
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
     * @return AccountManager for chaining convenience
     */
    @CheckReturnValue
    public AccountManager setPassword(String newPassword, String currentPassword)
    {
        Checks.notNull(newPassword, "password");
        Checks.check(newPassword.length() >= 6 && newPassword.length() <= 128, "Password must be between 2-128 characters long");
        this.currentPassword = currentPassword;
        this.password = newPassword;
        set |= PASSWORD;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        boolean isClient = api.getAccountType() == AccountType.CLIENT;
        Checks.check(!isClient || (currentPassword != null && !currentPassword.isEmpty()),
            "Provided client account password to be used in auth is null or empty!");

        JSONObject body = new JSONObject();

        //Required fields. Populate with current values..
        body.put("username", selfUser.getName());
        body.put("avatar", opt(selfUser.getAvatarId()));

        if (shouldUpdate(NAME))
            body.put("username", name);
        if (shouldUpdate(AVATAR))
            body.put("avatar", avatar == null ? JSONObject.NULL : avatar.getEncoding());

        if (isClient)
        {
            //Required fields. Populate with current values.
            body.put("password", currentPassword);
            body.put("email", email);

            if (shouldUpdate(EMAIL))
                body.put("email", email);
            if (shouldUpdate(PASSWORD))
                body.put("new_password", password);
        }

        reset();
        return getRequestBody(body);
    }

    @Override
    protected void handleResponse(Response response, Request<Void> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        String newToken = response.getObject().getString("token").replace("Bot ", "");
        api.setToken(newToken);
        request.onSuccess(null);
    }
}
