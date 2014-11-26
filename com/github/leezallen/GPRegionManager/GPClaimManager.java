package com.github.leezallen.GPClaimManager;

import java.util.List;

import me.ryanhamshire.GriefPrevention.Claim;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import com.github.leezallen.GPClaimManager.GPCMCommands;
import com.github.leezallen.GPClaimManager.GPCMStateManagerClaim.ClaimFlagState;
import com.github.leezallen.GPClaimManager.GPCMStateManagerPlayer;
import com.github.leezallen.GPClaimManager.GPCMStateManagerClaim;

public class GPClaimManager extends JavaPlugin{

	public static boolean vaultPresent = false;
    public static Economy econ = null;
    public static Permission perms = null;
    
    // StateManagers
    private GPCMStateManagerPlayer gpcmStateManagerPlayer; 
    private GPCMStateManagerClaim gpcmStateManagerClaim;
    
    // Config files loaded at startup
    public boolean allowMobListener;
    public boolean allowMovementListener;
    public boolean allowMovementEntryListener;
    public boolean allowDamageListener;
    public List<String> allowedMobSpawn;
    public int minYStopMobSpawn;
    
	@Override 
    public void onEnable(){
	
		// First we check that vault is loaded. If not then it is 
		// not worth doing anything else!
		if (checkVault()) {
			/* Vault has been detected */
			getLogger().info("Vault detected and enabled.");
			if (setupEconomy()) {
				getLogger().info("Vault has detected and connected to " + econ.getName());	
			} else {
				getLogger().warning("No compatible economy plugin detected [Vault].");
				getLogger().warning("Disabling plugin.");
				getPluginLoader().disablePlugin(this);
	            return;
			}
		} 
		
		// Load the configuration file!
		this.saveDefaultConfig(); // will save the default file if one is not present
		
		allowMobListener = this.getConfig().getBoolean("ClaimEventManagement.Mobspawn");
	    allowMovementListener = this.getConfig().getBoolean("ClaimEventManagement.Movement");
	    allowMovementEntryListener = this.getConfig().getBoolean("ClaimEventManagement.Entry");
	    allowDamageListener = this.getConfig().getBoolean("ClaimEventManagement.Damage");
	    allowedMobSpawn = this.getConfig().getStringList("AllowedMobs");
	    minYStopMobSpawn = this.getConfig().getInt("MinYStopMobSpawn");
				
	    // Just in case we have changed versions we need to save the defaults back to the main config file!
	    
	    this.getConfig().set("ClaimEventManagement.Mobspawn", allowMobListener);
	    this.getConfig().set("ClaimEventManagement.Movement", allowMovementListener);
	    this.getConfig().set("ClaimEventManagement.Entry", allowMovementEntryListener);
	    this.getConfig().set("ClaimEventManagement.Damage", allowDamageListener);
	    this.getConfig().set("AllowedMobs", allowedMobSpawn);
	    this.getConfig().set("MinYStopMobSpawn", minYStopMobSpawn);
	    
	    this.saveConfig();
		
		// Initalize the StateManagers
		// ---------------------------
		// Used to store all the claim and player data in memory rather
		// than searching the disk everytime!
		gpcmStateManagerPlayer = new GPCMStateManagerPlayer (this);
		gpcmStateManagerClaim = new GPCMStateManagerClaim (this);
		
		
		getCommand("cshowid").setExecutor(new GPCMCommands(this));
		getCommand("cflag").setExecutor(new GPCMCommands(this));
		getCommand("csell").setExecutor(new GPCMCommands(this));
		getCommand("ccancel").setExecutor(new GPCMCommands(this));
		getCommand("crent").setExecutor(new GPCMCommands(this));
		getCommand("cbuy").setExecutor(new GPCMCommands(this));
		getCommand("cevict").setExecutor(new GPCMCommands(this));
		
		// Register events
		if (allowMovementListener) {
			(new GPCMListenerPlayer(this)).registerEvents();
			getLogger().info("Movement Listener has been enabled.");
		}
		if (allowMobListener) {
			(new GPCMListenerCreatureSpawn(this)).registerEvents();
			getLogger().info("Mob Spawner has been enabled.");
		}
		if (allowMovementEntryListener) {
			getLogger().info("Entry listener has not yet been implemented.");
		}
		if (allowDamageListener) {
			getLogger().info("Damage listener has not yet been implemented.");
		}
		
		getLogger().info("V" + this.getDescription().getVersion() + " Enabled!");
		
	}
	
	public void onDisable(){
		
		gpcmStateManagerClaim.SaveAlltoDB();
		getLogger().info("V" + this.getDescription().getVersion() + " Disabled!");
		// and make sure everything is saved!!!!
	}
	

    private boolean checkVault() {
   
    	vaultPresent = !(getServer().getPluginManager().getPlugin("Vault") == null); 
        return vaultPresent;

    }
	
	
	private boolean setupEconomy() {

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

   
    //Following functions are all related to the GPCMStateManagerPlayer.
    public GPCMStateManagerPlayer getPlayerStateManager () {
    	return gpcmStateManagerPlayer;
    }
    
    public void forgetPlayer(Player player) {
    	gpcmStateManagerPlayer.forget(player);
    }
    
    public GPCMStateManagerClaim getClaimsStateManager() {
    	return gpcmStateManagerClaim;
    }
    
    public void forgetClaim(Claim claim) {
    	gpcmStateManagerClaim.forget(claim);
    }
    
    public int listPlayerClaim(Player player) {
    	return gpcmStateManagerClaim.listClaims(player);
    }
    
    // We are using this while subdividedclaims have no claim ID. 
    public String GetFormattedClaimID (Claim claim) {
    	return gpcmStateManagerClaim.GetFormattedClaimID(claim);
    }
    
    public boolean SaveClaimtoDB(ClaimFlagState state) {
    	return gpcmStateManagerClaim.SaveClaimtoDB(state);
    }

	
	/* Economy based tools */
	
	public boolean TransferMoney (Player fromPlayer, Player toPlayer, Long amount) {
		return true;
	}
}
	
