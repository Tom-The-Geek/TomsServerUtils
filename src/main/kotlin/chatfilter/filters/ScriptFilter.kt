package me.geek.tom.serverutils.chatfilter.filters

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.geek.tom.serverutils.chatfilter.ChatFilter
import me.geek.tom.serverutils.chatfilter.api.GlobalMethods
import me.geek.tom.serverutils.chatfilter.api.impl.ScriptRequirementsImpl
import me.geek.tom.serverutils.configDir
import net.minecraft.server.MinecraftServer
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class ScriptFilter(
    scriptConfig: Path
) : ChatFilter {
    private val config: ScriptConfig
    init {
        val obj = GSON.fromJson(Files.newBufferedReader(scriptConfig), JsonObject::class.java)
        val result = ScriptConfig.CODEC.parse(Dynamic(JsonOps.INSTANCE, obj))
        this.config = result.resultOrPartial(LOGGER::error).orElseThrow { JsonParseException("failed to parse!") }
        if (this.config.name.matches(NAME_REGEX)) {
            throw IllegalStateException("Name should match ${NAME_REGEX.pattern}")
        }
    }

    private var dataDir: Path? = null
    private fun getDataDir(configDir: Path?): Path {
        if (dataDir != null) return dataDir!!
        if (configDir != null) {
            dataDir = configDir.resolve("scripts").resolve(this.getName())
            return dataDir!!
        }
        throw IllegalStateException()
    }

    override fun initialise(configDir: Path, server: MinecraftServer) {
        this.config.requiredFiles.forEach {
            it.download(getDataDir(configDir))
        }
    }

    override fun checkChatMessage(message: String): Boolean {
        val cx = Context.enter()
        val scope = cx.initSafeStandardObjects()

        ScriptableObject.putConstProperty(scope, "requirements", ScriptRequirementsImpl(this.config.bakedRequirements))
        ScriptableObject.putConstProperty(scope, "message", message)
        ScriptableObject.putConstProperty(scope, "message_parts", message.split("[^a-zA-z]".toRegex()).filter { it.isNotEmpty() })
        scope.defineFunctionProperties(arrayOf(
            "setMessageOk", "debug"
        ), GlobalMethods::class.java, ScriptableObject.CONST)

        cx.evaluateReader(scope, Files.newBufferedReader(this.config.getScriptPath(configDir.resolve("scripts"))),
            this.getName(), 1, null)

        Context.exit()

        return GlobalMethods.isMessageOk()
    }

    override fun getName(): String {
        return this.config.name
    }

    companion object {
        private val GSON by lazy { GsonBuilder().create() }
        private val LOGGER by lazy { LogManager.getLogger() }
        private val NAME_REGEX by lazy { Regex("[^a-zA-Z_-]") }
    }

    data class ScriptConfig(
        val name: String,
        val script: String,
        val requiredFiles: List<FileDownload>
    ) {

        private var scriptPath: Path? = null

        val bakedRequirements: Map<String, Path> get() =
            requiredFiles.stream().collect(Collectors.toMap({ it.name }) { it.path!! })

        fun getScriptPath(dataDir: Path): Path {
            if (scriptPath != null) return scriptPath!!
            scriptPath = dataDir.resolve(if (script.endsWith(".js")) script else "$script.js")
            return scriptPath!!
        }

        companion object {
            val CODEC: Codec<ScriptConfig> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("name").forGetter { it.name },
                    Codec.STRING.fieldOf("script").forGetter { it.script },
                    FileDownload.CODEC.listOf().fieldOf("required_files").forGetter { it.requiredFiles }
                ).apply(instance, ::ScriptConfig)
            }
        }

        data class FileDownload(
            val name: String,
            val url: String,
        ) {
            var path: Path? = null

            fun download(dataDir: Path) {
                path = dataDir.resolve(this.name)
                LOGGER.info("Downloading $url to $path (requirement of a script)...")
                FileUtils.copyURLToFile(URL(url), path?.toFile())
            }

            companion object {
                val CODEC: Codec<FileDownload> = RecordCodecBuilder.create { instance ->
                    instance.group(
                        Codec.STRING.fieldOf("name").forGetter { it.name },
                        Codec.STRING.fieldOf("url").forGetter { it.url }
                    ).apply(instance, ::FileDownload)
                }
            }
        }
    }
}
