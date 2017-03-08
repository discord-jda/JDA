package net.dv8tion.jda.core.entities

import net.dv8tion.jda.core.AbstractEmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.requests.RestAction
import java.awt.Color
import java.util.function.Consumer

class ResponseAction(val channel: MessageChannel) {
    /**
     * Quick-reply to a message.
     *
     * @param text The text to send.
     * @return The Message created by this function.
     */
    fun text(text: String): RestAction<Message> {
        val message = MessageBuilder().append(text).build()

        return channel.sendMessage(message)
    }

    /**
     * Send a standard info message.
     *
     * @param msg The text to send.
     * @return The Message created by this function.
     */
    fun info(msg: String): RestAction<Message> {
        return embed {
            author("Info", null, "https://gnarbot.xyz/assets/img/info.png")
            description(msg)
        }.rest()
    }

    /**
     * Send a standard error message.
     *
     * @param msg The text to send.
     * @return The Message created by this function.
     */
    fun error(msg: String): RestAction<Message> {
        return embed {
            author("Error", null, "https://gnarbot.xyz/assets/img/error.png")
            description(msg)
            color(Color.RED)
        }.rest()
    }

    /**
     * Send an embeded message.
     *
     * @param text The text to send.
     * @return The Message created by this function.
     */
    @JvmOverloads
    fun embed(title: String? = null,
              text: String?,
              color: Color? = null,
              thumb: String? = null,
              img: String? = null): RestAction<Message> {
        return embed(title) {
            description(text)
            color?.let { color(it) }
            thumbnail(thumb)
            image(img)
        }.rest()
    }

    @JvmOverloads
    fun embed(title: String? = null): ResponseAbstractEmbedBuilder = ResponseAbstractEmbedBuilder(channel).title(title)

    fun embed(title: String? = null, block: Consumer<ResponseAbstractEmbedBuilder>): ResponseAbstractEmbedBuilder {
        return embed(title).apply { block.accept(this) }
    }

    inline fun embed(title: String? = null, value: ResponseAbstractEmbedBuilder.() -> Unit): ResponseAbstractEmbedBuilder {
        return embed(title).apply { value(this) }
    }

    class ResponseAbstractEmbedBuilder(val channel: MessageChannel) : AbstractEmbedBuilder<ResponseAbstractEmbedBuilder>() {
        fun rest(): RestAction<Message> {
            return channel.sendMessage(build())
        }
    }
}