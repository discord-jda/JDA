package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.ModalInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyCallbackActionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModalInteractionImpl extends DeferrableInteractionImpl implements ModalInteraction
{
    private final String modalId;
    private final List<ActionRow> components;

    public ModalInteractionImpl(JDAImpl api, DataObject object)
    {
        super(api, object);

        DataObject data = object.getObject("data");

        this.modalId = data.getString("custom_id");

        this.components = data.getArray("components")
                .stream(DataArray::getObject)
                .map(ActionRow::fromData)
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public String getModalId()
    {
        return modalId;
    }

    @NotNull
    @Override
    public List<ActionRow> getComponents()
    {
        return Collections.unmodifiableList(components);
    }

    @NotNull
    @Override
    public ReplyCallbackAction deferReply()
    {
        return new ReplyCallbackActionImpl(hook);
    }
}
