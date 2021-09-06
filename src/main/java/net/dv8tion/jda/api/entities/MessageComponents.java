package net.dv8tion.jda.api.entities;

import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageComponents
{
    @NotNull
    public List<Button> getButtons()
    {
        return Collections.emptyList();
    }

    @NotNull
    public List<SelectionMenu> getMenus()
    {
        return Collections.emptyList();
    }

    @NotNull
    public List<ActionRow> getActionRows()
    {
        return Collections.emptyList();
    }

    @Nullable
    public Button getButtonById(String id, boolean ignoreCase) {
        return null;
    }

    @Nullable
    public SelectionMenu getMenuById(String id, boolean ignoreCase) {
        return null;
    }
}
