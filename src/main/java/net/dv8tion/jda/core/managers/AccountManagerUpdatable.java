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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.managers.fields.AccountField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class AccountManagerUpdatable
{
    public static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+");

    protected final SelfUser selfUser;

    protected AccountField<String> name;
    protected AccountField<String> email;
    protected AccountField<String> password;
//    protected AccountField<Avatar> avatar;

    public AccountManagerUpdatable(SelfUser selfUser)
    {
        this.selfUser = selfUser;
        setupFields();
    }

    public JDA getJDA()
    {
        return selfUser.getJDA();
    }

    public SelfUser getSelfUser()
    {
        return selfUser;
    }

    public AccountField<String> getNameField()
    {
        return name;
    }

    public AccountField<String> getEmailField()
    {
        if (!isType(AccountType.CLIENT))
            throw new AccountTypeException(AccountType.CLIENT);

        return email;
    }

    public AccountField<String> getPasswordField()
    {
        if (!isType(AccountType.CLIENT))
            throw new AccountTypeException(AccountType.CLIENT);

        return password;
    }

    public void reset()
    {
        name.reset();

        if (isType(AccountType.CLIENT))
        {
            email.reset();
            password.reset();
        }
    }

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
                if (response.isOk())
                {
                    String newToken = response.getObject().getString("token");
                    newToken = newToken.replace("Bot ", "");

                    ((JDAImpl) getJDA()).setToken(newToken);
                    request.onSuccess(null);
                }
                else
                    request.onFailure(response);
            }
        };
    }

    protected boolean needToUpdate()
    {
        return name.shouldUpdate()
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
                checkNull(value, "account name");
                if (value.length() < 2 || value.length() > 32)
                    throw new IllegalArgumentException("Provided name must be 2 to 32 characters in length");
            }
        };

        if (isType(AccountType.CLIENT))
        {
            email = new AccountField<String>(this, selfUser::getEmail)
            {
                @Override
                public void checkValue(String value)
                {
                    checkNull(value, "account email");
                    if (!EMAIL_PATTERN.matcher(value).find())
                        throw new IllegalArgumentException("Provided email is in invalid format. Provided value: " + value);
                }
            };

            password = new AccountField<String>(this, null)
            {
                @Override
                public void checkValue(String value)
                {
                    checkNull(value, "account password");
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
