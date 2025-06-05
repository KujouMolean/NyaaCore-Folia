package cat.nyaa.nyaacore.utils;

import com.molean.folia.adapter.FoliaRunnable;
import com.molean.folia.adapter.SchedulerContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ClickSelectionUtils {
    private static final Map<UUID, Consumer<Location>> callbackMap = new HashMap<>();
    private static final Map<UUID, FoliaRunnable> timeoutListener = new HashMap<>();

    /**
     * Callback will be invoked if the player right clicked on a block, or the timer goes off.
     *
     * @param player   the player
     * @param timeout  seconds, must positive
     * @param callback if timeout, the parameter will be null
     */
    public static void registerRightClickBlock(UUID player, int timeout, Consumer<Location> callback, Plugin plugin, SchedulerContext context) {
        // force timeout any existing listeners
        Consumer<Location> cb = callbackMap.remove(player);
        FoliaRunnable tl = timeoutListener.remove(player);
        if (cb != null) cb.accept(null);
        if (tl != null) tl.cancel();

        // add new callback
        callbackMap.put(player, callback);
        FoliaRunnable runnable = new FoliaRunnable() {
            @Override
            public void run() {
                if (callbackMap.containsKey(player))
                    callbackMap.remove(player).accept(null);
                timeoutListener.remove(player);
            }
        };
        context.runTaskLater(plugin, runnable, timeout * 20L);
        timeoutListener.put(player, runnable);
    }

    public static class _Listener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onRightClickBlock(PlayerInteractEvent ev) {
            if (callbackMap.containsKey(ev.getPlayer().getUniqueId()) && ev.hasBlock() && ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block b = ev.getClickedBlock();
                Consumer<Location> cb = callbackMap.remove(ev.getPlayer().getUniqueId());
                FoliaRunnable tl = timeoutListener.remove(ev.getPlayer().getUniqueId());

                if (tl == null || !tl.isCancelled()) {
                    cb.accept(b.getLocation());
                    ev.setCancelled(true);
                }
            }
        }
    }
}
