package org.bukkit;
public enum Material {
    AIR(0,false,false), STONE(64,true,false), OAK_LOG(64,true,false), DIRT(64,true,false),
    COBBLESTONE(64,true,false), DIAMOND_SWORD(1,false,false), IRON_PICKAXE(1,false,false),
    IRON_HELMET(1,false,false), DIAMOND_CHESTPLATE(1,false,false), BREAD(64,false,true),
    COOKED_BEEF(64,false,true), POTION(1,false,false), STICK(64,false,false),
    DIAMOND(64,false,false), IRON_INGOT(64,false,false), ELYTRA(1,false,false),
    BOW(1,false,false), TRIDENT(1,false,false), IRON_SPEAR(1,false,false), SHIELD(1,false,false);
    private final int max; private final boolean block; private final boolean edible;
    Material(int max, boolean block, boolean edible) { this.max=max; this.block=block; this.edible=edible; }
    public boolean isBlock() { return block; }
    public boolean isEdible() { return edible; }
    public int getMaxStackSize() { return max; }
}
