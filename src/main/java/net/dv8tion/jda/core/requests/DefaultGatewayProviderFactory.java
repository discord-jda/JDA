package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;

/**
 * The default implementation of {@link IGatewayProviderFactory IGatewayProviderFactory}.
 */
public class DefaultGatewayProviderFactory implements IGatewayProviderFactory
{
    @Override
    public IGatewayProvider createGatewayProvider(JDA jda)
    {
        return new DefaultGatewayProvider((JDAImpl)jda);
    }
}
