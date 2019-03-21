package me.odogo.aweather;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Core extends JavaPlugin {

	public final String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + " Advanced " + ChatColor.RED + "Weather" + ChatColor.GRAY + "] " + ChatColor.RESET;
	public World selectedWorld;

	public boolean enableEffects = false;

	@Override
	public void onEnable() {
		registerConfigs();

		int i = 0;
		int max = this.getServer().getWorlds().size();

		boolean foundWorld = false;

		for(World worlds : this.getServer().getWorlds()) {

			if(foundWorld) {
				break;
			}

			if(worlds.getName().equals(getConfig().getString("default-enabled-world"))) {
				selectedWorld = worlds;
				foundWorld = true;
				break;
			}

			i++;

			if(i == max) {
				IllegalStateException e = new IllegalStateException("The world listed in the config could not be found.");
				e.printStackTrace();
				break;
			}

		}

		if(!foundWorld) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		registerEvents();
		startWeatherTimer();
	}

	@Override
	public void onDisable() {

	}

	private void registerConfigs() {

		getConfig().addDefault("default-enabled-world", "world");
		getConfig().options().copyDefaults(true);
		saveConfig();

	}

	private void registerEvents() {
		PluginManager pm = this.getServer().getPluginManager();

	}

	public void startWeatherTimer() {

		new BukkitRunnable() {

			boolean alreadyDay = false;
			boolean rain = false;
			int secElap = 0;
			int ticksElap = 0;

			@Override
			public void run() {

				if(selectedWorld.getTime() == 23999) {
					rain = false;
					enableEffects = false;
					alreadyDay = false;
					secElap = 0;
				}

				if(alreadyDay) {
					if(secElap == 120) {
						if(rain) {
							setRaining(true);
							setThundering(true);
						}
					} else if(secElap == 60) {
						enableEffects = true;
					}

					if(ticksElap >= 20) {
						secElap++;
						ticksElap = 0;
					} else {
						ticksElap++;
					}
				} else { 

					if(selectedWorld.getTime() == 0) {

						alreadyDay = true;

						int temp = new Random().nextInt((110 - 45) + 1) + 45;
						int forecastN = new Random().nextInt((3-1) + 1) + 1;
						int rainN;
						if(forecastN == 1) { rainN = 0; } else { rainN = new Random().nextInt((100 - 1) + 1) + 1; }

						String forecast = "";

						if(forecastN == 1) { forecast = "Sunny"; } else if(forecastN == 2) { forecast = "Rain Showers"; } else if(forecastN == 3) { forecast = "Rain Showers and Thunderstorms"; }

						String[] message = {

								ChatColor.GOLD + "-=- Daily Forecast Message -=-",
								" ",
								ChatColor.YELLOW + "Current Temperature: " + ChatColor.GREEN + temp + "°F",
								ChatColor.YELLOW + "Forecast: " + ChatColor.GREEN + forecast,
								ChatColor.YELLOW + "Rain Chance: " + ChatColor.GREEN + rainN + "%",
								" ",
								ChatColor.GOLD + "-=- Daily Forecast Message -=-",

						};

						for(Player players : Bukkit.getOnlinePlayers()) {			
							if(players.getWorld().getName().equals(selectedWorld.getName())) {
								players.sendMessage(message);
								players.playSound(players.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							}
						}

						if(rainN > 0) {
							if((new Random().nextInt((100 - 1) + 1) + 1) <= rainN) {
								rain = true;
							}
						}
					}
				}

			}

		}.runTaskTimer(this, 0, 1);

	}

	public boolean isRaining() {
		return selectedWorld.hasStorm();
	}

	public void setRaining(boolean raining) {
		selectedWorld.setStorm(raining);
	}

	public boolean isThundering() {
		return selectedWorld.isThundering();
	}

	public void setThundering(boolean thunder) {
		selectedWorld.setThundering(thunder);
	}
}
