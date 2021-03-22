package me.geek.tom.serverutils.chatfilter.api;

/**
 * Exposed to chat filter scripts as a global constant called 'requirements' and is used to have access to dependent
 * files at runtime (eg. a badwords list)
 */
@SuppressWarnings("unused")
public interface ScriptRequirements {
    String getRequirement(String name);
}
