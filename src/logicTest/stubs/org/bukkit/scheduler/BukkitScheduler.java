package org.bukkit.scheduler;
import org.bukkit.plugin.Plugin;
public interface BukkitScheduler { Object runTask(Plugin plugin, Runnable task); }
