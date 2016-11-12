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

package net.dv8tion.jda.core.exceptions;

import net.dv8tion.jda.core.AccountType;

public class AccountTypeException extends RuntimeException
{
    AccountType requiredType;

    public AccountTypeException(AccountType requiredType)
    {
        this(requiredType, "The current AccountType is not valid for the attempted action. Required AccountType: " + requiredType);
    }

    public AccountTypeException(AccountType requiredType, String message)
    {
        super(message);
        this.requiredType = requiredType;
    }

    public AccountType getRequiredType()
    {
        return requiredType;
    }
}
