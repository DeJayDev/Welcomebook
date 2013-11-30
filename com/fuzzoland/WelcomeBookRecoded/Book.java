package com.fuzzoland.WelcomeBookRecoded;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;

public class Book{

	private String title;
	private String author;
	private List<String> pages;
	private Map<String, Long> cooldowns;
	
	public Book(String title, String author, List<String> pages, Map<String, Long> cooldowns){
		this.title = title;
		this.author = author;
		this.pages = pages;
		this.cooldowns = cooldowns;
	}
	
	public void save(String codename) throws IOException{
		YamlConfiguration book = new YamlConfiguration();
		book.set("Title", this.title);
		book.set("Author", this.author);
		book.set("Pages", this.pages);
		for(Entry<String, Long> entrySet : cooldowns.entrySet()){
			book.set("Cooldowns." + entrySet.getKey(), entrySet.getValue());
		}
		book.save(new File(WBR.datafolder + "/books", codename + ".yml"));
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public String getAuthor(){
		return this.author;
	}
	
	public void setAuthor(String author){
		this.author = author;
	}
	
	public List<String> getPages(){
		return this.pages;
	}
	
	public void setPages(List<String> pages){
		this.pages = pages;
	}
	
	public Boolean hasCooldown(String player){
		if(cooldowns.containsKey(player)){
			return true;
		}else{
			return false;
		}
	}
	
	public Long getCooldown(String player){
		return this.cooldowns.get(player);
	}
	
	public void updateCooldown(String player, Long cooldown){
		this.cooldowns.put(player, cooldown);
	}
}
