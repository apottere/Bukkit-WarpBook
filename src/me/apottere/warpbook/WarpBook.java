package me.apottere.warpbook;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Andrew Potter
 */
public class WarpBook extends JavaPlugin {

    private BookListener listener;

    private int createLevelCost;
    private int extraPageLevelCost;

    public WarpBook() {
        doConfig();
        listener = new BookListener(this);
    }

    private void doConfig() {
        FileConfiguration config = getConfig();

        createLevelCost = config.getInt("Creation level cost", 20);
        config.set("Creation level cost", createLevelCost);

        extraPageLevelCost = config.getInt("Extra page level cost", 10);
        config.set("Extra page level cost", extraPageLevelCost);

        this.saveConfig();
    }

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    public void onDisable() {

    }

    public int getCreateLevelCost() {
        return createLevelCost;
    }

    public int getExtraPageLevelCost() {
        return extraPageLevelCost;
    }
}
