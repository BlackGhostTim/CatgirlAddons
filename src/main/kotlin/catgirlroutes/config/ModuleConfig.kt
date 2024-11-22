package catgirlroutes.config

import catgirlroutes.CatgirlRoutes.Companion.MOD_NAME
import catgirlroutes.config.jsonutils.SettingDeserializer
import catgirlroutes.config.jsonutils.SettingSerializer
import catgirlroutes.module.ConfigModule
import catgirlroutes.module.ModuleManager
import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.impl.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.awt.Color
import java.io.File
import java.io.IOException

/**
 * ## A class to handle the module config file for the mod.
 *
 * Provides methods to save and load the settings for all [modules][ModuleManager.modules] in [ModuleManager] to / from the file.
 *
 * @author Aton
 */
class ModuleConfig(path: File) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingSerializer())
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingDeserializer())
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting().create()


    private val configFile = File(path, "catgirlroutesConfig.json")

    init {
        try {
            // This gets run before the pre initialization event (it gets run when the Companion object from FloppaClient
            // is created)
            // therefore the directory did not get created by the preInit handler.
            // It is created here
            if (!path.exists()) {
                path.mkdirs()
            }
            // create file if it doesn't exist
            configFile.createNewFile()
        } catch (e: Exception) {
            println("Error initializing $MOD_NAME module config")
        }
    }

    fun loadConfig() {
        try {
            val configModules: ArrayList<ConfigModule>
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") {
                    return
                }
                configModules= gson.fromJson(
                    this,
                    object : TypeToken<ArrayList<ConfigModule>>() {}.type
                )
            }
            configModules.forEach { configModule ->
                ModuleManager.getModuleByName(configModule.name).run updateModule@{
                    // If the module was not found check whether it can be a keybind
                    val module = this!!
                    if (module.enabled != configModule.enabled) module.toggle()
//                    module.keyCode = configModule.keyCode
                    for (configSetting in configModule.settings) {
                        // It seems like when the config parsing failed it can result in this being null. The compiler does not know this.
                        // This check ensures that the rest of the config will still get processed in that case, avoiding the loss of data.
                        // So just suppress the warning here.
                        @Suppress("SENSELESS_COMPARISON")
                        if (configSetting == null) continue
                        val setting = module.getSettingByName(configSetting.name) ?: continue
                        when (setting) {
                            is BooleanSetting ->    if (configSetting is BooleanSetting) setting.enabled = configSetting.enabled
                            is NumberSetting ->     if (configSetting is NumberSetting) setting.value = configSetting.value
                            is ColorSetting ->      if (configSetting is NumberSetting) setting.value = Color(configSetting.value.toInt(), true)
                            is StringSelectorSetting -> if (configSetting is StringSetting) setting.selected = configSetting.text
                            is SelectorSetting ->   if (configSetting is StringSetting) setting.selected = configSetting.text
                            is StringSetting ->     if (configSetting is StringSetting) setting.text = configSetting.text
                            is KeyBindSetting ->    if (configSetting is NumberSetting) { setting.value = Keybinding(configSetting.value.toInt()).apply { onPress = setting.value.onPress } } // fuck me I'm a nigger
                        }
                    }
                }
            }

        } catch (e: JsonSyntaxException) {
            println("Error parsing $MOD_NAME config.")
            println(e.message)
            e.printStackTrace()
        } catch (e: JsonIOException) {
            println("Error reading $MOD_NAME config.")
        } catch (e: Exception) {
            println("$MOD_NAME Config Error.")
            println(e.message)
            e.printStackTrace()
        }
    }

    fun saveConfig() {
        try {
            configFile.bufferedWriter().use {
                it.write(gson.toJson(ModuleManager.modules))
            }
        } catch (e: IOException) {
            println("Error saving $MOD_NAME config.")
        }
    }
}