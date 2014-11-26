package com.github.leezallen.GPClaimManager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.github.leezallen.GPClaimManager.GPCMStateManagerClaim.ClaimFlagState;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GPCMCommands implements CommandExecutor {

	private GPClaimManager plugin;

	public GPCMCommands(GPClaimManager plugin) {
		this.plugin = plugin;
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		//First make sure that this command has been sent by a player
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		
		if (label.equalsIgnoreCase("cshowid")) {
			// Does the player have permission to run this command?
			if (player.hasPermission("GPClaimManager.commands.cshowid")) {
				Location location = player.getLocation();
				Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
				
				if (!(claim == null)) {
					//There is a claim on this location. Lets show some details!
					player.sendMessage(ChatColor.DARK_GREEN + "This claim has a ClaimID of " + ChatColor.ITALIC + claim.getID());
					player.sendMessage(ChatColor.DARK_GREEN + "and is owned by " + ChatColor.ITALIC + claim.getOwnerName());
					player.sendMessage(ChatColor.DARK_GREEN + "Currently Loaded Regions: " + ChatColor.ITALIC + plugin.getClaimsStateManager().listtotal());
				} else {
					//No claim here. Lets tell the player that they are mistaken!
					player.sendMessage(ChatColor.RED + "There is no claim at this location!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			}
		} // end of cshowid command
		
		if (label.equalsIgnoreCase("cflag")) {
			// Everyone has the permission to run the cflag command - but we have permissions on separate flags!
			Location location = player.getLocation();
			Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
			/* First make sure that the player is either an owner of the claim or an admin */
			
			if (!(claim == null)) {
				//If we are standing on a claim lets get the details and display them
				String currentClaimID = plugin.GetFormattedClaimID(claim); 
				ClaimFlagState claimState = plugin.getClaimsStateManager().getStateByID(currentClaimID);
				if (args.length == 0) { // No permissions checks for this one!
					// If we do not have any arguments then lets show the current flags.
					player.sendMessage(ChatColor.GREEN + "Showing flags for claim (" + claimState.claimID + ")");
					player.sendMessage(ChatColor.DARK_GREEN + "This claim has a parent of " + claimState.parentClaimID);
					player.sendMessage(ChatColor.DARK_GREEN + "Allow Mob Spawning (doMobSpawning): " + ChatColor.ITALIC + claimState.doMobSpawning);
					player.sendMessage(ChatColor.DARK_GREEN + "Allow PVP (doAllowPVP): " + ChatColor.ITALIC + claimState.doAllowPVP);
					player.sendMessage(ChatColor.DARK_GREEN + "Allow Entry (doAllowEntry): " + ChatColor.ITALIC + claimState.doAllowEntry);
					player.sendMessage(ChatColor.DARK_GREEN + "Entry Text (entrytext): " + ChatColor.ITALIC + claimState.entrytext);
					player.sendMessage(ChatColor.DARK_GREEN + "Exit Text (exittext): " + ChatColor.ITALIC + claimState.exittext);
				} else { // We have arguments, lets make sure they are valid and set them appropriately
					if ((args[0].equalsIgnoreCase("doMobSpawning")) && (player.hasPermission("GPClaimManager.cflag.doMobSpawn"))) {
						claimState.doMobSpawning = !(claimState.doMobSpawning);
						player.sendMessage(ChatColor.GREEN + "Allow Mob Spawning is now "+ claimState.doMobSpawning);
					}
					if ((args[0].equalsIgnoreCase("doAllowEntry")) && (player.hasPermission("GPClaimManager.cflag.doAllowEntry"))){
						claimState.doAllowEntry = !(claimState.doAllowEntry);
						player.sendMessage(ChatColor.GREEN + "Allow Entry is now "+ claimState.doAllowEntry);
					}
					if ((args[0].equalsIgnoreCase("doAllowPVP")) && (player.hasPermission("GPClaimManager.cflag.doAllowPVP"))){
						claimState.doAllowPVP = !(claimState.doAllowPVP);
						player.sendMessage(ChatColor.GREEN + "Allow PVP is now "+ claimState.doAllowPVP);
					}
					if ((args[0].equalsIgnoreCase("EntryText")) && (player.hasPermission("GPClaimManager.cflag.EntryText"))) {
						if (args.length > 1) {
							// get rid of the first argument as we do not need it for the text
							args[0] = null;
							claimState.entrytext = StringUtils.join(args, " ");
							player.sendMessage(ChatColor.GREEN + "EntryText has now been set.");
							
						} else {
							// if we do not have any extra arguments assume we want to wipe the current entry.
							claimState.entrytext = null;
							player.sendMessage(ChatColor.GREEN + "EntryText has now been removed from this claim.");
						}
						// Make sure we save before we return!
						plugin.SaveClaimtoDB(claimState);
						return true;
					}
					if ((args[0].equalsIgnoreCase("ExitText")) && (player.hasPermission("GPClaimManager.cflag.ExitText"))){
						if (args.length > 1) {
							// get rid of the first argument as we do not need it for the text
							args[0] = null;
							claimState.exittext = StringUtils.join(args, " ");
							player.sendMessage(ChatColor.GREEN + "ExitText has now been set.");
							
						} else {
							// if we do not have any extra arguments assume we want to wipe the current entry.
							claimState.exittext = null;
							player.sendMessage(ChatColor.GREEN + "ExitText has now been removed from this claim.");
						}
						//make sure we save before we return!
						plugin.SaveClaimtoDB(claimState);
						return true;
					}
					if ((args[0].equalsIgnoreCase("forget")) && (player.hasPermission("GPClaimManager.cflag.forget"))) {
						player.sendMessage(ChatColor.RED + "Function not yet implemented.");
					}
					plugin.SaveClaimtoDB(claimState);
					
					return true;
				}			
			} else {
				player.sendMessage(ChatColor.RED + "There is no claim at this location!");
			}
		} // end of cflag command
		
		if (label.equalsIgnoreCase("csell")) {
			// Does the player have permission to run this command?
			if (player.hasPermission("GPClaimManager.commands.sell")) {
				CSellClaim(player, 100);
			} else {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			}
		}  // end of command
		
		if (label.equalsIgnoreCase("ccancel")) {
			// Does the player have permission to run this command?
			if (player.hasPermission("GPClaimManager.commands.sell")) {
				CancelRentorSale (player);
			} else {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			}
		}  // end of command
		
		if (label.equalsIgnoreCase("crent")) {
			// we need to look at the arguments to determine if we need to call CRentClaim or CRentCancel
			if (args.length > 0) { // we definitely have arguments to pass on}
				if ((args[0].equalsIgnoreCase("cancel")) && (player.hasPermission("GPClaimManager.commands.buy"))) { //we have asked to cancel renting the current claim.
					CRentCancel(player);
					return true;
				}
				if ((args[0].equalsIgnoreCase("claim")) && (player.hasPermission("GPClaimManager.commands.buy"))) { //we have asked to cancel renting the current claim.
					CRentClaim(player);
					return true;
				}
				if  ((args.length == 2) && (args[0].equalsIgnoreCase("rent")) && (player.hasPermission("GPClaimManager.commands.sell"))) { 
					// if the player has typed /crent rent <amount> then the user wants to rent out the current claim.
					// lets try to convert that second argument to an integer!
					try {
						int rentAmount = Integer.parseInt(args[1]);
						if (rentAmount > 0) {
							// Every thing is good and the value provided is valid.
							CRentOutClaim(player, rentAmount);
							return true;
						} else {
							// Tell the player that they need to have a positive value
							player.sendMessage(ChatColor.GREEN + "You need to provide a valid figure to rent this plot");
							return true;
						}	
					} catch (NumberFormatException e) {
							player.sendMessage(ChatColor.RED + "You have not provided a valid value.");
							player.sendMessage(ChatColor.GREEN + "/crent rent <amount> - Allows the claim to be rented by other players");
							return true;
					}
					
					
				}
			} 
			// if there are no arguments simply show the different use of the command or the user does not have privileges then show the usage/error
			if ((player.hasPermission("GPClaimManager.commands.buy")) || (player.hasPermission("GPClaimManager.commands.sell"))) {
				// as long as the player has at least one of the permissions we can tell them how to use the command...
				player.sendMessage(ChatColor.GREEN + "This command has the following different uses..");
				if (player.hasPermission("GPClaimManager.commands.buy")) player.sendMessage(ChatColor.GREEN + "/crent claim - Rents the claim you are standing in.");
				if (player.hasPermission("GPClaimManager.commands.sell")) player.sendMessage(ChatColor.GREEN + "/crent rent <amount> - Allows the claim to be rented by other players");
				if (player.hasPermission("GPClaimManager.commands.buy")) player.sendMessage(ChatColor.GREEN + "/crent cancel - Stop renting this claim (get your stuff out first!)");
			} else {
				// ...otherwise we better break the bad news!
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			}
				
		}  // end of command
		
		if (label.equalsIgnoreCase("cbuy")) {
			// Does the player have permission to run this command?
			if (player.hasPermission("GPClaimManager.commands.buy")) {
				CBuyClaim(player);
			} else {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			}
		}  // end of command
		
		if (label.equalsIgnoreCase("cevict")) {
			// Does the player have permission to run this command?
			if (player.hasPermission("GPClaimManager.commands.cevict")) {
				CEvictPlayer (player);
			} else {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			}
		}  // end of command
		
		
		/*
		 * The following is the basic structure for a command.... copy and paste and change the <> entries!
		 * 
		if (label.equalsIgnoreCase("<command>")) {
			// Does the player have permission to run this command?
			if (player.hasPermission("GPClaimManager.commands.<permission>")) {
				
			} else {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			}
		}  // end of command
		*/
		
		return true; // We always return true because we handle all the messages above!
	}

	private void CBuyClaim (Player player) {
	//* This script processes a players request to buy the claim that the player is currently standing in.
		player.sendMessage("You have called the BuyClaim function");
	}
	
	private void CSellClaim (Player player, int SaleAmount) {
	//* This script looks at selling the current claim for the specified amount.
		
		// First check the player claim
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null); // Get the claim that the user was standing in when he issued the command. 
		ClaimFlagState claimState = plugin.getClaimsStateManager().getState(claim); // Get the current claim details for this claim.
		
		// We need to handle this differently depending on if this is a parent claim or a subdivided claim.
		
		if (HasClaimPerms(claim, player)) {
			// This player is not the current owner and cannot decide who
			claimState.cmForSale = true;
			claimState.cmForSaleAmount = SaleAmount;
			plugin.SaveClaimtoDB(claimState);
		}
	}
	
	private void CRentOutClaim (Player player, int rentAmount) {
	/* This looks at renting out the claim that the player is currently standing in
	 * and makes sure that the player has the correct permissions and will change the
	 * state of the claim to cmForRent=TRUE and cmForRentAmount = rentAmount.
	 */
		// First check the player claim
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null); // Get the claim that the user was standing in when he issued the command. 
		ClaimFlagState claimState = plugin.getClaimsStateManager().getState(claim); // Get the current claim details for this claim.
		
		// We need to handle this differently depending on if this is a parent claim or a subdivided claim.
		
		if (HasClaimPerms(claim, player)) {
			// This player is not the current owner and cannot decide who
			claimState.cmForRent = true;
			claimState.cmForRentAmount = rentAmount;
			plugin.SaveClaimtoDB(claimState);
		}
	}
	
	private void CRentCancel (Player player) {
	// THis script looks at cancelling the players rent in the current claim
		player.sendMessage("You want to cancel renting this plot");
	}
	
	private void CRentClaim (Player player) {
	// This script looks at renting the current plot to the player.
		player.sendMessage("You want to rent this claim");
	}
	
	private void CEvictPlayer (Player player) {
	/* This evicts all users from the current claim (and puts it back up for sale!)
	 * This looks to make sure that the player is currently the owner of the subdivision
	 * and if they are will...
	 * 1. Clear all existing permissions and managers.
	 * 2. Change the state of cmForRent to true. The rental figure will
	 *    be identical as it will not have changed.
	 */
		player.sendMessage("You cruelly want to evict a player from this claim");
	}
	
	private void CancelRentorSale (Player player) {
		
		/* This cancels the current rent or sale of the current claim.
		 * This will only work if the plot is up and nobody has bought it yet. 
		 * It simply looks at the Claim, checks that the user has permissions 
		 * and then makes sure the states cmForSale and cmForRent are false. 
		 * 
		 */
		player.sendMessage("You want to cancel the rent or sale of this property");

		
		// Cancel the scheduled 
	}
	
	/* =============================================================
	 * The remaining functions are all to do with permissions, 
	 * ownership and economy!
	 * ============================================================= */
	
	private boolean HasClaimPerms (Claim claim, Player player) {
		// There are a number of conditions as to whether or not the player can be considered an owner of a claim.
		// * If the player is the owner of the claim.
		// * If the player has manager permissions for the claim
		// * if the player has GPClaimManager.claimadmin
		
		return ((player.getName().equalsIgnoreCase(claim.getOwnerName())) || (claim.isManager(player.getName())) || (player.hasPermission("GPClaimManager.claimadmins")));
		// If we have passed all of the above tests then we must be allowed to act as an owner.
	}
    
	/* Transferring ownership related tools */
	private boolean TransferClaim (Claim claim, Player player) {
		
		return true;
	}
	
	private boolean TransferSubClaim (Claim claim, Player player) {
		
		// For subclaims we need 
		
		claim.clearPermissions(); //we 
		
		// Edit since the method of clearing managers has changed! 
		// First we need to step through the existing manager list... Then we can get rid of them all
		claim.getManagerList();
		
		return true;
	}
		 
}
