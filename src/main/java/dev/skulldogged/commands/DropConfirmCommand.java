package dev.skulldogged.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.skulldogged.DropConfirm;
import dev.skulldogged.config.PlayerPreferences;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Command to toggle drop confirmation on/off for the player.
 * Usage: /dropconfirm
 */
public class DropConfirmCommand extends AbstractPlayerCommand {
  public DropConfirmCommand() {
    super("dropconfirm", "Toggle drop confirmation on/off");
  }

  @Override
  protected void execute(
    @Nonnull CommandContext context,
    @Nonnull Store<EntityStore> store,
    @Nonnull Ref<EntityStore> ref,
    @Nonnull PlayerRef playerRef,
    @Nonnull World world
  ) {
    if (!DropConfirm.getInstance().getConfig().isAllowPlayerConfig()) {
      playerRef.sendMessage(Message.raw("Player configuration is disabled by the server."));
      return;
    }

    UUID uuid = playerRef.getUuid();
    PlayerPreferences.PlayerSettings settings = DropConfirm.getInstance().getPlayerSettings(uuid);

    settings.setEnabled(!settings.isEnabled());
    DropConfirm.getInstance().savePreferences();

    String status = settings.isEnabled() ? "enabled" : "disabled";
    playerRef.sendMessage(Message.raw("Drop confirmation " + status + "."));
  }
}
