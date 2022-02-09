package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface IModalCallback extends IDeferrableCallback
{
    @Nonnull
    @CheckReturnValue
    default RestAction<InteractionHook> replyModal(Modal modal)
    {
        Route.CompiledRoute route = Route.Interactions.CALLBACK.compile(getId(), getToken());
        DataObject object = DataObject.empty()
                .put("type", InteractionCallbackAction.ResponseType.MODAL.getRaw())
                .put("data", modal.toData());
        return new RestActionImpl<>(getJDA(), route, object, ((response, voidRequest) -> getHook()));
    }
}
