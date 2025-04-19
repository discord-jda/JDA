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

import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.managers.ApplicationEmojiManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class ApplicationEmojiManagerImpl extends ManagerBase<ApplicationEmojiManager> implements ApplicationEmojiManager
{
    protected final ApplicationEmoji emoji;

    protected String name;

    public ApplicationEmojiManagerImpl(ApplicationEmoji emoji)
    {
        super(emoji.getJDA(), Route.Applications.MODIFY_APPLICATION_EMOJI.compile(emoji.getJDA().getSelfUser().getApplicationId(), emoji.getId()));
        this.emoji = emoji;
    }

    @NotNull
    @Override
    public ApplicationEmoji getEmoji()
    {
        return emoji;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ApplicationEmojiManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ApplicationEmojiManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    public ApplicationEmojiManager setName(@NotNull String name)
    {
        Checks.inRange(name, 2, CustomEmoji.EMOJI_NAME_MAX_LENGTH, "Emoji name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Emoji name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();
        if (shouldUpdate(NAME))
            object.put("name", name);
        reset();
        return getRequestBody(object);
    }
}
