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
package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.SelfInfo;
import net.dv8tion.jda.core.exceptions.AccountTypeException;

public class SelfInfoImpl extends UserImpl implements SelfInfo
{
    private boolean verified;
    private boolean mfaEnabled;

    //Client only
    private String email;

    public SelfInfoImpl(String id, JDAImpl api)
    {
        super(id, api);
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

//    @Override
//    public String getAuthUrl(Permission... perms)
//    {
//        return ApplicationUtil.getAuthInvite(getJDA(), perms);
//    }

    public SelfInfoImpl setVerified(boolean verified)
    {
        this.verified = verified;
        return this;
    }

    public SelfInfoImpl setMfaEnabled(boolean enabled)
    {
        this.mfaEnabled = enabled;
        return this;
    }

    public SelfInfoImpl setEmail(String email)
    {
        this.email = email;
        return this;
    }
}
