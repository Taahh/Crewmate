package dev.taah.crewmate.backend.plugin

import com.google.common.collect.Lists
import dev.taah.crewmate.api.plugin.Plugin
import dev.taah.crewmate.core.CrewmateServer
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.net.URLClassLoader

class PluginManager {

    val plugins: ArrayList<Plugin> = Lists.newArrayList()

    fun loadPlugins() {
        val pluginsFolder = File("plugins${File.separator}")
        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdir()
            return
        }
        pluginsFolder.listFiles()?.forEach {
            if (it.isDirectory || !it.name.lowercase().endsWith(".jar")) {
                return
            }
            val classLoader = URLClassLoader(
                arrayOf(it.toURI().toURL()),
                CrewmateServer::class.java.classLoader
            )
            val json = JSONObject(JSONTokener(classLoader.getResourceAsStream("plugin.json")))
            val pluginFile = CrewmateServer.GSON.fromJson(json.toString(), PluginFile::class.java)
            CrewmateServer.LOGGER.info("Loading plugin ${pluginFile.name} with version ${pluginFile.version}, main class ${pluginFile.mainClass}${if (pluginFile.authors != null) ", authors ${pluginFile.authors.contentToString()}" else ""}")
            val clazz = Class.forName(pluginFile.mainClass, true, classLoader)
            val plugin: Plugin = clazz.constructors[0].newInstance() as Plugin
            plugins.add(plugin)
        }
    }

    fun enablePlugins() {
        plugins.forEach { it.enable() }
    }

    fun disablePlugins() {
        plugins.forEach { it.disable() }
    }

}