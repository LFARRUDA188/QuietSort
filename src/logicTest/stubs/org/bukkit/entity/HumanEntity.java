package org.bukkit.entity;
import org.bukkit.inventory.PlayerInventory;
public interface HumanEntity {
    PlayerInventory getInventory();
    java.util.UUID getUniqueId();
    void sendMessage(String message);
    boolean hasPermission(String node);
}
