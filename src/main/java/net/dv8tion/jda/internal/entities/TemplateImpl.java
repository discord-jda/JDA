package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.Template;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.TemplateManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.TemplateManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class TemplateImpl implements Template
{
    private final JDAImpl api;
    private final String code;
    private final String name;
    private final String description;
    private final int uses;
    private final User creator;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final Template.Guild guild;
    private final boolean synced;

    protected final ReentrantLock mngLock = new ReentrantLock();
    protected volatile TemplateManager manager;

    public TemplateImpl(final JDAImpl api, final String code, final String name, final String description,
                        final int uses, final User creator, final OffsetDateTime createdAt, final OffsetDateTime updatedAt,
                        final Template.Guild guild, final boolean synced)
    {
        this.api = api;
        this.code = code;
        this.name = name;
        this.description = description;
        this.uses = uses;
        this.creator = creator;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.guild = guild;
        this.synced = synced;
    }

    public static RestAction<Template> resolve(final JDA api, final String code)
    {
        Checks.notNull(code, "code");
        Checks.notNull(api, "api");

        Route.CompiledRoute route = Route.Templates.GET_TEMPLATE.compile(code);

        JDAImpl jda = (JDAImpl) api;
        return new RestActionImpl<>(api, route, (response, request) ->
                jda.getEntityBuilder().createTemplate(response.getObject()));
    }

    @Nonnull
    @Override
    public RestAction<Template> sync()
    {
        checkInteraction();
        final Route.CompiledRoute route = Route.Templates.SYNC_TEMPLATE.compile(guild.getId(), this.code);
        JDAImpl jda = (JDAImpl) api;
        return new RestActionImpl<>(api, route, (response, request) ->
                jda.getEntityBuilder().createTemplate(response.getObject()));
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        checkInteraction();
        final Route.CompiledRoute route = Route.Templates.DELETE_TEMPLATE.compile(guild.getId(), this.code);
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public String getCode()
    {
        return this.code;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return this.name;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public int getUses()
    {
        return this.uses;
    }

    @Nonnull
    @Override
    public User getCreator()
    {
        return this.creator;
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeCreated()
    {
        return this.createdAt;
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeUpdated()
    {
        return this.updatedAt;
    }

    @Nonnull
    @Override
    public Template.Guild getGuild()
    {
        return this.guild;
    }

    @Override
    public boolean isSynced()
    {
        return this.synced;
    }

    @Nonnull
    @Override
    public TemplateManager getManager()
    {
        TemplateManager mng = manager;
        if (mng == null)
        {
            mng = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new TemplateManagerImpl(this);
                return manager;
            });
        }
        return mng;
    }

    private void checkInteraction()
    {
        final net.dv8tion.jda.api.entities.Guild guild = this.api.getGuildById(this.guild.getIdLong());

        if (guild == null)
            throw new IllegalStateException("Cannot interact with a template without shared guild");
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(guild, Permission.MANAGE_SERVER);
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return this.api;
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof TemplateImpl))
            return false;
        TemplateImpl impl = (TemplateImpl) obj;
        return impl.code.equals(this.code);
    }

    @Override
    public String toString()
    {
        return "Template(" + this.code + ")";
    }

    public static class GuildImpl implements Guild
    {
        private final String name, iconId;
        private final long id;
        private final VerificationLevel verificationLevel;

        public GuildImpl(final long id, final String iconId, final String name, final VerificationLevel verificationLevel)
        {
            this.id = id;
            this.iconId = iconId;
            this.name = name;
            this.verificationLevel = verificationLevel;
        }

        @Nullable
        @Override
        public String getIconId()
        {
            return this.iconId;
        }

        @Nullable
        @Override
        public String getIconUrl()
        {
            return this.iconId == null ? null
                    : "https://cdn.discordapp.com/icons/" + this.id + "/" + this.iconId + ".png";
        }

        @Override
        public long getIdLong()
        {
            return this.id;
        }

        @Nonnull
        @Override
        public String getName()
        {
            return this.name;
        }

        @Nonnull
        @Override
        public VerificationLevel getVerificationLevel()
        {
            return this.verificationLevel;
        }
    }
}
