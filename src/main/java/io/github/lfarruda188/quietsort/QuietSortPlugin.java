package io.github.lfarruda188.quietsort;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class QuietSortPlugin extends JavaPlugin {

    private final Set<UUID> disabled = new HashSet<>();
    private File disabledFile;

    private boolean sortContainers;
    private boolean sortPlayerInventory;
    private boolean sortEnderChest;
    private boolean sortPlayerInventoryOnContainerClose;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        readConfig();
        loadDisabled();
        getServer().getPluginManager().registerEvents(new SortListener(this), this);
        getLogger().info("QuietSort ativo (baus: " + sortContainers
                + ", mochila: " + sortPlayerInventory + ")");
    }

    @Override
    public void onDisable() {
        saveDisabled();
    }

    private void readConfig() {
        FileConfiguration config = getConfig();
        sortContainers = config.getBoolean("sort-containers", true);
        sortPlayerInventory = config.getBoolean("sort-player-inventory", true);
        sortEnderChest = config.getBoolean("sort-ender-chest", true);
        sortPlayerInventoryOnContainerClose =
                config.getBoolean("sort-player-inventory-on-container-close", false);
    }

    public boolean isSortContainers() {
        return sortContainers;
    }

    public boolean isSortPlayerInventory() {
        return sortPlayerInventory;
    }

    public boolean isSortEnderChest() {
        return sortEnderChest;
    }

    public boolean isSortPlayerInventoryOnContainerClose() {
        return sortPlayerInventoryOnContainerClose;
    }

    public boolean isDisabledFor(UUID uuid) {
        return disabled.contains(uuid);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("quietsort.admin")) {
                sender.sendMessage(ChatColor.RED + "Voce nao tem permissao para isso.");
                return true;
            }
            reloadConfig();
            readConfig();
            sender.sendMessage(ChatColor.GREEN + "QuietSort recarregado.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Use /" + label + " reload no console.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (disabled.contains(uuid)) {
            disabled.remove(uuid);
            player.sendMessage(ChatColor.GREEN + "Organizacao automatica LIGADA para voce.");
        } else {
            disabled.add(uuid);
            player.sendMessage(ChatColor.YELLOW + "Organizacao automatica DESLIGADA para voce.");
        }
        saveDisabled();
        return true;
    }

    private void loadDisabled() {
        disabledFile = new File(getDataFolder(), "desativado.yml");
        if (!disabledFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(disabledFile);
        for (String raw : yaml.getStringList("desativado")) {
            try {
                disabled.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                // Linha invalida no arquivo: ignora em vez de quebrar o plugin.
            }
        }
    }

    private void saveDisabled() {
        if (disabledFile == null) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        List<String> raw = new ArrayList<>();
        for (UUID uuid : disabled) {
            raw.add(uuid.toString());
        }
        yaml.set("desativado", raw);
        try {
            if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
                getLogger().warning("Nao foi possivel criar a pasta do plugin.");
                return;
            }
            yaml.save(disabledFile);
        } catch (IOException e) {
            getLogger().warning("Falha ao salvar desativado.yml: " + e.getMessage());
        }
    }
}
