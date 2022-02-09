package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;

public interface IModalCallback extends IDeferrableCallback
{
    @NotNull
    @CheckReturnValue
    default RestAction<InteractionHook> replyModal(Modal modal)
    {
        Checks.notNull(modal, "Modal");
        Route.CompiledRoute route = Route.Interactions.CALLBACK.compile(getId(), getToken());
        DataObject object = DataObject.empty()
                .put("type", InteractionCallbackAction.ResponseType.MODAL.getRaw())
                .put("data", modal.toData());
        return new RestActionImpl<>(getJDA(), route, object, ((response, voidRequest) -> getHook()));
    }
}
