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

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.managers.fields.AccountField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * An {@link #update(String) updatable} manager that allows
 * to modify account settings like the {@link #getNameField() username} or the {@link #getAvatarField() avatar}.
 *
 * <p>This manager allows to modify multiple fields at once
 * by getting the {@link net.dv8tion.jda.core.managers.fields.AccountField AccountFields} for specific
 * properties and setting or resetting their values; followed by a call of {@link #update(String)}!
 *
 * <p>The {@link net.dv8tion.jda.core.managers.AccountManager AccountManager} implementation
 * simplifies this process by giving simple setters that return the {@link #update(String) update} {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p><b>To update the {@link net.dv8tion.jda.core.entities.Game Game} or {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus}
 * for the current session use the {@link net.dv8tion.jda.core.managers.Presence Presence} instance of the corresponding JDA instance</b>
 */
public class AccountManagerUpdatable
{
    public static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+");

    protected final SelfUser selfUser;

    protected AccountField<String> name;
    protected AccountField<Icon> avatar;
    protected AccountField<String> email;
    protected AccountField<String> password;

    /**
     * Creates a new AccountManagerUpdatable instance
     *
     * @param selfUser
     *        A {@link net.dv8tion.jda.core.entities.SelfUser SelfUser} instance
     *        that represents the currently logged in account
     */
    public AccountManagerUpdatable(SelfUser selfUser)
    {
        this.selfUser = selfUser;
        setupFields();
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this AccountManagerUpdatable
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return selfUser.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.SelfUser SelfUser} that will be
     * modified by this AccountManagerUpdatable instance.
     * <br>This represents the currently logged in account.
     *
     * @return The corresponding SelfUser
     */
    public SelfUser getSelfUser()
    {
        return selfUser;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField}
     * for the {@code username} of the currently logged in account.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField} instance.
     *
     * <p>A username <b>must not</b> be {@code null} nor less than 2 characters or more than 32 characters long!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField} - Type: {@code String}
     */
    public AccountField<String> getNameField()
    {
        return name;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField}
     * for the {@code avatar} of the currently logged in account.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Icon)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField} instance.
     * <br>An {@link net.dv8tion.jda.core.entities.Icon Icon} can be retrieved through one of the static {@code Icon.from(...)} methods
     *
     * <p>Providing {@code null} as value will cause the {@link net.dv8tion.jda.core.entities.SelfUser#getDefaultAvatarId() default avatar} for this account to be used.
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField} - Type: {@link net.dv8tion.jda.core.entities.Icon Icon}
     */
    public AccountField<Icon> getAvatarField()
    {
        return avatar;
    }

    /**
     * <b><u>Client Only</u></b>
     *
     * <p>An {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField}
     * for the {@code email} of the currently logged in account.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField} instance.
     *
     * <p>An email <b>must not</b> be {@code null} and must be valid according to {@link #EMAIL_PATTERN}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField} - Type: {@code String}
     */
    public AccountField<String> getEmailField()
    {
        if (!isType(AccountType.CLIENT))
            throw new AccountTypeException(AccountType.CLIENT);

        return email;
    }

    /**
     * <b><u>Client Only</u></b>
     *
     * <p>An {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField}
     * for the {@code password} of the currently logged in account.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField} instance.
     *
     * <p>A password <b>must not</b> be {@code null} or empty and must be in the range of 6-128 characters in length!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.AccountField AccountField} - Type: {@code String}
     */
    public AccountField<String> getPasswordField()
    {
        if (!isType(AccountType.CLIENT))
            throw new AccountTypeException(AccountType.CLIENT);

        return password;
    }

    /**
     * Resets all {@link net.dv8tion.jda.core.managers.fields.AccountField Fields}
     * for this manager instance by calling {@link net.dv8tion.jda.core.managers.fields.Field#reset() Field.reset()} sequentially
     */
    public void reset()
    {
        name.reset();
        avatar.reset();

        if (isType(AccountType.CLIENT))
        {
            email.reset();
            password.reset();
        }
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.RestAction RestAction} instance
     * that will apply <b>all</b> changes that have been made to this manager instance.
     *
     * <p>Before applying new changes it is recommended to call {@link #reset()} to reset previous changes.
     * <br>This is automatically called if this method returns successfully.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} for this
     * update include the following:
     * <ul>
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_PASSWORD INVALID_PASSWORD}
     *      <br>If the specified {@code currentPassword} is not a valid password</li>
     * </ul>
     *
     * @param  currentPassword
     *         Used for accounts from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT},
     *         provide {@code null} if this is not a client-account
     *
     * @throws IllegalArgumentException
     *         If the provided password is null or empty and the currently logged in account
     *         is from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Updates all modified fields or does nothing if none of the {@link net.dv8tion.jda.core.managers.fields.Field Fields}
     *         have been modified. ({@link net.dv8tion.jda.core.requests.RestAction.EmptyRestAction EmptyRestAction})
     */
    public RestAction<Void> update(String currentPassword)
    {
        if (isType(AccountType.CLIENT) && (currentPassword == null || currentPassword.isEmpty()))
            throw new IllegalArgumentException("Provided client account password to be used in auth is null or empty!");

        if (!needToUpdate())
            return new RestAction.EmptyRestAction<>(null);

        JSONObject body = new JSONObject();

        //Required fields. Populate with current values..
        body.put("username", name.getOriginalValue());
        body.put("avatar", selfUser.getAvatarId() != null ? selfUser.getAvatarId() : JSONObject.NULL);

        if (name.shouldUpdate())
            body.put("username", name.getValue());
        if (avatar.shouldUpdate())
            body.put("avatar", avatar.getValue() != null ? avatar.getValue().getEncoding() : JSONObject.NULL);

        if (isType(AccountType.CLIENT))
        {
            //Required fields. Populate with current values.
            body.put("password", currentPassword);
            body.put("email", email.getOriginalValue());

            if (email.shouldUpdate())
                body.put("email", email.getValue());
            if (password.shouldUpdate())
                body.put("new_password", password.getValue());
        }

        reset();    //now that we've built our JSON object, reset the manager back to the non-modified state
        Route.CompiledRoute route = Route.Self.MODIFY_SELF.compile();
        return new RestAction<Void>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                String newToken = response.getObject().getString("token");
                newToken = newToken.replace("Bot ", "");

                ((JDAImpl) getJDA()).setToken(newToken);
                request.onSuccess(null);
            }
        };
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.RestAction RestAction} instance
     * that will apply <b>all</b> changes that have been made to this manager instance (one per runtime per JDA instance).
     *
     * <p>Before applying new changes it is recommended to call {@link #reset()} to reset previous changes.
     * <br>This is automatically called if this method returns successfully.
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     *         Updates all modified fields or does nothing if none of the {@link net.dv8tion.jda.core.managers.fields.Field Fields}
     *         have been modified. ({@link net.dv8tion.jda.core.requests.RestAction.EmptyRestAction EmptyRestAction})
     */
    public RestAction<Void> update()
    {
        if (getJDA().getAccountType() == AccountType.CLIENT)
            throw new AccountTypeException(AccountType.BOT);
        return update(null);
    }

    protected boolean needToUpdate()
    {
        return name.shouldUpdate()
                || avatar.shouldUpdate()
                || (isType(AccountType.CLIENT) && email.shouldUpdate())
                || (isType(AccountType.CLIENT) && password.shouldUpdate());
    }

    protected void setupFields()
    {
        name = new AccountField<String>(this, selfUser::getName)
        {
            @Override
            public void checkValue(String value)
            {
                Args.notNull(value, "account name");
                if (value.length() < 2 || value.length() > 32)
                    throw new IllegalArgumentException("Provided name must be 2 to 32 characters in length");
            }
        };

        avatar = new AccountField<Icon>(this, null)
        {
            @Override
            public void checkValue(Icon value) { }

            @Override
            public Icon getOriginalValue()
            {
                throw new UnsupportedOperationException("Cannot easily provide the original Avatar. Use User#getIconUrl() and download it yourself.");
            }

            @Override
            public boolean shouldUpdate()
            {
                return isSet();
            }
        };

        if (isType(AccountType.CLIENT))
        {
            email = new AccountField<String>(this, selfUser::getEmail)
            {
                @Override
                public void checkValue(String value)
                {
                    Args.notNull(value, "account email");
                    if (!EMAIL_PATTERN.matcher(value).find())
                        throw new IllegalArgumentException("Provided email is in invalid format. Provided value: " + value);
                }
            };

            password = new AccountField<String>(this, null)
            {
                @Override
                public void checkValue(String value)
                {
                    Args.notNull(value, "account password");
                    if (value.length() < 6 || value.length() > 128)
                        throw new IllegalArgumentException("Provided password must ben 6 to 128 characters in length");
                }

                @Override
                public String getOriginalValue()
                {
                    throw new UnsupportedOperationException("Cannot get the original password. We are not given this information.");
                }

                @Override
                public boolean shouldUpdate()
                {
                    return isSet();
                }
            };
        }
    }

    private boolean isType(AccountType type)
    {
        return getJDA().getAccountType() == type;
    }
}
