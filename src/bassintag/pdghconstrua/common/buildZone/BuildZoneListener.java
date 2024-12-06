package bassintag.pdghconstrua.common.buildZone;

import bassintag.pdghconstrua.common.BuildMyThing;
import bassintag.pdghconstrua.common.ChatUtil;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;

public class BuildZoneListener
  implements Listener
{
  private BuildMyThing instance;

  public BuildZoneListener(BuildMyThing instance)
  {
    this.instance = instance;
  }

  @EventHandler
  public void onPlayerLogOut(PlayerQuitEvent event) {
      this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).leave(event.getPlayer());
  }

@EventHandler
  public void onPlayerPlaceBlock(BlockPlaceEvent event) {
    if (event.getPlayer().hasMetadata("inbmt")) {
      if ((this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()) != null) && 
        (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).getBuilder().getName() == event.getPlayer().getName()) && 
        (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).getBuildZone().contains(event.getBlock()))) {
        //event.getPlayer().getInventory().addItem(new ItemStack[] { new ItemStack(event.getBlockPlaced().getType(), 1, event.getBlockPlaced().getData()) });
        //event.getBlockPlaced().setType(event.getBlockPlaced().getType());
        return;
      }

      event.setCancelled(true);
    }
  }

  @SuppressWarnings("deprecation")
@EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
      if ((event.getPlayer().hasMetadata("inbmt")) && 
        (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()) != null) && 
        (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).getBuilder() != null) && 
        (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).getBuilder().getName() == event.getPlayer().getName()) && 
        (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).getBuildZone().contains(event.getClickedBlock()))) {
        event.getClickedBlock().setType(Material.AIR);
      }

    }
    else if ((event.getPlayer().hasMetadata("inbmt")) && 
      (event.getPlayer().getItemInHand().getTypeId() >= 256))
      event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerBreakBlock(BlockBreakEvent event) {
      event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
      event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerHit(EntityDamageEvent event) {
        event.setCancelled(true);
    }

  @EventHandler
  public void onPlayerHungerChange(FoodLevelChangeEvent event) {
      event.setCancelled(true);
  }

	
	public boolean isStarted(){
		return this.instance.started;
	}
	
  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
	  if(!this.instance.isStarted()){
	        event.setCancelled(true);
          this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).sendMessage("§7"+event.getPlayer().getName() + "§f: "+ event.getMessage().toLowerCase());
	  }
    if ((this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()) != null) && 
      (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).instance.isStarted()))
      if (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).getBuilder().getName() == event.getPlayer().getName()) {
        ChatUtil.send(event.getPlayer(), "Você não pode conversar enquanto constroi");
        event.setCancelled(true);
      } else {
        event.setCancelled(true);
        String word = this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).getWord();
        if (this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).hasFound(event.getPlayer()))
          ChatUtil.send(event.getPlayer(), "Você já acertou a palavra");
        else if (event.getMessage().toLowerCase().contains(word))
          this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).wordFoundBy(event.getPlayer());
        else
          this.instance.getRoomByName(((MetadataValue)event.getPlayer().getMetadata("inbmt").get(0)).asString()).sendMessage("§7"+event.getPlayer().getName() + "§f: "+ event.getMessage().toLowerCase());
      }
  }
}