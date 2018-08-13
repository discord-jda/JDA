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
package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.managers.AccountManager;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.concurrent.locks.ReentrantLock;

public class SelfUserImpl extends UserImpl implements SelfUser
{
    protected final ReentrantLock mngLock = new ReentrantLock();
    protected volatile AccountManager manager;

    private boolean verified;
    private boolean mfaEnabled;

    //Client only
    private String email;
    private String phoneNumber;
    private boolean mobile;
    private boolean nitro;

    public SelfUserImpl(long id, JDAImpl api)
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
    public String getPhoneNumber() throws AccountTypeException
    {
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT, "Phone number retrieval can only be done on CLIENT accounts!");
        return this.phoneNumber;
    }

    @Override
    public boolean isMobile() throws AccountTypeException
    {
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT, "Mobile app retrieval can only be done on CLIENT accounts!");
        return this.mobile;
    }

    @Override
    public boolean isNitro() throws AccountTypeException
    {
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT, "Nitro status retrieval can only be done on CLIENT accounts!");
        return this.nitro;
    }

    @Override
    public long getAllowedFileSize()
    {
        if (this.nitro) // by directly accessing the field we don't need to check the account type
            return Message.MAX_FILE_SIZE_NITRO;
        else
            return Message.MAX_FILE_SIZE;
    }

    @Override
    public AccountManager getManager()
    {
        AccountManager mng = manager;
        if (mng == null)
        {
            mng = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new AccountManager(this);
                return manager;
            });
        }
        return mng;
    }

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

    public SelfUserImpl setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public SelfUserImpl setMobile(boolean mobile)
    {
        this.mobile = mobile;
        return this;
    }

    public SelfUserImpl setNitro(boolean nitro)
    {
        this.nitro = nitro;
        return this;
    }
}
