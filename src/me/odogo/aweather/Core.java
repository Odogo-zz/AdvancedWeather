package me.odogo.aweather;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Core extends JavaPlugin {

	public final String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + " Advanced " + ChatColor.RED + "Weather" + ChatColor.GRAY + "] " + ChatColor.RESET;
	public World selectedWorld;

	@Override
	public void onEnable() {
		registerConfigs();

		int i = 0;
		int max = this.getServer().getWorlds().size();

		boolean foundWorld = false;

		for(World worlds : this.getServer().getWorlds()) {

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

			int elapsedSeconds = 0;
			boolean broadcasted = false;

			String sForcast = "";
			String Temperature = "";

			@Override
			public void run() {

				long time = selectedWorld.getTime();

				if(time >= 23990 && time <= 24000) {
					broadcasted = false;
					elapsedSeconds = 0;
				}

				if(!(time > 0) && !(time < 25)) {
					return;
				}


				int forcast = new Random().nextInt((3 - 1) + 1) + 1;
				int temperature = new Random().nextInt((120 - 10) + 1) + 10;

				int rainChance;
				if(forcast == 2 || forcast == 3) {
					rainChance = new Random().nextInt((100 - 0) + 1) + 0;
				} else {
					rainChance = 0;
				}

				if(forcast == 1) { sForcast = "Sunny"; } else if(forcast == 2) { sForcast = "Light or Medium Rain or Small Thunderstorms"; } else if(forcast == 3) { sForcast = "Hard Rain or Severe Thunderstorms"; }

				if(temperature >= 85) { Temperature = temperature + " (Make sure to drink water)"; } else if(temperature < 85 && temperature > 45) { Temperature = temperature + " (A good day to work on projects)"; } else if(temperature <= 45) { Temperature = temperature + " (Might wanna wear a chestplate)"; }

				if(!broadcasted) {

					getServer().broadcastMessage(prefix + ChatColor.GOLD + "-=- Daily Forcast Notification -=-");
					getServer().broadcastMessage(" ");

					getServer().broadcastMessage(ChatColor.GREEN + "Forcast: " + ChatColor.YELLOW + sForcast);
					getServer().broadcastMessage(ChatColor.GREEN + "Temperature: " + ChatColor.YELLOW + Temperature);
					getServer().broadcastMessage(ChatColor.GREEN + "Rain Chance (Percentage): " + ChatColor.YELLOW + rainChance + "%");

					getServer().broadcastMessage(" ");
					getServer().broadcastMessage(ChatColor.GOLD + "-=- Daily Forcast Notification -=-");

					broadcasted = true;
				}

				if((new Random().nextInt((100 - 0) + 1) + 0) < rainChance) {

					if(elapsedSeconds == 120) {

						if(forcast == 2) {
							selectedWorld.setStorm(true);
							selectedWorld.setThundering(false);
						} else if(forcast == 3) {
							selectedWorld.setStorm(true);
							selectedWorld.setThundering(true);
							selectedWorld.setThunderDuration(0 * 20);
						}

					}

				}

				elapsedSeconds++;

			}

		}.runTaskTimer(this, 0, 20);

	}

}
