package me.apottere.warpbook;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Andrew Potter
 */
public class WarpBook extends JavaPlugin {

    private BookListener listener;

    public WarpBook() {
        listener = new BookListener();
    }

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    public void onDisable() {

    }

    public static void main(String[] args) {
        //Do Nothing
    }
}
