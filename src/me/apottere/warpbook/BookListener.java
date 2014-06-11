package me.apottere.warpbook;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;

/**
 * @author Andrew Potter
 */
public class BookListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getItemInHand().getType() == Material.WRITTEN_BOOK ) {

            ItemStack book = player.getItemInHand();

            if (!isBookWarping(book)) {
                if (player.isSneaking() && initializeBook(book)) {
                    event.setCancelled(true);
                    player.closeInventory();
                    player.sendRawMessage("You created a warping book!");
                }

                return;
            }

            BookMeta meta = (BookMeta) book.getItemMeta();
            if (player.isSneaking()) {
                event.setCancelled(true);
                player.closeInventory();

                Location location = player.getLocation();
                String coords = player.getWorld().getName() + ':' + (Math.floor(location.getX()) + 0.5) + ':' + location.getY() + ':' + (Math.floor(location.getZ()) + 0.5) + ':' + location.getYaw();

                meta.setPage(meta.getPageCount(), coords);
                book.setItemMeta(meta);
                player.sendRawMessage("Bound location: " + coords);

            } else {
                event.setCancelled(true);
                player.closeInventory();

                String coords = meta.getPage(meta.getPageCount());
                if(coords == null || coords.equals("")) {
                    return;
                }

                Location location = null;
                try {
                    String worldString = null;
                    World world = null;
                    Double x;
                    Double y;
                    Double z;
                    Float yaw;

                    String[] parts = coords.split(":", 5);

                    worldString = parts[0];
                    world = Bukkit.getServer().getWorld(worldString);

                    x = Double.parseDouble(parts[1]);
                    y = Double.parseDouble(parts[2]);
                    z = Double.parseDouble(parts[3]);
                    yaw = Float.parseFloat(parts[4]);

                    location = new Location(world, x,y,z);
                    location.setYaw(yaw);

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                player.teleport(location);
            }
        }
    }

    private boolean initializeBook(ItemStack book) {
        BookMeta meta = (BookMeta) book.getItemMeta();

        if(meta.getPageCount() > 1 || (meta.getPageCount() == 1 && !meta.getPage(1).equals(""))) {
            return false;
        }

        meta.setLore(Arrays.asList("Warping"));
        if(!meta.hasPages()) {
            meta.addPage("");
        }
        book.setItemMeta(meta);

        return true;
    }

    private boolean isBookWarping(ItemStack book) {
        return book.getItemMeta().hasLore() && book.getItemMeta().getLore().get(0).equals("Warping");
    }
}
