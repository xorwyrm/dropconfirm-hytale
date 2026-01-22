package dev.skulldogged;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.skulldogged.commands.DropConfirmCommand;
import dev.skulldogged.commands.DropConfirmConfigCommand;
import dev.skulldogged.config.DropConfirmConfig;
import dev.skulldogged.config.PlayerPreferences;
import dev.skulldogged.events.CancelDropEvent;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.logging.Level;

public class DropConfirm extends JavaPlugin {
  private static DropConfirm instance;

  private final Config<DropConfirmConfig> config;
  private final Config<PlayerPreferences> playerPreferences;

  public DropConfirm(@Nonnull JavaPluginInit init) {
    super(init);
    instance = this;

    // Register configs - must be called in constructor before setup()
    this.config = withConfig(DropConfirmConfig.CODEC);
    this.playerPreferences = withConfig("player-preferences", PlayerPreferences.CODEC);
  }

  @Override
  protected void setup() {
    getLogger().at(Level.INFO).log("DropConfirm loading...");

    // Register event system
    this.getEntityStoreRegistry().registerSystem(new CancelDropEvent());

    // Register commands
    this.getCommandRegistry().registerCommand(new DropConfirmCommand());
    this.getCommandRegistry().registerCommand(new DropConfirmConfigCommand());

    getLogger().at(Level.INFO).log("DropConfirm loaded! Default enabled: %s, Default delay: %dms",
      getConfig().isDefaultEnabled(),
      getConfig().getDefaultResetDelayMs()
    );
  }

  /**
   * Get the plugin instance.
   */
  @Nonnull
  public static DropConfirm getInstance() {
    return instance;
  }

  /**
   * Get the global plugin configuration.
   */
  @Nonnull
  public DropConfirmConfig getConfig() {
    return config.get();
  }

  /**
   * Get a player's settings, creating defaults if they don't exist.
   */
  @Nonnull
  public PlayerPreferences.PlayerSettings getPlayerSettings(@Nonnull UUID uuid) {
    PlayerPreferences prefs = playerPreferences.get();
    boolean isNew = !prefs.hasSettings(uuid);
    
    PlayerPreferences.PlayerSettings settings = prefs.getSettings(uuid);

    // Apply defaults from global config if this is a new player
    if (isNew) {
      settings.setEnabled(getConfig().isDefaultEnabled());
      settings.setResetDelayMs(getConfig().getDefaultResetDelayMs());
      savePreferences();
    }

    return settings;
  }

  /**
   * Save player preferences to disk.
   */
  public void savePreferences() {
    playerPreferences.save();
  }
}
