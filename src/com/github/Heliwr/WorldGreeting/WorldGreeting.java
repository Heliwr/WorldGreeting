package com.github.Heliwr.WorldGreeting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldGreeting extends JavaPlugin implements Listener {
	public static final Logger logger = Logger.getLogger("Minecraft.WorldGreeter");
    static Set greetings = new LinkedHashSet();
    
    public void onDisable() {
    	PluginDescriptionFile pdfFile = this.getDescription();
		logger.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!" );
    }

    public void onEnable() {
        try {
            checkConfig();
            loadConfig();
        }
        catch(FileNotFoundException ex) {
            logger.log(Level.SEVERE, "WorldGreeting: No config file found!", ex);
            return;
        }
        catch(IOException ex) {
            logger.log(Level.SEVERE, "WorldGreeting: Error while reading config!", ex);
            return;
        }
        catch(InvalidConfigurationException ex) {
            logger.log(Level.SEVERE, "WorldGreeting: Error while parsing config!", ex);
            return;
        }
        
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
		logger.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }

	
    private void checkConfig() throws FileNotFoundException, IOException, InvalidConfigurationException {
    	File f = new File(this.getDataFolder(), "config.yml");
    	YamlConfiguration conf = new YamlConfiguration();
    	if(!f.exists()) {
    		File confFile = new File(this.getDataFolder(), "config.yml");
    		conf.set("greetings.worlds.world", "Welcome to world.");
    		conf.set("greetings.worlds.world_nether", "Welcome to the nether.");
    		conf.save(confFile);
    	}
    }

    private void loadConfig() throws FileNotFoundException, IOException, InvalidConfigurationException {
    	File f = new File(this.getDataFolder(), "config.yml");
    	YamlConfiguration conf = new YamlConfiguration();
    	conf.load(f);
    	greetings.clear();
    	greetings = conf.getConfigurationSection("greetings.worlds").getKeys(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args ) {
    	String cmdname = cmd.getName().toLowerCase();
        Player player = null;
        if (sender instanceof Player) {
        	player = (Player)sender;
        }
        
        if (cmdname.equals("worldgreeting") && args.length > 0) {
        	if (player == null || player.isOp() || player.hasPermission("worldgreeting.admin")) {
	        	if (args[0].equalsIgnoreCase("reload")) {
	        		if (player != null) {
	        			player.sendMessage("[WorldGreeting] Reloading configuration.");
		        		logger.info("[WorldGreeting] Reloading configuration by " + player.getName());
	        		} else {
		        		logger.info("[WorldGreeting] Reloading configuration from server console");
	        		}
	        	}
	        	this.reloadConfig();
	        	
	        	try {
	                checkConfig();
	                loadConfig();
	            }
	            catch(FileNotFoundException ex) {
	                logger.log(Level.SEVERE, "WorldGreeting: No config file found!", ex);
	                return false;
	            }
	            catch(IOException ex) {
	                logger.log(Level.SEVERE, "WorldGreeting: Error while reading config!", ex);
	                return false;
	            }
	            catch(InvalidConfigurationException ex) {
	                logger.log(Level.SEVERE, "WorldGreeting: Error while parsing config!", ex);
	                return false;
	            }
        	} else {
        		logger.info("[WorldGreeting] Command access denied for " + player.getName());
        	}
    		return true;
        }
        return false;
    }
    
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event){
    	if(greetings.contains(event.getPlayer().getWorld().getName())) {
    		event.getPlayer().sendMessage(ChatColor.GOLD + this.getConfig().getString("greetings.worlds." + event.getPlayer().getWorld().getName()));
    	}
    }
}
