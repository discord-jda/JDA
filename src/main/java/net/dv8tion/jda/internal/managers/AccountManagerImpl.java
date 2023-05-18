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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.managers.AccountManager;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class AccountManagerImpl extends ManagerBase<AccountManager> implements AccountManager
{
    protected final SelfUser selfUser;

    protected String name;
    protected Icon avatar;

    /**
     * Creates a new AccountManager instance
     *
     * @param selfUser
     *        The {@link net.dv8tion.jda.api.entities.SelfUser SelfUser} to manage
     */
    public AccountManagerImpl(SelfUser selfUser)
    {
        super(selfUser.getJDA(), Route.Self.MODIFY_SELF.compile());
        this.selfUser = selfUser;
    }

    @Nonnull
    @Override
    public SelfUser getSelfUser()
    {
        return selfUser;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public AccountManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & AVATAR) == AVATAR)
            avatar = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public AccountManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public AccountManagerImpl reset()
    {
        super.reset();
        avatar = null;
        return this;
    }

    @Nonnull
    @Override
    @Deprecated
    @CheckReturnValue
    public AccountManagerImpl setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public AccountManagerImpl setAvatar(Icon avatar)
    {
        this.avatar = avatar;
        set |= AVATAR;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject body = DataObject.empty();

        //Required fields. Populate with current values..
        body.put("username", getSelfUser().getName());
        body.put("avatar", getSelfUser().getAvatarId());

        if (shouldUpdate(NAME))
            body.put("username", name);
        if (shouldUpdate(AVATAR))
            body.put("avatar", avatar == null ? null : avatar.getEncoding());

        reset();
        return getRequestBody(body);
    }

    @Override
    protected void handleSuccess(Response response, Request<Void> request)
    {
//        String newToken = response.getObject().getString("token").replace("Bot ", "");
//        api.setToken(newToken);
        request.onSuccess(null);
    }
}
