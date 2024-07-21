package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
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
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.inRange(name, 2, 32, "Name");
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
