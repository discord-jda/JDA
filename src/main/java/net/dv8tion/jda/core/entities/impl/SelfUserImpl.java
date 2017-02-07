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
package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.managers.AccountManager;
import net.dv8tion.jda.core.managers.AccountManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;

public class SelfUserImpl extends UserImpl implements SelfUser
{
    protected volatile AccountManager manager;
    protected volatile AccountManagerUpdatable managerUpdatable;
    private Object mngLock = new Object();

    private boolean verified;
    private boolean mfaEnabled;

    //Client only
    private String email;

    public SelfUserImpl(String id, JDAImpl api)
    {
        super(id, api);
    }

    @Override
    public boolean hasPrivateChannel()
    {
        return false;
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        throw new UnsupportedOperationException("You cannot get a PrivateChannel with yourself (SelfUser)");
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel()
    {
        throw new UnsupportedOperationException("You cannot open a PrivateChannel with yourself (SelfUser)");
    }

    @Override
    public boolean isVerified()
    {
        return verified;
    }

    @Override
    public boolean isMfaEnabled()
    {
        return mfaEnabled;
    }

    @Override
    public String getEmail() throws AccountTypeException
    {
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT, "Email retrieval can only be done on CLIENT accounts!");
        return email;
    }

    @Override
    public AccountManager getManager()
    {
        AccountManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new AccountManager(this);
            }
        }
        return mng;
    }

    @Override
    public AccountManagerUpdatable getManagerUpdatable()
    {
        AccountManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new AccountManagerUpdatable(this);
            }
        }
        return mng;
    }

//    @Override
//    public String getAuthUrl(Permission... perms)
//    {
//        return ApplicationUtil.getAuthInvite(getJDA(), perms);
//    }

    public SelfUserImpl setVerified(boolean verified)
    {
        this.verified = verified;
        return this;
    }

    public SelfUserImpl setMfaEnabled(boolean enabled)
    {
        this.mfaEnabled = enabled;
        return this;
    }

    public SelfUserImpl setEmail(String email)
    {
        this.email = email;
        return this;
    }
}
