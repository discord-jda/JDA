package net.dv8tion.jda.api.interactions.modals;

import net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;

/**
 * ID/Component pair for a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent}.
 *
 * @see ModalSubmitInteractionEvent#getValue(String)
 * @see ModalSubmitInteractionEvent#getValues()
 */
public class ModalMapping
{
    private final String id;
    private final String value;
    private final Component.Type componentType;

    public ModalMapping(DataObject object)
    {
        this.id = object.getString("custom_id");
        this.value = object.getString("value");
        this.componentType = Component.Type.fromKey(object.getInt("type"));
    }

    /**
     * The custom id of this component
     *
     * @return The custom id of this component
     */
    @Nonnull
    public String getId()
    {
        return id;
    }

    @Nonnull
    public Component.Type getType()
    {
        return componentType;
    }

    /**
     * The String representation of this component.
     *
     * For TextInputs, this returns what the User typed in it.
     *
     * @return The String representation of this component.
     */
    @Nonnull
    public String getAsString()
    {
        return value;
    }
}
