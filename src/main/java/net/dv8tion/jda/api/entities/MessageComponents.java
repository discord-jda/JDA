package net.dv8tion.jda.api.entities;

import java.util.Collections;
import java.util.List;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageComponents
{
    private final List<ActionRow> actionRows;

    public MessageComponents(@NotNull List<ActionRow> actionRows) {
        this.actionRows = actionRows;
    }

    @NotNull
    public List<Button> getButtons()
    {
        return getActionRows().stream()
                .map(ActionRow::getButtons)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<SelectionMenu> getMenus()
    {
        return getActionRows()
                .stream()
                .flatMap(x -> x.getComponents().stream())
                .filter(SelectionMenu.class::isInstance)
                .map(SelectionMenu.class::cast)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<ActionRow> getActionRows()
    {
        return actionRows;
    }

    @Nullable
    public Button getButtonById(@NotNull String id)
    {
        Checks.notNull(id, "Button ID");
        return getButtons().stream()
                .filter(it -> id.equals(it.getId()))
                .findFirst()
                .orElse(null);
    }

    @Nonnull
    List<Button> getButtonsByLabel(@Nonnull String label, boolean ignoreCase)
    {
        Checks.notNull(label, "Label");
        Predicate<Button> filter;
        if (ignoreCase)
            filter = b -> label.equalsIgnoreCase(b.getLabel());
        else
            filter = b -> label.equals(b.getLabel());

        return getButtons().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Nullable
    public SelectionMenu getMenuById(@NotNull String id)
    {
        Checks.notNull(id, "Menu ID");
        return getMenus().stream()
                .filter(it -> id.equals(it.getId()))
                .findFirst().orElse(null);
    }
}
