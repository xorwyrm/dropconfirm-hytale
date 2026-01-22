package dev.skulldogged.ui;

import au.ellie.hyui.builders.PageBuilder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.skulldogged.DropConfirm;
import dev.skulldogged.config.PlayerPreferences;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

/**
 * Opens the DropConfirm configuration UI using HyUI.
 */
public class DropConfirmConfigPage {

  public static void open(@Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    UUID playerUuid = playerRef.getUuid();
    PlayerPreferences.PlayerSettings settings = DropConfirm.getInstance().getPlayerSettings(playerUuid);

    // Convert delay to slider value (100-5000ms range, step of 100)
    int delaySliderValue = (int) Math.min(5000, Math.max(100, settings.getResetDelayMs()));

    String html = """
      <style>
        .container {
          layout-mode: top;
          anchor-width: 500;
          anchor-height: 300;
        }
        .container-contents {
          layout-mode: top;
        }
        .section {
          layout-mode: top;
          anchor-height: 80;
        }
        .section-header {
          font-size: 14;
          font-weight: bold;
          color: #AAAAAA;
          text-transform: uppercase;
        }
        .setting-row {
          layout-mode: left;
          anchor-height: 25;
        }
        .setting-label {
          font-size: 18;
          anchor-width: 220;
          color: #FFFFFF;
        }
        .description {
          font-size: 14;
          color: #888888;
          anchor-height: 25;
        }
        .delay-display {
          font-size: 16;
          color: #FFCC00;
          anchor-width: 80;
          text-align: center;
        }
        .content-spacer {
          flex-weight: 1;
        }
        .button-row {
          layout-mode: right;
          anchor-height: 50;
        }
        .button-spacer {
          anchor-width: 20;
        }
      </style>

      <div class="page-overlay">
        <div class="container" data-hyui-title="DropConfirm Settings">
          <div class="container-contents">
            <!-- Enable/Disable Section -->
            <div class="section">
              <p class="section-header">Protection</p>
              <div class="setting-row">
                <label class="setting-label" data-hyui-tooltiptext="When enabled, you must press drop twice to confirm dropping an item">
                  Enable DropConfirm
                </label>
                <input type="checkbox" id="enabled-checkbox" value="%s"/>
              </div>
            </div>

            <!-- Delay Section -->
            <div class="section">
              <p class="section-header">Timing</p>
              <p class="description">How long before confirmation resets</p>
              <input
	        type="range"
	        id="delay-slider"
	        min="100"
	        max="5000"
	        step="100"
	        value="%d"
                data-hyui-tooltiptext="Slide to adjust the confirmation timeout (100ms - 5000ms)"
              />
              <p id="delay-display" class="delay-display">%dms</p>
            </div>

            <!-- Flexible spacer to push buttons to bottom -->
            <div class="content-spacer"></div>

            <!-- Action Buttons -->
            <div class="button-row">
              <button id="save-btn">Save Changes</button>
              <div class="button-spacer"></div>
              <input type="reset" id="close-btn" value="Cancel"/>
            </div>
          </div>
        </div>
      </div>
      """.formatted(settings.isEnabled(), delaySliderValue, delaySliderValue);

    PageBuilder.pageForPlayer(playerRef)
      .withLifetime(CustomPageLifetime.CanDismiss)
      .fromHtml(html)
      // ValueChanged listeners are required for getValue() to return updated values
      .addEventListener("enabled-checkbox", CustomUIEventBindingType.ValueChanged, (Object data, au.ellie.hyui.events.UIContext ctx) -> {
        // Value tracking happens automatically in HyUI when this listener exists
      })
      .addEventListener("delay-slider", CustomUIEventBindingType.ValueChanged, (Object data, au.ellie.hyui.events.UIContext ctx) -> {
        // Value tracking happens automatically in HyUI when this listener exists
      })
      .addEventListener("save-btn", CustomUIEventBindingType.Activating, (Object data, au.ellie.hyui.events.UIContext ctx) -> {
        // Get values from the page
        Optional<Boolean> enabled = ctx.getValue("enabled-checkbox", Boolean.class);
        Optional<Integer> delay = ctx.getValue("delay-slider", Integer.class);

        PlayerPreferences.PlayerSettings playerSettings = DropConfirm.getInstance().getPlayerSettings(playerUuid);

        enabled.ifPresent(playerSettings::setEnabled);

        if (delay.isPresent()) {
          long delayMs = delay.get().longValue();
          playerSettings.setResetDelayMs(delayMs);
        }

        DropConfirm.getInstance().savePreferences();
        playerRef.sendMessage(Message.raw("§aSettings saved! §7Drop confirm: " +
          (playerSettings.isEnabled() ? "§aEnabled" : "§cDisabled") +
          "§7, Delay: §e" + playerSettings.getResetDelayMs() + "ms"));

        // Close the page
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
          player.getPageManager().setPage(ref, store, Page.None);
        }
      })
      .addEventListener("close-btn", CustomUIEventBindingType.Activating, (Object data, au.ellie.hyui.events.UIContext ctx) -> {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
          player.getPageManager().setPage(ref, store, Page.None);
        }
      })
      .open(store);
  }
}
