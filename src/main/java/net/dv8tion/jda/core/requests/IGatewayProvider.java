package net.dv8tion.jda.core.requests;

/**
 * Tells JDA to which URL it should connect the websocket. Custom implementations can be used to have
 * the actual gateway connection on a separate process, so that if the bot process ever shuts down, the
 * connection won't be lost and events can still be received or, for large bots, the daily login limit
 * isn't reached
 */
public interface IGatewayProvider
{
    /**
     * Return a URL telling JDA where to connect the websocket, allowing proxying of the connection
     * to the discord gateway, such as a cache for faster bot loads and avoiding the need to re-login when
     * updating the bot.
     *
     * @return The URL JDA should connect to
     */
    String getGatewayUrl();
}
