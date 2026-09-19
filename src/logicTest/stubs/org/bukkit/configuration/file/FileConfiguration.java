package org.bukkit.configuration.file;
import java.util.List;
public abstract class FileConfiguration {
    public boolean getBoolean(String path, boolean def) { return def; }
    public List<String> getStringList(String path) { return new java.util.ArrayList<>(); }
    public void set(String path, Object value) { }
}
