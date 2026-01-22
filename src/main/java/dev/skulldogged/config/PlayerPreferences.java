package dev.skulldogged.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player preferences for DropConfirm plugin.
 * Stored in player-preferences.json in the plugin's data directory.
 */
public class PlayerPreferences {
  public static final BuilderCodec<PlayerPreferences> CODEC = BuilderCodec
    .builder(PlayerPreferences.class, PlayerPreferences::new)
    .addField(
      new KeyedCodec<>("Players", new MapCodec<>(PlayerSettings.CODEC, HashMap::new)),
      (prefs, map) -> prefs.players = map != null ? new HashMap<>(map) : new HashMap<>(),
      prefs -> prefs.players
    )
    .build();

  private Map<String, PlayerSettings> players = new HashMap<>();

  public PlayerSettings getSettings(UUID uuid) {
    return players.computeIfAbsent(uuid.toString(), k -> new PlayerSettings());
  }

  public void setSettings(UUID uuid, PlayerSettings settings) {
    players.put(uuid.toString(), settings);
  }

  public boolean hasSettings(UUID uuid) {
    return players.containsKey(uuid.toString());
  }

  /**
   * Individual player settings.
   */
  public static class PlayerSettings {
    public static final BuilderCodec<PlayerSettings> CODEC = BuilderCodec
      .builder(PlayerSettings.class, PlayerSettings::new)
      .addField(
        new KeyedCodec<>("Enabled", Codec.BOOLEAN),
        (s, v) -> s.enabled = v,
        s -> s.enabled
      )
      .addField(
        new KeyedCodec<>("ResetDelayMs", Codec.LONG),
        (s, v) -> s.resetDelayMs = v,
        s -> s.resetDelayMs
      )
      .build();

    private boolean enabled = true;
    private long resetDelayMs = 1000L;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public long getResetDelayMs() {
      return resetDelayMs;
    }

    public void setResetDelayMs(long resetDelayMs) {
      this.resetDelayMs = resetDelayMs;
    }
  }
}
