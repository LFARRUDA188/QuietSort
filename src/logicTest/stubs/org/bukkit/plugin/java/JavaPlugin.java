package org.bukkit.plugin.java;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
public abstract class JavaPlugin implements Plugin {
    public void onEnable() { }
    public void onDisable() { }
    public void saveDefaultConfig() { }
    public void reloadConfig() { }
    public FileConfiguration getConfig() { return null; }
    public File getDataFolder() { return null; }
    public Logger getLogger() { return Logger.getLogger("stub"); }
    public Server getServer() { return null; }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { return false; }
}
