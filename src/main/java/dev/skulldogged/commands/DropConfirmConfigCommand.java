package dev.skulldogged.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.skulldogged.DropConfirm;
import dev.skulldogged.ui.DropConfirmConfigPage;

import javax.annotation.Nonnull;

/**
 * Command to open the DropConfirm configuration UI.
 * Usage: /dropconfirmconfig
 */
public class DropConfirmConfigCommand extends AbstractPlayerCommand {
  public DropConfirmConfigCommand() {
    super("dropconfirmconfig", "Open DropConfirm settings UI");
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

    DropConfirmConfigPage.open(playerRef, ref, store);
  }
}
