package me.drunkenmeows.bocsvotelistener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class BOCSVoteListener extends JavaPlugin 
	implements Listener {
	
	public HashMap<String, Integer> enchantlimits = new HashMap<String, Integer>();
	public HashMap<String, ArrayList<ItemStack>> bufferedplayers = new HashMap<String, ArrayList<ItemStack>>();
	//public HashMap<String, ArrayList<String>> votedplayers = new HashMap<String, ArrayList<String>>();
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public ArrayList<String> enchantnames = new ArrayList<String>();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(bufferedplayers.containsKey(p.getName()) && p.getWorld().getName().equalsIgnoreCase("empire") )
		{
			HashMap<Integer, ItemStack> leftItems = new HashMap<Integer,ItemStack>();
			//get list of buffered items for player
			ArrayList<ItemStack> items = bufferedplayers.get(p.getName());
			//for all items buffer
			for(ItemStack i: items)
			{
				//give item to player
				leftItems = p.getInventory().addItem(i);
				//if items leftover drop near player
				if(!leftItems.isEmpty())
				{
					for(ItemStack li: leftItems.values())
						p.getWorld().dropItemNaturally(p.getLocation(), li);
				}
			}
			bufferedplayers.remove(p.getName());
		}
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player p = event.getPlayer();
		if(bufferedplayers.containsKey(p.getName()) && p.getWorld().getName().equalsIgnoreCase("pve") )
		{
			HashMap<Integer, ItemStack> leftItems = new HashMap<Integer,ItemStack>();
			//get list of buffered items for player
			ArrayList<ItemStack> items = bufferedplayers.get(p.getName());
			//for all items buffer
			for(ItemStack i: items)
			{
				//give item to player
				leftItems = p.getInventory().addItem(i);
				//if items leftover drop near player
				if(!leftItems.isEmpty())
				{
					for(ItemStack li: leftItems.values())
						p.getWorld().dropItemNaturally(p.getLocation(), li);
				}
			}
			bufferedplayers.remove(p.getName());
		}
	}
	
	@EventHandler
    public void onVotifierEvent(VotifierEvent event) {
		//get vote
		Vote vote = event.getVote();
		//create & initialise leftover items
        HashMap<Integer, ItemStack> leftItems = new HashMap<Integer,ItemStack>();
        //Item to get player
        ItemStack item = new ItemStack(403, 1, (short) 0);
        //enchantment info 
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
        
        int enchantchoice = new Random().nextInt(21);
		String enchant = enchantnames.get(enchantchoice);
		
		//get level for enchantment
		int limit = enchantlimits.get(enchant);
		int level;
		if (limit > 1)
			level = new Random().nextInt((limit-1))+1;
		else
			level = 1;
		//add enchantment
		meta.addStoredEnchant(Enchantment.getByName(enchant), level, false);
    	item.setItemMeta(meta);
        //player
        Player p = getServer().getPlayer(vote.getUsername());  
        
        getServer().broadcastMessage(c("&f[&6B&7O&6C&7S&f]: "+vote.getUsername()+" voted at: "+vote.getServiceName()));
        
        /*if(votedplayers.containsKey(vote.getUsername()))
    	{
    		votedplayers.get(vote.getUsername()).add(enchant+" "+level);
    	} else {
    		ArrayList<String> reward = new ArrayList<String>();
    		reward.add(enchant+" "+level);
    		votedplayers.put(vote.getUsername(), reward);
    	}*/
        
        //if player exsist and is online
        if(p!=null && p.isOnline())	{
        	//log reward given
    		this.logger.info("[BOCSVL] " + vote.getUsername() + " received an enchanted " + enchant + " " + level + " book.");
    		           		
        	//give item to player
        	leftItems = p.getInventory().addItem(item);
        	
        	//broadcast to server - left everyone know about it!
    		getServer().broadcastMessage(c("&f[&6B&7O&6C&7S&f]: "+vote.getUsername()+" received an Enchanted book!"));
    		
    		//if player inventory is full, drop items
        	if(!leftItems.isEmpty())
        	{
        		for(ItemStack is:leftItems.values())
        			p.getWorld().dropItemNaturally(p.getLocation(), is);       		
        	}	
        } else {
        	OfflinePlayer offp = this.getServer().getOfflinePlayer(vote.getUsername());
        	
        	if(offp != null) {
        		getServer().broadcastMessage(c("&f[&6B&7O&6C&7S&f]: "+vote.getUsername()+" offline. Saving their Enchanted book for later!"));
        		//add item to list
        		if(bufferedplayers.containsKey(offp.getName()))
        		{
        			bufferedplayers.get(offp.getName()).add(item);
        		} else {
        			ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        			items.add(item);
        			bufferedplayers.put(offp.getName(), items);
        		}
        	}
        }
	}
	
	//public synchronized void addtolist
	
	@Override
	public void onEnable() {
		initalise();
		getServer().getPluginManager().registerEvents(this,this);
	}
	
	@Override
	public void onDisable() {
	}
	
	public void initalise() {
		
		//setup enchantments and limits
		enchantlimits.put("DAMAGE_ALL", 5);		
		enchantlimits.put("DAMAGE_ARTHROPODS", 5);
		enchantlimits.put("DAMAGE_UNDEAD", 5);
		enchantlimits.put("DIG_SPEED", 5);
		enchantlimits.put("DURABILITY", 3);
		enchantlimits.put("FIRE_ASPECT", 2);
		enchantlimits.put("KNOCKBACK", 2);
		enchantlimits.put("LOOT_BONUS_BLOCKS", 3);
		enchantlimits.put("LOOT_BONUS_MOBS", 3);
		enchantlimits.put("OXYGEN", 3);
		enchantlimits.put("PROTECTION_ENVIRONMENTAL", 4);
		enchantlimits.put("PROTECTION_EXPLOSIONS", 4);
		enchantlimits.put("PROTECTION_FALL", 4);
		enchantlimits.put("PROTECTION_FIRE", 4);
		enchantlimits.put("PROTECTION_PROJECTILE", 4);
		enchantlimits.put("SILK_TOUCH", 1);
		enchantlimits.put("WATER_WORKER", 1);
		enchantlimits.put("ARROW_FIRE", 1);
		enchantlimits.put("ARROW_DAMAGE", 5);
		enchantlimits.put("ARROW_KNOCKBACK", 1);
		enchantlimits.put("ARROW_INFINITE", 1);
		
		
		enchantnames.add("DAMAGE_ALL");		
		enchantnames.add("DAMAGE_ARTHROPODS");
		enchantnames.add("DAMAGE_UNDEAD");
		enchantnames.add("DIG_SPEED");
		enchantnames.add("DURABILITY");
		enchantnames.add("FIRE_ASPECT");
		enchantnames.add("KNOCKBACK");
		enchantnames.add("LOOT_BONUS_BLOCKS");
		enchantnames.add("LOOT_BONUS_MOBS");
		enchantnames.add("OXYGEN");
		enchantnames.add("PROTECTION_ENVIRONMENTAL");
		enchantnames.add("PROTECTION_EXPLOSIONS");
		enchantnames.add("PROTECTION_FALL");
		enchantnames.add("PROTECTION_FIRE");
		enchantnames.add("PROTECTION_PROJECTILE");
		enchantnames.add("SILK_TOUCH");
		enchantnames.add("WATER_WORKER");
		enchantnames.add("ARROW_FIRE");
		enchantnames.add("ARROW_DAMAGE");
		enchantnames.add("ARROW_KNOCKBACK");
		enchantnames.add("ARROW_INFINITE");
		
		return;
	}
	
	//colourise text
	public String c(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)	{	
		if(cmd.getLabel().equals("bocsvltest"))
		{
			if(args.length > 0)
			{
				String name = args[0];
				Vote vote = new Vote();
				vote.setAddress("http://www.planetminecraft.com");
				vote.setUsername(name);
				vote.setServiceName("planetminecraft.com");
				vote.setTimeStamp("12:00pm");
				VotifierEvent event = new VotifierEvent(vote);
				
				this.onVotifierEvent(event);
				
			} else {
				if(sender instanceof Player) {
					Player p = (Player)sender;
					
					Vote vote = new Vote();
					vote.setAddress("http://www.planetminecraft.com");
					vote.setUsername(p.getName());
					vote.setServiceName("planetminecraft.com");
					vote.setTimeStamp("12:00pm");
					VotifierEvent event = new VotifierEvent(vote);
					this.onVotifierEvent(event);
						
					return true;
				}
			}
		}
		return true;
	}
}

/*//Vote vote = event.getVote();
HashMap<Integer, ItemStack> leftItems = new HashMap<Integer,ItemStack>();
ItemStack item = new ItemStack(403, 1, (short) 0);
EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
//Player p = plugin.getServer().getPlayer(vote.getUsername());

//excute a command
//plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "/give "+p.getName()+" enchantedbook 1 digspeed:3");

if(p!=null && p.isOnline())
{
	//get reward
	//item = getReward(item);
	
	int enchantchoice = new Random().nextInt(21);
	String enchant = this.enchantnames.get(enchantchoice);
	
	//get level for enchantment
	int limit = this.enchantlimits.get(enchant);
	
	int level;
	if (limit > 1)
		level = new Random().nextInt((limit-1))+1;
	else
		level = 1;
	
	this.getServer().broadcastMessage("Enchant:"+enchant);
	this.getServer().broadcastMessage("Limit:"+limit);
	this.getServer().broadcastMessage("Level:"+level);
	
	//add enchantment
	//item.addEnchantment(Enchantment.getByName(enchant), level);
	meta.addStoredEnchant(Enchantment.getByName(enchant), level, false);
	item.setItemMeta(meta);
	
	this.getServer().broadcastMessage("[BOCS-VOTE]: "+p.getName()+" voted at: ");//vote.getAddress());
	this.getServer().broadcastMessage("[BOCS-VOTE]: "+p.getName()+" received an Enchanted book!");
	
	//give item to player
	leftItems = p.getInventory().addItem(item);
	if(!leftItems.isEmpty())
	{
		for(ItemStack is:leftItems.values())
			p.getWorld().dropItemNaturally(p.getLocation(), is);
	}
}*/
