package io.github.lfarruda188.quietsort;

import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SortListener implements Listener {

    /** Slots 0-8 sao a hotbar: nunca sao tocados. */
    private static final int HOTBAR_END = 9;
    /** A mochila vai do slot 9 ao 35. */
    private static final int BACKPACK_END = 36;

    private final QuietSortPlugin plugin;

    public SortListener(QuietSortPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player)) {
            return;
        }
        final Player player = (Player) human;

        if (plugin.isDisabledFor(player.getUniqueId())) {
            return;
        }
        if (!player.hasPermission("quietsort.use")) {
            return;
        }

        final Inventory inventory = event.getInventory();
        InventoryType type = inventory.getType();

        if (type == InventoryType.CRAFTING || type == InventoryType.PLAYER) {
            // Fechou o proprio inventario: organiza so a mochila.
            if (plugin.isSortPlayerInventory()) {
                runNextTick(() -> sortBackpack(player));
            }
            return;
        }

        if (!plugin.isSortContainers()) {
            return;
        }
        if (!isSortableContainer(inventory, type)) {
            return;
        }
        // Se outra pessoa ainda esta com o bau aberto, nao mexe (evita desync).
        if (inventory.getViewers().size() > 1) {
            return;
        }

        final boolean alsoBackpack = plugin.isSortPlayerInventoryOnContainerClose();
        runNextTick(() -> {
            sortContainer(inventory);
            if (alsoBackpack) {
                sortBackpack(player);
            }
        });
    }

    /**
     * Mexer no inventario durante o proprio evento de fechar dessincroniza o
     * cliente. Por isso a ordenacao sempre roda no tick seguinte.
     */
    private void runNextTick(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    /**
     * Aceita apenas bau, barril, shulker e ender chest de verdade.
     * Menu de plugin (BedrockGUI, por exemplo) tem holder nulo ou proprio
     * e e ignorado de proposito.
     */
    private boolean isSortableContainer(Inventory inventory, InventoryType type) {
        if (type == InventoryType.ENDER_CHEST) {
            return plugin.isSortEnderChest();
        }
        if (type != InventoryType.CHEST
                && type != InventoryType.BARREL
                && type != InventoryType.SHULKER_BOX) {
            return false;
        }
        InventoryHolder holder = inventory.getHolder();
        return holder instanceof Container || holder instanceof DoubleChest;
    }

    private void sortContainer(Inventory inventory) {
        // Se alguem reabriu o bau nesse meio tempo, deixa quieto.
        if (inventory.getViewers().size() > 0) {
            return;
        }
        ItemStack[] contents = inventory.getStorageContents();
        ItemStack[] sorted = ItemSorter.sortRegion(contents, 0, contents.length);
        if (sorted == null) {
            return;
        }
        inventory.setStorageContents(sorted);
    }

    /** Organiza apenas os slots 9-35. Hotbar, armadura e mao secundaria ficam intactas. */
    private void sortBackpack(Player player) {
        if (!player.isOnline()) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getStorageContents();
        if (contents.length < BACKPACK_END) {
            return;
        }
        ItemStack[] sorted = ItemSorter.sortRegion(contents, HOTBAR_END, BACKPACK_END);
        if (sorted == null) {
            return;
        }
        inventory.setStorageContents(sorted);
        player.updateInventory();
    }
}
