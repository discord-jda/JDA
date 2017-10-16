package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.utils.Checks;

/**
 * Default implementation of {@link IGatewayProvider IGatewayProvider}.
 */
public class DefaultGatewayProvider implements IGatewayProvider
{
    private final JDAImpl api;

    public DefaultGatewayProvider(JDA api)
    {
        Checks.notNull(api, "api");
        this.api = (JDAImpl) api;
    }

    @Override
    public String getGatewayUrl()
    {
        try
        {
            RestAction<String> gateway = new RestAction<String>(api, Route.Misc.GATEWAY.compile())
            {
                @Override
                protected void handleResponse(Response response, Request<String> request)
                {
                    try
                    {
                        if (response.isOk())
                            request.onSuccess(response.getObject().getString("url"));
                        else
                            request.onFailure(new Exception("Failed to get gateway url"));
                    }
                    catch (Exception e)
                    {
                        request.onFailure(e);
                    }
                }
            };

            return gateway.complete(false) + "?encoding=json&v=" + WebSocketClient.DISCORD_GATEWAY_VERSION;
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
