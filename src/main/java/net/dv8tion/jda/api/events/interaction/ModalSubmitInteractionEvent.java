package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.internal.interactions.ModalInteractionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModalSubmitInteractionEvent extends GenericInteractionCreateEvent
{
    private final JDA api;
    private final ModalInteractionImpl interaction;

    public ModalSubmitInteractionEvent(@NotNull JDA api, long responseNumber, @NotNull ModalInteractionImpl interaction)
    {
        super(api, responseNumber, interaction);
        this.api = api;
        this.interaction = interaction;

    }

    @NotNull
    @Override
    public ModalInteractionImpl getInteraction()
    {
        return interaction;
    }

    public String getModalId()
    {
        return interaction.getModalId();
    }

    public List<ActionRow> getComponents()
    {
        return interaction.getComponents();
    }

    @Nullable
    public TextInput getInputField(String id)
    {
        for (ActionRow row : getComponents())
        {
            for (ItemComponent component : row)
            {
                if (component instanceof TextInput)
                {
                    TextInput textInput = (TextInput) component;
                    if (textInput.getId().equals(id))
                        return textInput;
                }
            }
        }
        return null;
    }

    public ReplyCallbackAction deferReply()
    {
        return interaction.deferReply();
    }

    public ReplyCallbackAction deferReply(boolean ephemeral)
    {
        return interaction.deferReply(ephemeral);
    }

    public ReplyCallbackAction reply(String content)
    {
        return interaction.reply(content);
    }


}
