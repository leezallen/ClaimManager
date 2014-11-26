package com.github.leezallen.GPClaimManager;

import org.bukkit.World;
import org.bukkit.entity.Player;


import java.util.HashMap;
import java.util.Map;

public class GPCMStateManagerPlayer {
	
	//private GPClaimManager plugin;
    private Map<String, PlayerFlagState> states;
	@SuppressWarnings("unused")
	private GPClaimManager plugin;	

    public GPCMStateManagerPlayer(GPClaimManager plugin) {
        this.plugin = plugin;
        states = new HashMap<String, PlayerFlagState>();
    }
    
    public synchronized void forget(Player player) {
        states.remove(player.getName());
    }
    
    public synchronized void forgetAll() {
        states.clear();
    }    
    
    public synchronized PlayerFlagState getState(Player player) {
        PlayerFlagState state = states.get(player.getName());

        if (state == null) {
            state = new PlayerFlagState();
            states.put(player.getName(), state);
        }

        return state;
    }
    
    public static class PlayerFlagState {
        public World lastWorld;
        public String lastRegion;
        public int lastBlockX;
        public int lastBlockY;
        public int lastBlockZ;
    }
}
