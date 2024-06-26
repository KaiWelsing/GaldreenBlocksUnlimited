package events;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import commands.AddTool;
import de.ewu2000.galdreenblocksunlimited.CustomBlock;
import de.ewu2000.galdreenblocksunlimited.CustomBlockCompound;
import de.ewu2000.galdreenblocksunlimited.CustomBlockCycle;
import de.ewu2000.galdreenblocksunlimited.GaldreenBlocksUnlimited;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.plugin.RegisteredListener;

public class PlayerInteractEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event){

        //check if worldguard regions allow for building.
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(event.getClickedBlock().getLocation());
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(event.getPlayer().getWorld());
        boolean wgResult = query.testState(loc, WorldGuardPlugin.inst().wrapPlayer(event.getPlayer()), Flags.BUILD);
        boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(WorldGuardPlugin.inst().wrapPlayer(event.getPlayer()), world);

        //check if towny allows building for this player
        boolean bBuild = PlayerCacheUtil.getCachePermission(event.getPlayer(), event.getClickedBlock().getLocation(), event.getClickedBlock().getType(), TownyPermission.ActionType.BUILD);

        if ((wgResult && bBuild) || canBypass) {
            if (event.getAction().isRightClick() && event.getClickedBlock() != null && !event.getPlayer().isSneaking()) {
                if (event.useInteractedBlock() != Event.Result.DENY) { //Not denied by plot plugin
                    for (CustomBlockCompound cbcmp : GaldreenBlocksUnlimited.allCustomBlockCompounds) {
                        for (CustomBlockCycle cbc : cbcmp.getBlockCyclesList()) {
                            int i = 0;
                            if (cbc.getCustomBlocks().size() > 1) {
                                for (CustomBlock cb : cbc.getCustomBlocks()) {
                                    if (event.getClickedBlock().getBlockData().equals(cb.getGoalData())) {
                                        if (event.getPlayer().getInventory().getItemInMainHand().isSimilar(AddTool.tool)) {
                                            if (event.getHand() == EquipmentSlot.HAND) {
                                                if (i == cbc.getCustomBlocks().size() - 1) {
                                                    i = 0;
                                                } else {
                                                    i++;
                                                }
                                                event.getClickedBlock().setBlockData(cbc.getCustomBlocks().get(i).getGoalData(), false);
                                            }
                                            event.setCancelled(true);
                                        } else if (event.getClickedBlock().getType().isInteractable()) {
                                            if(!event.getPlayer().isSneaking()){
                                                event.setCancelled(true);
                                            }
                                        }
                                        return;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
