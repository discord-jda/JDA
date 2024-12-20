package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;

import javax.annotation.Nonnull;

/**
 * Interactions which have a custom ID, namely {@link ComponentInteraction} and {@link  ModalInteraction}.
 *
 * <br>This id does not have to be numerical.
 *
 */
public interface CustomIdInteraction
{
    /**
     * The custom ID provided to the component or modal when it was originally created.
     * <br>This value should be used to determine what action to take in regard to this interaction.
     *
     * <br>This id does not have to be numerical.
     *
     * @return The custom ID
     *
     * @see ComponentInteraction#getComponentId()
     * @see ModalInteraction#getModalId()
     */
    @Nonnull
    String getCustomId();
}
