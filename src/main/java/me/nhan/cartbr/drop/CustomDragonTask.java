package me.nhan.cartbr.drop;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import io.papermc.paper.entity.TeleportFlag;
import me.nhan.cartbr.CartBRPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class CustomDragonTask extends BukkitRunnable {

    private final CartBRPlugin plugin;
    private final World world;
    private final List<Player> initialPassengers;

    private Location p2;
    private Vector stepVector;
    private float fixedYaw;
    private ArmorStand carrier;
    private ModeledEntity modeledEntity;
    private int ticksElapsed = 0;

    public CustomDragonTask(CartBRPlugin plugin, World world, List<Player> players) {
        this.plugin = plugin;
        this.world = world;
        this.initialPassengers = players;
    }

    public void start() {
        Location p1 = plugin.getConfigManager().getDragonP1(world);
        this.p2 = plugin.getConfigManager().getDragonP2(world);
        double speed = plugin.getConfigManager().getDragonSpeed();

        Vector dir = p2.toVector().subtract(p1.toVector());
        this.stepVector = dir.clone().normalize().multiply(speed);

        Location spawnLoc = p1.clone().setDirection(dir);
        spawnLoc.setPitch(0f);
        this.fixedYaw = spawnLoc.getYaw();

        this.carrier = world.spawn(spawnLoc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setGravity(false);
            as.setMarker(true);
            as.setInvulnerable(true);
            as.setSilent(true);
            as.setPersistent(false);
            as.setRotation(fixedYaw, 0f);
        });

        if (Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {
            try {
                this.modeledEntity = ModelEngineAPI.createModeledEntity(carrier);
                this.modeledEntity.setBaseEntityVisible(false);
                ActiveModel activeModel = ModelEngineAPI.createActiveModel(plugin.getConfigManager().getDragonModelId());
                if (activeModel != null) {
                    modeledEntity.addModel(activeModel, true);
                    activeModel.getAnimationHandler().playAnimation(
                            plugin.getConfigManager().getDragonAnimation(), 0.3, 0.3, 1.0, true
                    );
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("ModelEngine dragon model fallback (using invisible carrier): " + t.getMessage());
            }
        }

        for (Player p1Player : initialPassengers) {
            for (Player p2Player : initialPassengers) {
                if (!p1Player.equals(p2Player)) {
                    p1Player.hidePlayer(plugin, p2Player);
                }
            }
            carrier.addPassenger(p1Player);
        }

        this.runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        ticksElapsed++;
        if (carrier == null || !carrier.isValid()) {
            finishFlight();
            return;
        }

        Location currentLoc = carrier.getLocation();
        if (currentLoc.distanceSquared(p2) <= 4.0 || carrier.getPassengers().isEmpty()) {
            finishFlight();
            return;
        }

        Location nextLoc = currentLoc.add(stepVector);
        nextLoc.setYaw(fixedYaw);
        nextLoc.setPitch(0f);
        carrier.teleport(nextLoc, TeleportFlag.EntityState.RETAIN_PASSENGERS);

        if (ticksElapsed % 5 == 0) {
            String yStr = String.valueOf(nextLoc.getBlockY());
            for (Entity passenger : carrier.getPassengers()) {
                if (passenger instanceof Player p) {
                    plugin.getLocaleManager().sendActionBar(p, "drop-phase.on-dragon-actionbar", "<y>", yStr);
                }
            }
        }
    }

    private void finishFlight() {
        cleanup();
        plugin.getMatchManager().onDragonFlightFinished();
    }

    public void cleanup() {
        try { cancel(); } catch (IllegalStateException ignored) {}

        if (carrier != null && carrier.isValid()) {
            for (Entity entity : List.copyOf(carrier.getPassengers())) {
                carrier.removePassenger(entity);
                if (entity instanceof Player p) {
                    revealPlayer(p);
                    plugin.getParachuteManager().startParachuting(p);
                }
            }
            if (modeledEntity != null) {
                modeledEntity.destroy();
            }
            carrier.remove();
        }
    }

    public void revealPlayer(Player jumpingPlayer) {
        for (UUID uuid : plugin.getMatchManager().getMatchPlayers()) {
            Player other = Bukkit.getPlayer(uuid);
            if (other != null && !other.equals(jumpingPlayer)) {
                other.showPlayer(plugin, jumpingPlayer);
                jumpingPlayer.showPlayer(plugin, other);
            }
        }
    }

    public boolean isCarrier(Entity entity) {
        return carrier != null && carrier.getUniqueId().equals(entity.getUniqueId());
    }

    public int getTicksElapsed() {
        return ticksElapsed;
    }
}
