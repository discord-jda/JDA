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

package net.dv8tion.jda.core.exceptions;

import net.dv8tion.jda.core.AccountType;

/**
 * Indicates that an operation is not possible unless the {@link net.dv8tion.jda.core.AccountType AccountType}
 * matches the one provided in {@link #getRequiredType()}
 */
public class AccountTypeException extends RuntimeException
{
    private final AccountType requiredType;

    /**
     * Creates a new AccountTypeException instance
     *
     * @param requiredType
     *        The required {@link net.dv8tion.jda.core.AccountType AccountType} for the operation
     */
    public AccountTypeException(AccountType requiredType)
    {
        this(requiredType, "The current AccountType is not valid for the attempted action. Required AccountType: " + requiredType);
    }

    /**
     * Creates a new AccountTypeException instance
     *
     * @param requiredType
     *        The required {@link net.dv8tion.jda.core.AccountType AccountType} for the operation
     * @param message
     *        A specialized message
     */
    public AccountTypeException(AccountType requiredType, String message)
    {
        super(message);
        this.requiredType = requiredType;
    }

    /**
     * The required {@link net.dv8tion.jda.core.AccountType AccountType} for the operation
     *
     * @return AccountType
     */
    public AccountType getRequiredType()
    {
        return requiredType;
    }

    public static void check(AccountType actualType, AccountType requiredType)
    {
        if (actualType != requiredType)
            throw new AccountTypeException(requiredType);
    }
}
