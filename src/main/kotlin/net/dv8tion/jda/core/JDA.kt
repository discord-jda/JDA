package net.dv8tion.jda.core

inline fun jda(token: String, block: JDABuilder.() -> Unit): JDA {
    return jda(token, 1, block)[0]
}

inline fun jda(token: String, shards: Int, block: JDABuilder.() -> Unit): List<JDA> {
    val jda = mutableListOf<JDA>()
    for (id in 0..shards - 1) {
        val jb = JDABuilder(AccountType.BOT).apply {
            if (shards > 1) useSharding(id, shards)
            setToken(token)
            block()
        }
        jda.add(jb.buildBlocking())
    }
    return jda
}