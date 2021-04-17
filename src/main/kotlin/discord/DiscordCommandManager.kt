package me.geek.tom.serverutils.discord

//class DiscordCommandManager(config: Config) {
//    private val commands: MutableMap<String, DiscordCommand> = HashMap()
//
//    init {
//        if (config[DiscordBotSpec.Commands.enableList]) {
//            commands["players"] = PlayersCommand
//            commands["list"] = PlayersCommand
//            commands["online"] = PlayersCommand
//        }
//        if (config[DiscordBotSpec.Commands.enableIngameCommands]) {
//            commands["/"] = MinecraftCommand
//        }
//    }
//
//    fun handleMessage(server: MinecraftServer, message: Message): Boolean {
//        if (!message.contentRaw.startsWith(prefix)) {
//            return false
//        }
//
//        val cmd = message.contentRaw.substring(prefix.length)
//        for ((name, command) in commands) {
//            if (cmd.startsWith(name)) {
//                command.execute(server, message)
//                return true
//            }
//        }
//
//        return false
//    }
//
//    companion object {
//        const val prefix = "/"
//    }
//}
