package cat.nyaa.nyaacore.utils;

import com.molean.folia.adapter.Folia;
import com.molean.folia.adapter.SchedulerContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ConcurrentUtils {
    /**
     * Execute a task asynchronously then execute the callback synchronously
     */
    public static <P, Q> void runAsyncTask(Plugin plugin, P parameter, Function<P, Q> asyncTask, Consumer<Q> callback, SchedulerContext context) {
        Folia.getScheduler().runTaskAsynchronously(plugin, () -> {
            final Q ret = asyncTask.apply(parameter);
            context.runTask(plugin, () -> callback.accept(ret));
        });
    }

    /**
     * @deprecated caller can use {@link org.bukkit.scheduler.BukkitScheduler#runTaskAsynchronously(Plugin, Runnable)} directly
     */
    @Deprecated
    public static <P> void runAsyncTask(Plugin plugin, P parameter, Consumer<P> asyncTask) {
        Folia.getScheduler().runTaskAsynchronously(plugin, () -> {
            asyncTask.accept(parameter);
        });
    }
}
