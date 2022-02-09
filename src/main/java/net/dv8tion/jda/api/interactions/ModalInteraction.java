package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.text.TextInput;

import javax.annotation.Nonnull;
import java.util.List;

public interface ModalInteraction extends IReplyCallback
{
    @Nonnull
    String getFormId();

    @Nonnull
    String getTitle();

    @Nonnull
    List<TextInput> getComponents();
}
