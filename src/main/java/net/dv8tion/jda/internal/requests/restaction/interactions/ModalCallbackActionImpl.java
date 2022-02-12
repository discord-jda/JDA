package net.dv8tion.jda.internal.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;

public class ModalCallbackActionImpl extends DeferrableCallbackActionImpl implements ModalCallbackAction
{
    private final Modal modal;

    public ModalCallbackActionImpl(InteractionHook hook, Modal modal)
    {
        super((InteractionHookImpl) hook);
        this.modal = modal;
    }

    @Override
    protected DataObject toData()
    {
        return DataObject.empty()
                .put("type", InteractionCallbackAction.ResponseType.MODAL.getRaw())
                .put("data", modal);
    }
}
