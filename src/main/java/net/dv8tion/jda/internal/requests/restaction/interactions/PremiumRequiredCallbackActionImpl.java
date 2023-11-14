package net.dv8tion.jda.internal.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.callbacks.IPremiumRequiredReplyCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class PremiumRequiredCallbackActionImpl extends InteractionCallbackImpl<Void> implements PremiumRequiredCallbackAction
{

    public PremiumRequiredCallbackActionImpl(IPremiumRequiredReplyCallback interaction)
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
    public PremiumRequiredCallbackAction setCheck(BooleanSupplier checks)
    {
        return (PremiumRequiredCallbackAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public PremiumRequiredCallbackAction deadline(long timestamp)
    {
        return (PremiumRequiredCallbackAction) super.deadline(timestamp);
    }
}
