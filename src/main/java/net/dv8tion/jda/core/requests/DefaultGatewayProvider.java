package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.entities.impl.JDAImpl;

/**
 * Default implementation of {@link IGatewayProvider IGatewayProvider} that queries discord for the gateway url
 */
public class DefaultGatewayProvider implements IGatewayProvider
{
    private final JDAImpl api;

    public DefaultGatewayProvider(JDAImpl api)
    {
        this.api = api;
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
