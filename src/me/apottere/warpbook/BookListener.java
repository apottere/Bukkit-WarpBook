package me.apottere.warpbook;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * @author Andrew Potter
 */
public class BookListener implements Listener {

    private WarpBook plugin;

    public BookListener(WarpBook plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerScroll(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if(player.isSneaking() && player.getItemInHand().getType() == Material.WRITTEN_BOOK && isBookWarping(player.getItemInHand())) {

            event.setCancelled(true);

            if(event.getPreviousSlot() == event.getNewSlot()) {
                return;
            }

            int direction = getScrollDirection(event);

            ItemStack book = player.getItemInHand();
            BookMeta meta = (BookMeta) book.getItemMeta();
            int page = getSelectedPage(meta);

            page += direction;

            if(page > meta.getPageCount()) {
                page = 1;
            } else if(page < 1) {
                page = meta.getPageCount();
            }

            setSelectedPage(book, page);

            player.sendRawMessage("Scrolled to page " + page + ": " + meta.getPage(page));
        }
    }

    private int getSelectedPage(BookMeta meta) {
        try {
            return Integer.parseInt(meta.getLore().get(1).split(" ")[1]);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse lore for page: \"" + meta.getLore() + "\".", e);
        }
    }

    private void setSelectedPage(ItemStack book, int page) {
        BookMeta meta = (BookMeta) book.getItemMeta();

        List<String> lore = meta.getLore();
        lore.set(1, "Selected: " + page);
        meta.setLore(lore);
        book.setItemMeta(meta);
    }

    private int getScrollDirection(PlayerItemHeldEvent event) {
        int direction = event.getNewSlot() - event.getPreviousSlot();
        if(direction < -1) {
            direction = 1;

        } else if(direction > 1) {
            direction = -1;
        }

        return direction;
    }


    @EventHandler
    public void onPlayerMiddleClick(InventoryClickEvent event) {

        if(event.getClick() == ClickType.MIDDLE &&
                event.getCurrentItem().getType() == Material.WRITTEN_BOOK && isBookWarping(event.getCurrentItem())) {

            event.setCancelled(true);

            Player player = plugin.getServer().getPlayer(event.getWhoClicked().getUniqueId());

            if(!costLevels(player, plugin.getExtraPageLevelCost())) {
                player.sendRawMessage("Not enough levels to add a page, requires " + plugin.getExtraPageLevelCost() + ".");
                return;
            }

            ItemStack book = event.getCurrentItem();
            BookMeta meta = (BookMeta) book.getItemMeta();

            meta.addPage("");
            int pages = meta.getPageCount();
            book.setItemMeta(meta);

            setSelectedPage(book, pages);

            player.sendRawMessage("Created a new blank page!");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getItemInHand().getType() == Material.WRITTEN_BOOK ) {

            ItemStack book = player.getItemInHand();

            if (!isBookWarping(book)) {
                if (player.isSneaking() && initializeBook(player, book)) {
                    event.setCancelled(true);
                    player.closeInventory();
                    player.sendRawMessage("You created a warping book!");
                }

                return;
            }

            BookMeta meta = (BookMeta) book.getItemMeta();

            event.setCancelled(true);
            player.closeInventory();

            if (player.isSneaking()) {

                String coords = serializeLocation(player.getLocation());

                int page = getSelectedPage(meta);
                meta.setPage(page, coords);
                book.setItemMeta(meta);
                player.sendRawMessage("Bound page " + page + ": " + coords);

            } else {

                String coords = meta.getPage(getSelectedPage(meta));
                if(coords == null || coords.equals("")) {
                    return;
                }
                Location location = deserializeLocation(coords);

                for (int i = 0; i < 8; i++) {
                    player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, i, 2);
                }
                player.teleport(location);
                for (int i = 0; i < 8; i++) {
                    player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, i, 2);
                }
            }
        }
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + ':' + (Math.floor(location.getX()) + 0.5) + ':' + location.getY() + ':' + (Math.floor(location.getZ()) + 0.5) + ':' + location.getYaw();
    }

    private Location deserializeLocation(String coords) {
        Location location;

        try {
            String[] parts = coords.split(":");

            String worldString = parts[0];
            World world = Bukkit.getServer().getWorld(worldString);

            Double x = Double.parseDouble(parts[1]);
            Double y = Double.parseDouble(parts[2]);
            Double z = Double.parseDouble(parts[3]);
            Float yaw = Float.parseFloat(parts[4]);

            location = new Location(world, x,y,z);
            location.setYaw(yaw);

        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to deserialize location \"" + coords + "\".", e);
        }
        return location;
    }

    private boolean initializeBook(Player player, ItemStack book) {
        BookMeta meta = (BookMeta) book.getItemMeta();

        if(meta.getPageCount() > 1 || (meta.getPageCount() == 1 && !meta.getPage(1).equals(""))) {
            return false;
        }

        if(!costLevels(player, plugin.getCreateLevelCost())) {
            player.sendRawMessage("Not enough levels to make warp book, requires " + plugin.getCreateLevelCost() + ".");
            return false;
        }

        meta.setLore(Arrays.asList("Warping", "Selected: 1"));
        if(!meta.hasPages()) {
            meta.addPage("");
        }
        book.setItemMeta(meta);

        return true;
    }

    private boolean isBookWarping(ItemStack book) {
        return book.getItemMeta().hasLore() && book.getItemMeta().getLore().get(0).equals("Warping");
    }

    private boolean costLevels(Player player, int levels) {
        if(player.getLevel() >= levels) {
            player.setLevel(player.getLevel() - levels);
            return true;
        }
        return false;
    }
}
