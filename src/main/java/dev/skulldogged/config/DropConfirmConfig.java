package dev.skulldogged.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Global configuration for DropConfirm plugin.
 * Stored in config.json in the plugin's data directory.
 */
public class DropConfirmConfig {
  public static final BuilderCodec<DropConfirmConfig> CODEC = BuilderCodec
    .builder(DropConfirmConfig.class, DropConfirmConfig::new)
    .addField(
      new KeyedCodec<>("DefaultEnabled", Codec.BOOLEAN),
      (config, value) -> config.defaultEnabled = value,
      config -> config.defaultEnabled
    )
    .addField(
      new KeyedCodec<>("DefaultResetDelayMs", Codec.LONG),
      (config, value) -> config.defaultResetDelayMs = value,
      config -> config.defaultResetDelayMs
    )
    .addField(
      new KeyedCodec<>("AllowPlayerConfig", Codec.BOOLEAN),
      (config, value) -> config.allowPlayerConfig = value,
      config -> config.allowPlayerConfig
    )
    .build();

  private boolean defaultEnabled = true;
  private long defaultResetDelayMs = 1000L;
  private boolean allowPlayerConfig = true;

  public boolean isDefaultEnabled() {
    return defaultEnabled;
  }

  public long getDefaultResetDelayMs() {
    return defaultResetDelayMs;
  }

  public boolean isAllowPlayerConfig() {
    return allowPlayerConfig;
  }
}
