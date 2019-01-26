/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.entities.proxy;

import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.exceptions.AccountTypeException;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.managers.AccountManager;

public class SelfUserProxy extends UserProxy implements SelfUser
{
    public SelfUserProxy(SelfUser user)
    {
        super(user);
    }

    @Override
    public SelfUser getSubject()
    {
        SelfUser user = getJDA().getSelfUser();
        if (user == null) // should be impossible but you never know
            throw new ProxyResolutionException("SelfUser");
        return user;
    }

    @Override
    public SelfUserProxy getProxy()
    {
        return this;
    }

    @Override
    public boolean isVerified()
    {
        return getSubject().isVerified();
    }

    @Override
    public boolean isMfaEnabled()
    {
        return getSubject().isMfaEnabled();
    }

    @Override
    public String getEmail() throws AccountTypeException
    {
        return getSubject().getEmail();
    }

    @Override
    public boolean isMobile() throws AccountTypeException
    {
        return getSubject().isMobile();
    }

    @Override
    public boolean isNitro() throws AccountTypeException
    {
        return getSubject().isNitro();
    }

    @Override
    public String getPhoneNumber() throws AccountTypeException
    {
        return getSubject().getPhoneNumber();
    }

    @Override
    public long getAllowedFileSize()
    {
        return getSubject().getAllowedFileSize();
    }

    @Override
    public AccountManager getManager()
    {
        return getSubject().getManager();
    }
}
