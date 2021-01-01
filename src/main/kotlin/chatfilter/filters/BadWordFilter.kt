package me.geek.tom.serverutils.chatfilter.filters

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.geek.tom.serverutils.chatfilter.ChatFilter
import net.minecraft.server.MinecraftServer
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

class BadWordFilter : ChatFilter {

    private val badWords = ArrayList<String>()

    override fun initialise(configDir: Path, server: MinecraftServer) {
        val wordsFile = configDir.resolve("badwords.json")
        if (!Files.exists(wordsFile)) {
            LOGGER.info("badwords.json does not exist, downloading latest copy from GitHub...")
            FileUtils.copyURLToFile(URL("https://raw.githubusercontent.com/web-mech/badwords/master/lib/lang.json"),
                wordsFile.toFile())
        }
        LOGGER.info("Loading word list...")
        val json = Gson().fromJson(wordsFile.toFile().reader(), JsonObject::class.java)
        val wordsFromJson = json["words"].asJsonArray
        wordsFromJson.forEach { badWords.add(it.asString) }
    }

    override fun checkChatMessage(message: String): Boolean {
        val parts = message.split("[^a-zA-z]".toRegex()).filter { it.isNotEmpty() }
        for (part in parts) {
            if (badWords.contains(part.toLowerCase()))
                return false
        }
        return true
    }

    override fun getName(): String {
        return "Bad words"
    }

    companion object {
        private val LOGGER = LogManager.getLogger()
    }
}
