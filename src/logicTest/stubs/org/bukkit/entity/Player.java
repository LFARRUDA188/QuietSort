package org.bukkit.entity;
import org.bukkit.command.CommandSender;
public interface Player extends HumanEntity, CommandSender {
    boolean isOnline();
    void updateInventory();
}
