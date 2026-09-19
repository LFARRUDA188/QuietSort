package org.bukkit.inventory;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
public interface Inventory {
    int getSize();
    ItemStack[] getStorageContents();
    void setStorageContents(ItemStack[] items);
    List<HumanEntity> getViewers();
    InventoryHolder getHolder();
    InventoryType getType();
}
