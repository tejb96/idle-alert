package com.idlealert;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.Notifier;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@PluginDescriptor(
		name = "Idle Alerts"
)
public class IdleAlertPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private IdleAlertConfig config;

	@Inject
	private Notifier notifier;

	// Use a queue to track recent animation states
	private Queue<Integer> animationStates = new LinkedList<>();
	private WorldPoint lastLocation = null;
	private int idleMovementCounter = 0;
	private static final int IDLE_THRESHOLD = 4; // Number of ticks to consider idle
	private static final int ANIMATION_STATE_QUEUE_SIZE = 5; // Track last 5 animation states

	@Override
	protected void startUp() throws Exception {
		log.info("Idle Alert plugin started!");
		resetIdleCounters();
	}

	private void resetIdleCounters() {
		idleMovementCounter = 0;
		animationStates.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
//			log.info("Logged in!");
			resetIdleCounters(); // Reset counters on login
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick) {
		Player player = client.getLocalPlayer();
		if (player == null) {
			return;
		}

		checkIdleAnimation(player);
		checkIdleMovement(player);
		checkLowHP();
	}

	private void checkIdleAnimation(Player player) {
		if (!config.animationIdle().isEnabled()) {
			return;
		}

		int currentAnimation = player.getAnimation();

		// Maintain a queue of last few animation states
		animationStates.offer(currentAnimation);
		if (animationStates.size() > ANIMATION_STATE_QUEUE_SIZE) {
			animationStates.poll();
		}

		// Check if we have a consistent sequence of -1 animations after a non-idle state
		if (isConsecutiveIdleAnimation()) {
//			log.info("Idle Animation Alert Triggered");
			notifier.notify("You are idle!");
		}
	}

	private boolean isConsecutiveIdleAnimation() {
		// Check if first item is not -1, and all subsequent items are -1
		return animationStates.size() >= ANIMATION_STATE_QUEUE_SIZE &&
				animationStates.stream().findFirst().orElse(-1) != -1 &&
				animationStates.stream().skip(1).allMatch(anim -> anim == -1);
	}

	private void checkIdleMovement(Player player) {
		if (!config.movementIdle().isEnabled()) {
			return;
		}

		WorldPoint currentLocation = player.getWorldLocation();

		// Check if player is at the exact same location
		if (lastLocation != null) {
			idleMovementCounter++;

			// Only trigger after consistent idle state
			if (idleMovementCounter >= IDLE_THRESHOLD) {
//				log.info("Idle Movement Alert Triggered");
				notifier.notify("You are idle (no movement detected)!");
				idleMovementCounter = 0; // Prevent repeated notifications
			}
		} else {
			// Reset counter if there's movement
			idleMovementCounter = 0;
		}

		lastLocation = currentLocation;
	}

	private void checkLowHP() {
		if (!config.getHitpointsNotification().isEnabled()) {
			return;
		}

		int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int absorptionHP = client.getVarbitValue(Varbits.NMZ_ABSORPTION); // NMZ absorption value
		int totalHP = currentHP + absorptionHP; // Combine regular and absorption HP
		int threshold = config.getHitpointsThreshold();

		// Trigger notification if total HP is at or below the threshold
		if (totalHP > 0 && totalHP <= threshold) {
//			log.info("Low HP Alert Triggered");
			notifier.notify(
					"Warning! Low Hitpoints: " + currentHP + "eat up now!"

			);
		}
	}


	@Provides
	IdleAlertConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IdleAlertConfig.class);
	}
}