package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import javax.annotation.Nonnull;
import java.util.List;

public interface ModalInteraction extends IReplyCallback
{
    @Nonnull
    String getModalId();

    @Nonnull
    List<ActionRow> getComponents();
}
