package org.bukkit.inventory;
import org.bukkit.Material;
public class ItemStack implements Cloneable {
    private Material type; private int amount; private String meta;
    public ItemStack(Material type, int amount) { this(type, amount, null); }
    public ItemStack(Material type, int amount, String meta) { this.type=type; this.amount=amount; this.meta=meta; }
    public Material getType() { return type; }
    public int getAmount() { return amount; }
    public void setAmount(int a) { this.amount = a; }
    public int getMaxStackSize() { return type.getMaxStackSize(); }
    public boolean isSimilar(ItemStack o) {
        if (o == null) return false;
        if (o.type != this.type) return false;
        return (meta == null) ? (o.meta == null) : meta.equals(o.meta);
    }
    @Override public ItemStack clone() { return new ItemStack(type, amount, meta); }
    @Override public String toString() { return type + "x" + amount + (meta!=null?("["+meta+"]"):""); }
}
