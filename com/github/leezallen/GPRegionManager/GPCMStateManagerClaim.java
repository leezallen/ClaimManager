package com.github.leezallen.GPClaimManager;

import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GPCMStateManagerClaim {
	
	private Map<String , ClaimFlagState> states;
	private GPClaimManager plugin;
	
	//These variables are used for the physical storage
	private FileConfiguration claimFileConfig = null;
	private File claimFile = null;

    public GPCMStateManagerClaim(GPClaimManager plugin) {
        this.plugin = plugin;
        states = new HashMap<String, ClaimFlagState>();
        
        if (claimFile == null) InitialiseDB();
    }
    
    private void InitialiseDB() {
        //lets initialise the filestore for this instance.
        claimFile = new File(plugin.getDataFolder(), "ClaimsData.yml");
        claimFileConfig = YamlConfiguration.loadConfiguration(claimFile);
        ClaimFlagState claimState;
        plugin.getLogger().info("Connected to ClaimsData.yml");
        try { 
        	claimFileConfig.save(claimFile); // If a file does not exist this will create an empty one. If one does exist then no harm done!
        } catch (IOException e) { 
        	plugin.getLogger().severe("Cannot access claims file: " + e);
        	plugin.getPluginLoader().disablePlugin(plugin);
        	return;
        } 
    
       // Finally, we are going to try and load all the existing hashmaps from the database.
        
        
        
        // Time to load all the existing claims into the database!
        Set<String> claimsInFile = claimFileConfig.getKeys(false);
        plugin.getLogger().info("Claims file has " + claimsInFile.size() + " claims.");
        
        // Now to try and load all the configuration details...
        
        Iterator<String> it = claimsInFile.iterator();
        
        while (it.hasNext()) {
        	String value= it.next();
        	//plugin.getLogger().info("Loading claim: " + value);
        	
        	// Why do we do it this way... Simply because I do not trust the ability
        	// for configurationsections to work properly. And because we only need 
        	// to save changes in the database in the first place!
        	// Plus we can afford to be a little inefficent because this only happens
        	// when the server starts.... better to be safe than sorry!
        	
        	claimState = getStateByID(value); // This will create the claim state if needed!
        	claimState.claimID = claimFileConfig.getString(value + ".ClaimID"); // always present so no need to check!
        	claimState.parentClaimID = claimFileConfig.getLong(value + ".ParentClaimID"); // always present so no need to check!
        	claimState.doMobSpawning = claimFileConfig.getBoolean(value + ".doMobSpawning");
        	claimState.doAllowPVP = claimFileConfig.getBoolean(value + ".doAllowPVP");
           	claimState.doAllowEntry = claimFileConfig.getBoolean(value + ".doAllowEntry");
           	// The next two strings are not present unless set by /cflag
           	if (claimFileConfig.isString(value + ".entrytext")) claimState.entrytext = claimFileConfig.getString(value + ".entrytext"); 
           	if (claimFileConfig.isString(value + ".exittext")) claimState.exittext = claimFileConfig.getString(value + ".exittext");
           	claimState.cmForSale = claimFileConfig.getBoolean(value + ".cmForSale");
           	claimState.cmForSaleAmount = claimFileConfig.getInt(value + ".cmForSaleAmount");
           	claimState.cmForRent = claimFileConfig.getBoolean(value + ".cmForRent");
           	claimState.cmForRentAmount = claimFileConfig.getInt(value + ".cmForRentAmount");
        } // end of loop - all claims loaded
    }

    public Boolean SaveClaimtoDB(ClaimFlagState state) {
        	
    	plugin.getLogger().info("Saving Claim Details to Database for " + state.claimID);
    	claimFileConfig.set(state.claimID , null);
    	claimFileConfig.set(state.claimID + ".ClaimID", state.claimID);
    	claimFileConfig.set(state.claimID + ".ParentClaimID", state.parentClaimID);
    	claimFileConfig.set(state.claimID + ".doMobSpawning", state.doMobSpawning);
    	claimFileConfig.set(state.claimID + ".doAllowPVP", state.doAllowPVP);
    	claimFileConfig.set(state.claimID + ".doAllowEntry", state.doAllowEntry);
    	claimFileConfig.set(state.claimID + ".entrytext", state.entrytext);
    	claimFileConfig.set(state.claimID + ".exittext", state.exittext);
    	claimFileConfig.set(state.claimID + ".cmForSale", state.cmForSale);
    	claimFileConfig.set(state.claimID + ".cmForSaleAmount", state.cmForSaleAmount);
    	claimFileConfig.set(state.claimID + ".cmForRent", state.cmForRent);
    	claimFileConfig.set(state.claimID + ".cmForRentAmount", state.cmForRentAmount);
        	
        try { 
        	claimFileConfig.save(claimFile); 
        	return true;
        } catch (IOException e) { 
        	plugin.getLogger().severe("Cannot access claims file: " + e);
        	plugin.getPluginLoader().disablePlugin(plugin);
        	return false;
        } 
    }
    
    public void SaveAlltoDB() {
    	@SuppressWarnings("rawtypes")
		Iterator it = states.entrySet().iterator();
    	while (it.hasNext()) {
    		@SuppressWarnings("rawtypes")
			Map.Entry stateMap = (Map.Entry)it.next();
    		ClaimFlagState state = getStateByID(stateMap.getKey().toString());
    		SaveClaimtoDB(state);
    	}
  	
    }
    
    public synchronized void forget(Claim claim) {
        // Completely forgets claim flags ... Resets to defaults!
    	String claimID = GetFormattedClaimID(claim);
    	
    	states.remove(claimID);
    	// and remove it from the file!
    	claimFileConfig.set(claimID, null);
    	
    }
    
    public synchronized void forgetAll() {
        states.clear();
    }
    
    public int listtotal () {
    	return states.size();
    }
    
    public int listClaims (Player player){
    	// List the number of claims currently owned by Player
    	return 0;
    }
    
    
    // This procedure is used to build a temporary claim ID that can be used for subclaims.
    public String GetFormattedClaimID (Claim claim) {
    	String buildClaimID = null;
    	
    	if (claim.getID() != null) {
    		buildClaimID = claim.getID().toString();
    		// If this is not a child domain we can simply return the parent ID
    	} else {
    		// This is a child domain so we are doing things a bit differently!
    		// we will be using the parent claim and greater boundaries. This obviously
    		// does not work very well if boundaries are moved.
    		buildClaimID = claim.parent.getID().toString();
    		buildClaimID = buildClaimID + "X" + claim.getGreaterBoundaryCorner().getBlockX();
    		//buildClaimID = buildClaimID + "Y" + claim.getGreaterBoundaryCorner().getBlockY(); // removed - we do not care about the height!
    		buildClaimID = buildClaimID + "Z" + claim.getGreaterBoundaryCorner().getBlockZ(); 
    	}
    	return buildClaimID;
    }
    
    public synchronized ClaimFlagState getState(Claim claim) {
        
    	String getClaimID = GetFormattedClaimID(claim);
    	ClaimFlagState state = states.get(getClaimID);

        if (state == null) {
            state = new ClaimFlagState();
            state.claimID = getClaimID;
            if (claim.parent != null) {
            	// This means that the claim has a parent and we need to make sure we save the parent ID for cascading flags!
            	state.parentClaimID = claim.parent.getID();
            }
            plugin.getLogger().info("Building new claim for " + state.claimID);
            states.put(getClaimID, state);
        }

        return state;
    }
    
    public synchronized ClaimFlagState getStateByID(String claimID) {
        
    	// This procedure is only for use when loading and saving from the database and
    	// using a preformatted string - It should never be used under any other circumstance!
    	// It can only be used if you are pre-filling all the other entries correctly OR
    	// if you are 100% sure that the claim already exists!!!! If not sure do not use!
    	ClaimFlagState state = states.get(claimID);

        if (state == null) {
            state = new ClaimFlagState();
            state.claimID = claimID;
            states.put(claimID, state);
        }

        return state;
    }
    
    public static class ClaimFlagState {
    	public String claimID = null; // I know it is weird to store it here too, but it is necessary.... - At least until children return valid ClaimID's
    	public long parentClaimID = -1; // This will be used to identify parents when cascading flags to child claims.
    	public boolean doMobSpawning = true;
    	public boolean doAllowPVP = true;
    	public boolean doAllowEntry = true;
    	public String entrytext = null;
    	public String exittext = null; 
    	public boolean cmForSale = false;
    	public long cmForSaleAmount = 0;
    	public boolean cmForRent = false;
    	public long cmForRentAmount = 0;
    }
}