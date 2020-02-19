package red.mohist.xenforologin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;

public class ListenerBlockMultiPlaceEvent implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void OnBlockMultiPlaceEvent(BlockMultiPlaceEvent event){
        if(Main.instance.needcancelled(event.getPlayer())){
            event.setCancelled(true);
        }
    }
}
