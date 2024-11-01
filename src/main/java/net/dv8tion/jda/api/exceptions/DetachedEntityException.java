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
package net.dv8tion.jda.api.exceptions;

/**
 * Indicates that the operation cannot be done on a detached entity.
 *
 * @see net.dv8tion.jda.api.entities.detached.IDetachableEntity
 */
public class DetachedEntityException extends RuntimeException
{
    /**
     * Creates a new DetachedEntityException
     */
    public DetachedEntityException()
    {
        this("Cannot perform action as the bot is not in the guild");
    }

    /**
     * Creates a new DetachedEntityException
     *
     * @param reason
     *        The reason for this Exception
     */
    public DetachedEntityException(String reason)
    {
        super(reason);
    }
}
