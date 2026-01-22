package dev.skulldogged.events;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import dev.skulldogged.DropConfirm;
import dev.skulldogged.config.PlayerPreferences;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class CancelDropEvent extends EntityEventSystem<EntityStore, DropItemEvent.PlayerRequest> {
  private final Map<UUID, ScheduledFuture<?>> pendingConfirmations = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread t = new Thread(r, "DropConfirm-Scheduler");
    t.setDaemon(true);
    return t;
  });

  public CancelDropEvent() {
    super(DropItemEvent.PlayerRequest.class);
  }

  @Override
  public void handle(
    int i,
    @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
    @Nonnull Store<EntityStore> store,
    @Nonnull CommandBuffer<EntityStore> commandBuffer,
    @Nonnull DropItemEvent.PlayerRequest playerRequest
  ) {
    Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
    if (uuidComponent == null) return;

    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null) return;

    UUID playerUuid = uuidComponent.getUuid();

    // Get player's settings
    PlayerPreferences.PlayerSettings settings = DropConfirm.getInstance().getPlayerSettings(playerUuid);

    // If player has drop confirm disabled, allow drop immediately
    if (!settings.isEnabled())
      return;

    // Check for pending confirmation (second drop attempt)
    ScheduledFuture<?> pending = pendingConfirmations.remove(playerUuid);
    if (pending != null) {
      pending.cancel(false);
      return; // Allow the drop
    }

    // First drop attempt - get item info and show confirmation
    Inventory inventory = player.getInventory();
    ItemContainer container = inventory.getSectionById(playerRequest.getInventorySectionId());
    if (container == null) return;

    ItemStack item = container.getItemStack(playerRequest.getSlotId());
    if (item == null) return;

    ItemQuality quality = ItemQuality.getAssetMap().getAsset(item.getItem().getQualityIndex());
    if (quality == null) return;

    Color textColor = quality.getTextColor();

    // Cancel the drop and show confirmation
    playerRequest.setCancelled(true);
    NotificationUtil.sendNotification(
      playerRef.getPacketHandler(),
      Message.join(
        Message.raw("Press again to drop "),
        Message
          .translation(item.getItem().getTranslationKey())
          .bold(true)
          .color(String.format("#%02x%02x%02x", textColor.red, textColor.green, textColor.blue))
      ),
      NotificationStyle.Warning
    );

    // Schedule reset using player's configured delay
    ScheduledFuture<?> future = scheduler.schedule(() -> {
      pendingConfirmations.remove(playerUuid);
    }, settings.getResetDelayMs(), TimeUnit.MILLISECONDS);

    pendingConfirmations.put(playerUuid, future);
  }

  @Nullable
  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.empty();
  }
}
