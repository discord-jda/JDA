package net.dv8tion.jda.api.interactions.modals;

import net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent;
import net.dv8tion.jda.api.utils.data.DataObject;

/**
 * Id/Value pair for a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent} TextInput.
 *
 * @see ModalSubmitInteractionEvent#getTextInputField(String)
 * @see ModalSubmitInteractionEvent#getTextInputs()
 */
public class TextInputMapping
{
    private final String id;
    private final String value;

    public TextInputMapping(DataObject object)
    {
        this.id = object.getString("custom_id");
        this.value = object.getString("value");
    }

    /**
     * The custom id of this TextInput
     *
     * @return The custom id of this TextInput
     */
    public String getId()
    {
        return id;
    }

    /**
     * The value of this TextInput.
     *
     * This is the string containing the content of the TextInput. (What the user typed)
     *
     * @return The value of this TextInput
     */
    public String getValue()
    {
        return value;
    }
}
