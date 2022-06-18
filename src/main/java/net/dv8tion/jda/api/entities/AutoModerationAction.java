package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AutoModerationAction {
    /**
     * Gets the action type.
     *
     * @return {@link ActionType ActionTypes}
     */
    @Nonnull
    ActionType getActionType();

    /**
     * Additional metadata needed during the execution for this specific action type
     *
     * @return {@link ActionMetadata ActionMetadata}
     */
    @Nullable
    ActionMetadata getActionMetadata();
}
