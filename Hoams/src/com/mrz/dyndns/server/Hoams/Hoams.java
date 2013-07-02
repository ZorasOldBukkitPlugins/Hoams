package com.mrz.dyndns.server.Hoams;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.mrz.dyndns.server.CommandSystem.CommandSystem;
import com.mrz.dyndns.server.CommandSystem.SimpleCommand;
import com.mrz.dyndns.server.Hoams.commands.*;
import com.mrz.dyndns.server.Hoams.management.HomeManager;
import com.mrz.dyndns.server.Hoams.management.HomeResult;
import com.mrz.dyndns.server.Hoams.management.LoadFailureType;

public class Hoams extends JavaPlugin {
	
	private CommandSystem cs;
	private HomeManager homeManager;
	
	private boolean useSetHome;
	private boolean goHomeOnDeath;
	
	private GoHomeCommand goHomeCommand;
	private SetHomeCommand setHomeCommand;
	
	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		
		homeManager = new HomeManager(this);
		
		cs = new CommandSystem(this);
		
		goHomeCommand = new GoHomeCommand(this);
		setHomeCommand = new SetHomeCommand(this);
		
		reload();

		//home
		//sethome
		//home set
		//home <player>
		//home set <player>
	}
	
	@Override
	public void onDisable() {
	}
	
	public HomeManager getHomeManager() {
		return homeManager;
	}
	
	public void reload() {
		cs.close();
		PlayerRespawnEvent.getHandlerList().unregister(this);
		reloadConfig();
		
		useSetHome = getConfig().getBoolean("Use_Sethome");
		goHomeOnDeath = getConfig().getBoolean("Go_home_on_death");
		
		cs.registerCommand("home", goHomeCommand);
		cs.registerCommand("home set", setHomeCommand);
		
		if(useSetHome)
			cs.registerCommand("sethome", setHomeCommand);
		
		cs.registerCommand("home reload", new SimpleCommand() {
			@Override
			public boolean Execute(String commandName, CommandSender sender, List<String> args) {
				if(sender.hasPermission("hoams.reload")) {
					reload();
					sender.sendMessage(ChatColor.GREEN + "Homes reloaded");
					if(sender instanceof org.bukkit.entity.Player) {
						getLogger().info("Homes reloaded");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
				}
				return true;
			}
		});
		
		cs.registerCommand("home help", new SimpleCommand() {
			@Override
			public boolean Execute(String commandName, CommandSender sender, List<String> args) {
				if(sender.hasPermission("hoams.help")) {
					sender.sendMessage(ChatColor.YELLOW + "Homes help");
					if(sender.hasPermission("hoams.gohome") || sender.hasPermission("hoams.gohome.self"))
						sender.sendMessage(ChatColor.DARK_AQUA + "/home " + ChatColor.GOLD + "-" + ChatColor.AQUA + " Takes you to your home");
					
					if(sender.hasPermission("hoams.sethome") || sender.hasPermission("hoams.set.self"))
						sender.sendMessage(ChatColor.DARK_AQUA + "/home set " + (useSetHome ? ChatColor.AQUA + "or " + ChatColor.DARK_AQUA + "/sethome " : "") + ChatColor.GOLD + "-" + ChatColor.AQUA + " Sets your home");
					
					if(sender.hasPermission("hoams.gohome.other"))
						sender.sendMessage(ChatColor.DARK_AQUA + "/home [PlayerName] " + ChatColor.GOLD + "-" + ChatColor.AQUA + " Takes you to a player's home");
					
					if(sender.hasPermission("hoams.set.other"))
						sender.sendMessage(ChatColor.DARK_AQUA + "/home set [PlayerName] " + (useSetHome ? ChatColor.AQUA + "or " + ChatColor.DARK_AQUA + "/sethome [PlayerName] " : "") + ChatColor.GOLD + "-" + ChatColor.AQUA + " Sets a player's home");
					
					if(sender.hasPermission("hoams.reload"))
						sender.sendMessage(ChatColor.DARK_AQUA + "/home reload " + ChatColor.GOLD + "-" + ChatColor.AQUA + " Reloads homes file");
					
					if(!(sender instanceof org.bukkit.entity.Player)) {
						sender.sendMessage(ChatColor.RED + "NOTICE: You are not a player so the only command you can use is " + ChatColor.DARK_AQUA + "/home reload" + ChatColor.RED + "!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You don't have permission to do that!"); 
				}
				return true;
			}
		});
		
		if(goHomeOnDeath) {
			this.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onPlayerRespawn(PlayerRespawnEvent event) {
					if(event.getPlayer().hasPermission("hoams.respawnhome")) {
						HomeResult result = homeManager.loadHome(event.getPlayer());
						if(result.getLoadFailureType().equals(LoadFailureType.NONE)){
							event.setRespawnLocation(result.getHome());
							useSetHome = getConfig().getBoolean("Use_Sethome");
							goHomeOnDeath = getConfig().getBoolean("Go_home_on_death");
						}
					}
				}
			}, this);
		}
	}
}
