package net.dv8tion.jda.api.interactions.components.text;

import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.internal.interactions.component.ModalImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Modal extends ActionComponent
{
    @NotNull
    String getId();

    @NotNull
    String getTitle();

    @NotNull
    List<ActionRow> getComponents();

    @NotNull
    @Override
    default ActionComponent withDisabled(boolean disabled)
    {
        throw new UnsupportedOperationException("Modals cannot be disabled!");
    }

    @NotNull
    @CheckReturnValue
    static Modal.Builder create(@NotNull String customId)
    {
        return new Modal.Builder(customId);
    }

    class Builder
    {
        private String id;
        private String title;
        private final List<ActionRow> components = new ArrayList<>();

        protected Builder(@NotNull String id)
        {
            setId(id);
        }

        public Builder setId(String id)
        {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title)
        {
            this.title = title;
            return this;
        }

        public Builder addComponents(ActionRow... components)
        {
            Collections.addAll(this.components, components);
            return this;
        }

        public Builder addComponents(Collection<? extends ActionRow> inputs)
        {
            this.components.addAll(inputs);
            return this;
        }

        public List<ActionRow> getComponents()
        {
            return components;
        }

        public String getTitle()
        {
            return title;
        }

        public String getId()
        {
            return id;
        }

        public Modal build()
        {
            return new ModalImpl(id, title, components);
        }
    }
}
