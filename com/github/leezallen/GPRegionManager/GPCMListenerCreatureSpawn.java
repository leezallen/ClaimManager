package com.github.leezallen.GPClaimManager;

import org.bukkit.event.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Location;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import com.github.leezallen.GPClaimManager.GPCMStateManagerClaim.ClaimFlagState;

public class GPCMListenerCreatureSpawn implements Listener{
	
	private GPClaimManager plugin;
		
	public GPCMListenerCreatureSpawn (GPClaimManager plugin) {
		this.plugin = plugin;
	}

	public void registerEvents() {
		// As we have created a class with the plugin method above, the only way I 
		// could get this working is if we implement the listeners here!
		final PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(this, plugin);
		
	}
	
	@EventHandler // For when we implement the NoEntry Flag
    public void onCreatureSpawn(CreatureSpawnEvent event) {
	// A new creature has spawned into the world.. Now we need to check to make sure it is allowed
		Location location = event.getLocation();
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
		if (!(claim == null)) {
			// we are within a claim so we need to check the flags
			ClaimFlagState claimState = plugin.getClaimsStateManager().getState(claim);
			if ((!(claimState.doMobSpawning)) && (location.getBlockY()>plugin.minYStopMobSpawn))  {
				// This claim does not allow spawning so it is time to check that the mob is allowed.
				String creatureName = event.getEntityType().getName();
				if (!(plugin.allowedMobSpawn.contains(creatureName))) {
					// if the creatureName is not in the allowed list cancel the spawn.
					event.setCancelled(true);
				}
			}
		}
		
	}
}
