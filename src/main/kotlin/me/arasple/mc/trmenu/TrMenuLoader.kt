package me.arasple.mc.trmenu

import io.izzel.taboolib.Version
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.util.Files
import io.izzel.taboolib.util.lite.Catchers
import me.arasple.mc.trmenu.display.Menu
import me.arasple.mc.trmenu.modules.configuration.menu.MenuConfiguration
import me.arasple.mc.trmenu.modules.configuration.serialize.MenuSerializer
import me.arasple.mc.trmenu.modules.expression.Expressions
import me.arasple.mc.trmenu.modules.hook.HookHeadDatabase
import me.arasple.mc.trmenu.modules.hook.HookPlayerPoints
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.PlaceholderAPIPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.io.File

/**
 * @author Arasple
 * @date 2020/2/29 17:27
 */
class TrMenuLoader {

    fun init() {
        if (!TrMenu.SETTINGS.getBoolean("Options.Hide-Logo", false)) {
            printLogo()
        }
        TLocale.sendToConsole("PLUGIN.LOADING", Version.getBukkitVersion())
    }

    fun active() {
        HookHeadDatabase.init()
        HookPlayerPoints.init()

        TLocale.sendToConsole("PLUGIN.LOADED", TrMenu.plugin.description.version)

        loadMenus()
    }

    fun cancel() {
        // 注销插件提供的 PlaceholderAPI 变量拓展
        PlaceholderAPI.unregisterExpansion(PlaceholderAPIPlugin.getInstance().expansionManager.getRegisteredExpansion("trmenu"))
        Catchers.getPlayerdata().clear()
    }

    companion object {

        fun loadMenus() = loadMenus(Bukkit.getConsoleSender())

        fun loadMenus(sender: CommandSender) {
            val start = System.currentTimeMillis()
            val folder = setupMenus()
            grabMenuFiles(folder).forEach {
                Menu.getMenus().add(loadMenu(it))
            }
            TrMenu.SETTINGS.getStringList("Load-Menu-Files").forEach { it ->
                val file = File(it)
                if (file.exists()) grabMenuFiles(file).forEach { Menu.getMenus().add(loadMenu(it)) }
            }
            TLocale.sendTo(sender, "LOADER.MENU", Menu.getMenus().size, System.currentTimeMillis() - start)
        }

        fun loadMenu(file: File) =
            MenuSerializer.loadMenu(file.name.removeSuffix(".yml"), MenuConfiguration().let {
                it.load(file)
                return@let it
            })

        fun grabMenuFiles(file: File): List<File> =
            mutableListOf<File>().let { files ->
                if (file.isDirectory) {
                    file.listFiles()?.forEach {
                        files.addAll(grabMenuFiles(it))
                    }
                } else if (!file.name.startsWith("#") && file.name.endsWith(".yml", true)) {
                    files.add(file)
                }
                return@let files
            }

        private fun setupMenus(): File {
            Menu.clearMenus()
            val folder = File(TrMenu.plugin.dataFolder, "menus")
            if (!folder.exists()) {
                Files.releaseResource(TrMenu.plugin, "menus/Example.yml", true)
            }

            return folder
        }

        /**
         * 打印插件的 Logo 字符画到控制台
         */
        private fun printLogo() = arrayOf(
            "§8___________         _____                     __________",
            "§8\\__    ___/______  /     \\   ____   ____  __ _\\______   \\_______  ____",
            "§8   |    |  \\_  __ \\/  \\ /  \\_/ __ \\ /    \\|  |  \\     ___/\\_  __ \\/  _ \\",
            "§8   |    |   |  | \\/    Y    \\  ___/|   |  \\  |  /    |     |  | \\(  <_> )",
            "§8   |____|   |__|  \\____|__  /\\___  >___|  /____/|____|     |__|   \\____/",
            "§8                       \\/     \\/     \\/                               ",
            "                                                      "
        ).let {
            it.forEachIndexed { index, raw ->
                if (raw.isNotBlank()) {
                    val line = raw.toCharArray()
                    val width = (3..6).random()
                    var randomIndex: Int
                    do {
                        randomIndex = (2..line.size - width).random()
                    } while (String(line.copyOfRange(randomIndex, randomIndex + width)).isBlank())
                    val replace = String(line.copyOfRange(randomIndex, randomIndex + width))
                    it[index] = String(line).replaceFirst(replace, "§${arrayOf('9', 'b', '3').random()}$replace§8")
                }
            }
            Bukkit.getConsoleSender().sendMessage(it)
        }

    }


}