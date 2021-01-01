package me.geek.tom.serverutils.chatfilter.api.impl

import me.geek.tom.serverutils.chatfilter.api.ScriptRequirements
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

class ScriptRequirementsImpl(
    private val requirements: Map<String, Path>,
) : ScriptRequirements {
    override fun getRequirement(name: String): String {
        if (!requirements.containsKey(name)) {
            throw IllegalArgumentException("Requirement $name was not found!")
        }
        return Files.readAllLines(requirements[name]!!, Charset.defaultCharset()).joinToString("\n")
    }
}
