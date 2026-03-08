package dev.iievietskyi.coreclans.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CoreMenuHolder implements InventoryHolder {
    private final String menuId;

    public CoreMenuHolder(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuId() {
        return menuId;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}