package com.github.leezallen.GPClaimManager;

import org.bukkit.event.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
//import org.bukkit.event.player.PlayerLoginEvent; //Used to process notifications about rent...
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import com.github.leezallen.GPClaimManager.GPCMStateManagerClaim.ClaimFlagState;
import com.github.leezallen.GPClaimManager.GPCMStateManagerPlayer.PlayerFlagState;

public class GPCMListenerPlayer implements Listener {

	private GPClaimManager plugin;
	
	
	public GPCMListenerPlayer (GPClaimManager plugin) {
		this.plugin = plugin;
	}
	
	public void registerEvents() {
		// As we have created a class with the plugin method above, the only way I 
		// could get this working is if we implement the listeners here!
		final PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(this, plugin);
		
	}
	
	@EventHandler(priority = EventPriority.HIGH) // For when we implement the NoEntry Flag - We need to process movement after all events!
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String currentClaimID = null; // used to store the claimID from the current location.
              
        // We only need to process this if we have actually moved a block!
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
        	
        	
        	// The player has definitely moved so lets load his current state.
        	PlayerFlagState playerState = plugin.getPlayerStateManager().getState(player);
            
        	// We have changed worlds.... forget everything and start again!
        	if (playerState.lastWorld != null && !playerState.lastWorld.equals(world)) {
        		plugin.getPlayerStateManager().forget(player);
        		playerState = plugin.getPlayerStateManager().getState(player);
        	}
        	
        	// Now we need to check the player, get their location and check to see if a claim is present.
        	Location location = player.getLocation();
        	Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
        	// Check to see if we are standing in a claim.
        	if(!(claim==null)) {
        		// We are standing in a claim. Lets retrieve the claimID!
        		currentClaimID = plugin.GetFormattedClaimID(claim);
        		ClaimFlagState claimState = plugin.getClaimsStateManager().getState(claim);
        		// Check to see if the user has moved into a new claim.
        		// Debug line
        		// player.sendMessage("Does " + currentClaimID + "=" + playerState.lastRegion + "(" + (currentClaimID == playerState.lastRegion) + ")");
        		if (!(currentClaimID.equalsIgnoreCase(playerState.lastRegion))) {
        			// If the two are different then the player has moved into a new region.
        			// Before we do the entry into new region first check to see if the user left an old one
        			if (!(playerState.lastRegion == null)) {
        				// Had to break my own rule here! Using GetStatebyID!
        				ClaimFlagState oldClaimState = plugin.getClaimsStateManager().getStateByID(playerState.lastRegion);
        				
        				if ((!(oldClaimState.exittext == null)) && (!oldClaimState.claimID.equalsIgnoreCase(Long.toString(claimState.parentClaimID)))) {
        					      					      				
                			player.sendMessage(ChatColor.GREEN + "*" + oldClaimState.exittext + " *");
        					
                		}
        				if ((!(claimState.entrytext == null)) && (!claimState.claimID.equalsIgnoreCase(Long.toString(oldClaimState.parentClaimID)))) {
        					player.sendMessage(ChatColor.GREEN + "*" + claimState.entrytext + " *");
        				}
        			} else {
        				// Now we can print the welcome message (unless we have entered a claim froma sublaim) 
            			if (!(claimState.entrytext == null)) player.sendMessage(ChatColor.GREEN + "*" + claimState.entrytext + " *");
        			}
        			
        			       				
        			
        			// CODE FOR CHECKING BUYING AND SELLING GOES HERE!
        			if (claimState.cmForRent) {
        				player.sendMessage(ChatColor.YELLOW + "This area is for rent at $" + claimState.cmForRentAmount + "per day.");
        				player.sendMessage(ChatColor.YELLOW + "To rent this area use /crent");
        			}
        			if (claimState.cmForSale) {
        				player.sendMessage(ChatColor.YELLOW + "This area is for sale at $" + claimState.cmForSaleAmount);
        				player.sendMessage(ChatColor.YELLOW + "To rent this area use /cbuy");
        			}
        		}
        		
        	} else {
        		// We are not standing in a claim.
        		currentClaimID = null; // If we are not standing in a claim then the claimID should not exist!
        		
        		//check to see if the user was standing in a claim before.
        		if (!(playerState.lastRegion == null)) {
        			// if they were in a region then send a farewell message, along with a standard wilderness message
        			ClaimFlagState oldClaimState = plugin.getClaimsStateManager().getStateByID(playerState.lastRegion);
        			if (!(oldClaimState.exittext == null)) player.sendMessage(ChatColor.GREEN + "*" + oldClaimState.exittext + " *");
        			player.sendMessage(ChatColor.GREEN + "You are now entering the wilderness...");
        			
        		}
        		
        	}
        		
        	
        // Finally update the players state so that we can check next time.
        // We only need to update if the movement check succeeded (as the data has changed)
        playerState.lastRegion = currentClaimID;
        playerState.lastWorld = event.getTo().getWorld();
        playerState.lastBlockX = event.getTo().getBlockX();
        playerState.lastBlockY = event.getTo().getBlockY();
        playerState.lastBlockZ = event.getTo().getBlockZ();
        } // end of movement check
            
	}
	
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event) {
		// remove this players state from memory. Note that we
		// do not store this state as it is temporary and only
		// used for tracking.
		plugin.forgetPlayer(event.getPlayer());
	}
}
