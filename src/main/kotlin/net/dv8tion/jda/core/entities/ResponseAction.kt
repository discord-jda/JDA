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
            //setAuthor("Info", null, "https://gnarbot.xyz/assets/img/info.png")
            title = "Info"
            description = msg
            color = Color.BLUE
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
            //author("Error", null, "https://gnarbot.xyz/assets/img/error.png")
            title = "Error"
            description = msg
            color = Color.RED
        }.rest()
    }

    @JvmOverloads
    fun embed(title: String? = null): ResponseEmbedBuilder = ResponseEmbedBuilder(channel).setTitle(title)

    fun embed(title: String? = null, block: Consumer<ResponseEmbedBuilder>): ResponseEmbedBuilder {
        return embed(title).apply { block.accept(this) }
    }

    inline fun embed(title: String? = null, value: ResponseEmbedBuilder.() -> Unit): ResponseEmbedBuilder {
        return embed(title).apply { value(this) }
    }

    class ResponseEmbedBuilder(val channel: MessageChannel) : AbstractEmbedBuilder<ResponseEmbedBuilder>() {
        fun rest(): RestAction<Message> {
            return channel.sendMessage(build())
        }
    }
}

