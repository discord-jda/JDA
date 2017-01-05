package net.dv8tion.jda.client.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.List;

public interface AuthorizedApplication extends ISnowflake
{
    RestAction<Void> delete();

    String getAuthId();

    String getDescription();

    /**
     * Returns the iconId of this Application
     * @return
     *      The iconId of this Application or null, if no icon is defined
     */
    String getIconId();

    /**
     * Returns the icon-url of this Application
     * @return
     *      The icon-url of this Application or null, if no icon is defined
     */
    String getIconUrl();

    JDA getJDA();

    /**
     * Returns the name of this Application
     * @return
     *      The name of this Application
     */
    String getName();

    /**
     * Returns a {@link java.util.List List<}{@link String String}{@link java.util.List >} of RPC origins of the Application
     * @return
     *      The name of the bot's Application
     */
    List<String> getRpcOrigins();

    /**
     * Returns a {@link java.util.List List<}{@link String String}{@link java.util.List >} of authorized scopes of the Application
     * @return
     *      The name of the bot's Application
     */
    List<String> getScpoes();
}
