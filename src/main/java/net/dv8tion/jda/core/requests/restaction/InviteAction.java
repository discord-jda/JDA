package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.requests.*;

import org.json.JSONObject;

public class InviteAction extends RestAction<Invite>
{
    private Integer maxAge = null;
    private Integer maxUses = null;
    private Boolean temporary = null;
    private Boolean unique = null;

    public InviteAction(final JDA api, final String channelId)
    {
        super(api, Route.Invites.CREATE_INVITE.compile(channelId), null);
    }

    @Override
    protected void finalizeData()
    {
        final JSONObject object = new JSONObject();

        if (this.maxAge != null)
            object.put("max_age", (int) this.maxAge);
        if (this.maxUses != null)
            object.put("max_uses", (int) this.maxUses);
        if (this.temporary != null)
            object.put("temporary", (boolean) this.temporary);
        if (this.unique != null)
            object.put("unique", (boolean) this.unique);

        this.data = object;
    }

    @Override
    protected void handleResponse(final Response response, final Request request)
    {
        if (response.isOk())
            request.onSuccess(EntityBuilder.get(this.api).createInvite(response.getObject()));
        else
            request.onFailure(response);
    }

    /**
     * Sets the max age in seconds for the invite. Set this to {@code 0} if the invite should never expire. Default is {@code 86400} (24 hours).
     *
     * @param  maxAge
     *         The max age for this invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setMaxAge(final Integer maxAge)
    {
        if (maxAge < 0)
            throw new IllegalArgumentException("maxAge must be grater than 0!");
        this.maxAge = maxAge;
        return this;
    }

    /**
     * Sets the max uses for the invite. Set this to {@code 0} if the invite should have unlimited uses. Default is {@code 0}.
     *
     * @param  maxUses
     *         The max uses for this invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setMaxUses(final Integer maxUses)
    {
        if (maxUses < 0)
            throw new IllegalArgumentException("maxAge must be grater than 0!");
        this.maxUses = maxUses;
        return this;
    }

    /**
     * Sets weather the invite should only grant temporary membership. Default is {@code false}.
     *
     * @param  temporary
     *         Weather the invite should only grant temporary membership or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setTemporary(final Boolean temporary)
    {
        this.temporary = temporary;
        return this;
    }

    /**
     * Sets weather discord should reuse a similar invite. Default is {@code false}.
     *
     * @param  temporary
     *         Weather discord should reuse a similar invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setUnique(final Boolean unique)
    {
        this.unique = unique;
        return this;
    }
}
