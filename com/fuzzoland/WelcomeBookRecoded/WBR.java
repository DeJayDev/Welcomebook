package com.fuzzoland.WelcomeBookRecoded;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class WBR extends JavaPlugin{

	public static File datafolder;
	public Logger logger = Bukkit.getLogger();
	public BookHandler bh;
	public Economy eco;
	public YamlConfiguration settings;
	
	public void onEnable(){
		File settingsFile = new File(getDataFolder(), "settings.yml");
		if(!settingsFile.exists()){
			settingsFile.getParentFile().mkdirs();
			saveResource("settings.yml", false);
		}
		settings = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "settings.yml"));
		File booksFolder = new File(getDataFolder() + "/books");
		if(!booksFolder.exists()){
			booksFolder.mkdirs();
		}
		bh = new BookHandler(booksFolder, logger);
		try{
			bh.loadBooks();
		}catch(IOException e){
			e.printStackTrace();
		}
		getServer().getPluginManager().registerEvents(new JoinListener(this), this);
		if(settings.getBoolean("BookShop.Enabled")){
			setupEconomy();
		}
		if(settings.getBoolean("CustomCommands.Enabled")){
			getServer().getPluginManager().registerEvents(new CommandListener(this), this);
		}
		logger.log(Level.INFO, "[WBR] Events registered!");
		getCommand("wbr").setExecutor(new CommandWBR(this));
		getCommand("gb").setExecutor(new CommandGB(this));
		logger.log(Level.INFO, "[WBR] Commands registered!");
		try{
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
			logger.log(Level.INFO, "[WBR] Metrics loaded!");
		}catch(IOException e){
			logger.log(Level.WARNING, "[WBR] Metrics has failed to load!");
			e.printStackTrace();
		}
		datafolder = getDataFolder();
		logger.log(Level.INFO, "[WBR] Version " + getDescription().getVersion() + " has been enabled.");
	}
	
	public void onDisable(){
		try{
			bh.saveBooks();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void setupEconomy(){
		logger.log(Level.INFO, "[WBR] You have chosen to enable the BookShops feature.");
    	logger.log(Level.INFO, "[WBR] Setting up economy support...");
    	if(getServer().getPluginManager().getPlugin("Vault") == null){
    		logger.log(Level.SEVERE, "[WBR] Failed to set up economy support - vault not found!");
    		return;
    	}
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
        	logger.log(Level.SEVERE, "[WelcomeBook] Failed to set up economy support - economy plugin not found!");
            return;
        }
        eco = rsp.getProvider();
		getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        logger.log(Level.INFO, "[WBR] Succesfully set up economy support!");
	}
	
	public String convertTime(Long millis){
		if(millis >= 86400000){
			return convertToDays(millis);
		}else if(millis >= 3600000){
			return convertToHours(millis);
		}else if(millis >= 60000){
			return convertToMinutes(millis);
		}else if(millis >= 1000){
			return convertToSeconds(millis);
		}else{
			return "<1 s";
		}
	}
	
	private String convertToSeconds(Long millis){
		return String.valueOf((int) ((millis / 1000) % 60)) + "s";
	}
	
	private String convertToMinutes(Long millis){
		return String.valueOf((int) ((millis / 60000) % 60)) + "m, " + String.valueOf((int) ((millis / 1000) % 60)) + "s";
	}
	
	private String convertToHours(Long millis){
		return String.valueOf((int) ((millis / 3600000) % 24)) + "h, " + String.valueOf((int) ((millis / 60000) % 60)) + "m, " + String.valueOf((int) ((millis / 1000) % 60)) + "s";
	}
	
	private String convertToDays(Long millis){
		return String.valueOf((int) ((millis / 86400000) % 365)) + "d, " + String.valueOf((int) ((millis / 3600000) % 24)) + "h, " + String.valueOf((int) ((millis / 60000) % 60)) + "m, " + String.valueOf((int) ((millis / 1000) % 60)) + "s";
	}
}
