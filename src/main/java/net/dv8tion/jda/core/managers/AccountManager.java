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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.requests.RestAction;

public class AccountManager
{
    protected final AccountManagerUpdatable updatable;

    public AccountManager(SelfUser selfUser)
    {
        this.updatable = new AccountManagerUpdatable(selfUser);
    }

    public JDA getJDA()
    {
        return updatable.getJDA();
    }

    public SelfUser getSelfUser()
    {
        return updatable.getSelfUser();
    }

    public RestAction<Void> setName(String name)
    {
        return setName(name, null);
    }

    public RestAction<Void> setName(String name, String currentPassword)
    {
        return updatable.getNameField().setValue(name).update(currentPassword);
    }

    public RestAction<Void> setEmail(String email, String currentPassword)
    {
        return updatable.getEmailField().setValue(email).update(currentPassword);
    }

    public RestAction<Void> setPassword(String newPassword, String currentPassword)
    {
        return updatable.getPasswordField().setValue(newPassword).update(currentPassword);
    }
}
