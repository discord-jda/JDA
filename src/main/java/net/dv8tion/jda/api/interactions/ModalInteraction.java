package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ModalInteraction extends IReplyCallback
{
    @NotNull
    String getModalId();

    @NotNull
    List<ActionRow> getComponents();
}
