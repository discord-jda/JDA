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

package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;

public class ForumPost
{
    private final Message message;
    private final ThreadChannel thread;

    public ForumPost(@Nonnull Message message, @Nonnull ThreadChannel thread)
    {
        this.message = message;
        this.thread = thread;
    }

    @Nonnull
    public Message getMessage()
    {
        return message;
    }

    @Nonnull
    public ThreadChannel getThread()
    {
        return thread;
    }
}
