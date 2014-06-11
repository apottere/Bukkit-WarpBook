package me.apottere.bookwarp;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Andrew Potter
 */
public class BookWarp extends JavaPlugin {

    private BookListener listener;

    public BookWarp() {
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
