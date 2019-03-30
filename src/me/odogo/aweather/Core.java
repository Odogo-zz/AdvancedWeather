package me.odogo.aweather;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Core extends JavaPlugin {

	public final String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + " Advanced " + ChatColor.RED + "Weather" + ChatColor.GRAY + "] " + ChatColor.RESET;
	public World world;
	public FileConfiguration message;
	public boolean enableEffects = false;

	@Override
	public void onEnable() {

		regConfigYML();

		int i = 0;
		int max = this.getServer().getWorlds().size();
		boolean foundWorld = false;

		for(World worlds : this.getServer().getWorlds()) {

			if(foundWorld) {
				break;
			}

			if(worlds.getName().equals(getConfig().getString("defaulted-selected-world"))) {
				world = worlds;
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

		regOtherConfig();
		startWeatherTimer();
	}

	@Override
	public void onDisable() {

	}

	private void regConfigYML() {
		this.saveDefaultConfig();
	}

	private void regOtherConfig() {
		File mes = new File(this.getDataFolder(), "messages.yml");

		if(!mes.exists()) {
			this.saveResource("messages.yml", false);
		}

		this.message = YamlConfiguration.loadConfiguration(mes);

	}

	public void startWeatherTimer() {

		new BukkitRunnable() {

			boolean alreadyDay = false;
			boolean rain = false;
			boolean thunder = false;
			int secElap = 0;
			int tickElap = 0;

			@Override
			public void run() {

				if(world.getTime() == 23999) {
					rain = false;
					alreadyDay = false;
					secElap = 0;
					tickElap = 0;
					enableEffects = false;
					return;
				}

				if(alreadyDay) {

					if(secElap == 120) {
						if(rain) {
							setRaining(true);
						}

						if(thunder) {
							setThundering(true);
						}
					} else if(secElap == 60) {
						enableEffects = true;
					}

					if(tickElap >= 20) {
						secElap++;
						tickElap = 0;
					} else {
						tickElap++;
					}

				} else {

					if(world.getTime() == 0) {

						alreadyDay = true;

						int temp = new Random().nextInt((110 - 15) + 1) + 15;
						int forecastN = new Random().nextInt((3 - 1) + 1) + 1;
						int rainN;

						if(forecastN == 1) { 
							rainN = 0;
						} else {
							rainN = new Random().nextInt((100 - 0) + 1) + 0;
						}

						String forecast = "";

						if(forecastN == 1) {
							forecast = "Sunny";
						} else if(forecastN == 2) {
							forecast = "Rain Showers";
						} else if(forecastN == 3) {
							forecast = "Rain Showers & Thunderstorms";
						}

						List<String> forecastNotifications;

						if(getConfig().getBoolean("use-default-messages")) {
							forecastNotifications = new ArrayList<String>();
							forecastNotifications.add("&6-=- Daily Forecast Notification -=-");
							forecastNotifications.add(" ");
							forecastNotifications.add("&eCurrent Forecast: &a%forecast%");
							forecastNotifications.add("&eTemperature: &a%temperature%");
							forecastNotifications.add("&eRain Chance: &a%rainChance%");
							forecastNotifications.add(" ");
							forecastNotifications.add("&6-=- Daily Forecast Notification -=-");
						} else {
							forecastNotifications = message.getStringList("forecast-information");
						}

						ArrayList<String> fNList = new ArrayList<String>();

						for(String fNLine : forecastNotifications) {

							fNLine = fNLine.replace("%forecast%", forecast + " (" + forecastN + ")");
							fNLine = fNLine.replace("%temperature%", temp + "°F");
							fNLine = fNLine.replace("%rainChance%", rainN + "%");

							fNList.add(ChatColor.translateAlternateColorCodes('&', fNLine));
						}

						for(Player players : Bukkit.getOnlinePlayers()) {
							if(players.getWorld().getName().equals(world.getName())) {

								for(String line : fNList) {
									players.sendMessage(line);
								}

								if(getConfig().getBoolean("play-sound-on-message")) {
									players.playSound(players.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
								}
							}
						}

						if(rainN > 0) {
							if(getConfig().getBoolean("weather-probability.random-probability")) {
								if((new Random().nextInt((100 - 0) + 1) + 0) < rainN) {
									if(forecastN == 2) {
										this.rain = true;
										this.thunder = false;
									} else if(forecastN == 3) {
										this.rain = true;
										this.thunder = true;
									}
								}
							} else {
								if(getConfig().getBoolean("weather-probability.fixed-probability.less-than")) {
									if(rainN < getConfig().getInt("weather-probability.fixed-probability.percentage-probability")) {
										if(forecastN == 2) {
											this.rain = true;
											this.thunder = false;
										} else if(forecastN == 3) {
											this.rain = true;
											this.thunder = true;
										}
									}
								} else {
									if(rainN > getConfig().getInt("weather-probability.fixed-probability.percentage-probability")) {
										if(forecastN == 2) {
											this.rain = true;
											this.thunder = false;
										} else if(forecastN == 3) {
											this.rain = true;
											this.thunder = true;
										}
									}
								}
							}
						}

					}

				}

			}

		}.runTaskTimer(this, 0, 1);

	}

	public boolean isRaining() {
		return world.hasStorm();
	}

	public void setRaining(boolean raining) {
		world.setStorm(raining);
	}

	public boolean isThundering() {
		return world.isThundering();
	}

	public void setThundering(boolean thunder) {
		world.setThundering(thunder);
	}
}
