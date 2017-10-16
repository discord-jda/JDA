package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.JDA;

/**
 * Factory interface for the creation of new {@link net.dv8tion.jda.core.requests.IGatewayProvider IGatewayProvider} objects.
 * <br>JDA, by default, uses {@link net.dv8tion.jda.core.requests.DefaultGatewayProviderFactory DefaultGatewayProviderFactory} for the
 * creation of its gateway providers.
 * <p>
 * Implementations of this interface are provided to
 * {@link net.dv8tion.jda.core.JDABuilder#setGatewayProviderFactory(IGatewayProviderFactory) JDABuilder.setAudioSendFactory(IAudioSendFactory)}.
 */
public interface IGatewayProviderFactory
{
    /**
     * Called by JDA's websocket client when it's first connecting and needs an {@link IGatewayProvider IGatewayProvider}
     * to tell it the URL it should connect to.
     *
     * @param jda
     *            The JDA instance that needs an {@link IGatewayProvider IGatewayProvider}.
     *
     * @return A new gateway provider to tell JDA the URL it should connect to.
     */
    IGatewayProvider createGatewayProvider(JDA jda);
}
