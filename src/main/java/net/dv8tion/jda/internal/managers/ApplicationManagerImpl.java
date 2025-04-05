package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.managers.ApplicationManager;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * When implement new fields, update also {@link #reset(long)}, {@link #reset(long...)}, {@link #reset()} and {@link #finalizeData()}
 */
public class ApplicationManagerImpl extends ManagerBase<ApplicationManager> implements ApplicationManager
{

    protected String description;
    protected Icon icon;
    protected Icon coverImage;

    public ApplicationManagerImpl(JDA jda)
    {
        super(jda, Route.Applications.EDIT_BOT_APPLICATION.compile());
    }

    @NotNull
    @Override
    public ApplicationInfo getApplicationInfo()
    {
        return this.getJDA().retrieveApplicationInfo().complete();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ApplicationManagerImpl reset(long fields)
    {
        super.reset(fields);

        if((fields & DESCRIPTION) == DESCRIPTION)
            description = null;
        if((fields & ICON) == ICON)
            icon = null;
        if((fields & COVER_IMAGE) == COVER_IMAGE)
            coverImage = null;

        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ApplicationManagerImpl reset(long... fields)
    {
        super.reset(fields);

        for(long field : fields)
        {

            if(field == DESCRIPTION)
                description = null;
            else if(field == ICON)
                icon = null;
            else if(field == COVER_IMAGE)
                coverImage = null;

        }

        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ApplicationManager reset()
    {
        super.reset();
        this.description = null;
        this.icon = null;
        this.coverImage = null;
        return this;
    }

    @NotNull
    @Override
    public ApplicationManager setDescription(@NotNull String description)
    {
        this.description = description.trim();
        set |= DESCRIPTION;
        return this;
    }

    @NotNull
    @Override
    public ApplicationManager setIcon(@NotNull Icon icon)
    {
        this.icon = icon;
        set |= ICON;
        return this;
    }

    @NotNull
    @Override
    public ApplicationManager setCoverImage(@NotNull Icon coverImage)
    {
        this.coverImage = coverImage;
        set |= COVER_IMAGE;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject body = DataObject.empty();

        if(shouldUpdate(DESCRIPTION))
            body.put("description", this.description);
        if(shouldUpdate(ICON))
            body.put("icon", this.icon == null ? null : this.icon.getEncoding());
        if(shouldUpdate(COVER_IMAGE))
            body.put("cover_image", this.coverImage == null ? null : this.coverImage.getEncoding());

        reset();
        return getRequestBody(body);
    }

    @Override
    protected void handleSuccess(Response response, Request<Void> request)
    {
        request.onSuccess(null);
    }

}
