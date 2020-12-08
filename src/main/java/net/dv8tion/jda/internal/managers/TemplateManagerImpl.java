package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Template;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.TemplateManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.TemplateImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemplateManagerImpl extends ManagerBase<TemplateManager> implements TemplateManager
{
    protected final Template template;
    protected final JDA api;

    protected String name;
    protected String description;

    /**
     * Creates a new TemplateManager instance
     *
     * @param template
     *        {@link net.dv8tion.jda.api.entities.Template Template} that should be modified
     */
    public TemplateManagerImpl(TemplateImpl template)
    {
        super(template.getJDA(), Route.Templates.MODIFY_TEMPLATE.compile(template.getGuild().getId(), template.getCode()));
        this.template = template;
        this.api = template.getJDA();
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & DESCRIPTION) == DESCRIPTION)
            this.description = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl reset()
    {
        super.reset();
        this.name = null;
        this.description = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl setName(@Nonnull String name)
    {
        Checks.notNull(name, "Name");
        Checks.check(name.length() >= 2 && name.length() <= 100, "Name must be between 1-100 characters long");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl setDescription(@Nullable String description)
    {
        Checks.check(description == null || description.length() <= 120, "Provided name must be 0 - 120 characters in length");
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject body = DataObject.empty().put("name", template.getName());
        if (shouldUpdate(NAME))
            body.put("name", name);
        if (shouldUpdate(DESCRIPTION))
            body.put("description", name);

        reset(); //now that we've built our JSON object, reset the manager back to the non-modified state
        return getRequestBody(body);
    }

    @Override
    protected boolean checkPermissions()
    {
        final Guild guild = api.getGuildById(template.getGuild().getIdLong());

        if (guild == null)
            throw new IllegalStateException("Cannot modify a template without shared guild");
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(guild, Permission.MANAGE_SERVER);
        return super.checkPermissions();
    }
}
