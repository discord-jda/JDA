package net.dv8tion.jda.internal.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class PremiumCallbackActionImpl extends InteractionCallbackImpl<Void> implements PremiumCallbackAction
{

    public PremiumCallbackActionImpl(IModalCallback interaction)
    {
        super((InteractionImpl) interaction);
    }

    @Override
    protected RequestBody finalizeData()
    {
        return getRequestBody(DataObject.empty()
                .put("type", InteractionCallbackAction.ResponseType.PREMIUM_REQUIRED.getRaw())
                .put("data", DataObject.empty()));
    }

    @Nonnull
    @Override
    public PremiumCallbackAction setCheck(BooleanSupplier checks)
    {
        return (PremiumCallbackAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public PremiumCallbackAction deadline(long timestamp)
    {
        return (PremiumCallbackAction) super.deadline(timestamp);
    }
}

