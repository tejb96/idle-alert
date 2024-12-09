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

@Slf4j
@PluginDescriptor(
		name = "Idle and low hp alerts"
)
public class IdleAlertPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private IdleAlertConfig config;

	@Inject
	private Notifier notifier;

	private int lastAnimation = -1;
	private WorldPoint lastLocation = null;
	private int idleAnimationCounter = 0;
	private int idleMovementCounter = 0;
	private static final int IDLE_THRESHOLD = 4; // Number of ticks to consider idle

	@Override
	protected void startUp() throws Exception {
		log.info("Idle Alert plugin started!");
		resetIdleCounters();
	}

	private void resetIdleCounters() {
		idleAnimationCounter = 0;
		idleMovementCounter = 0;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			log.info("Logged in!");
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

		// Consider an idle state only after consistent -1 animations and only if previous animation was not -1
		if (currentAnimation == -1 ) {
			idleAnimationCounter++;

			// Only trigger after consistent idle state
			if (idleAnimationCounter >= IDLE_THRESHOLD) {
				log.info("Idle Animation Alert Triggered");
				notifier.notify("You are idle (animation stopped)!");
				idleAnimationCounter = 0; // Prevent repeated notifications
			}
		} else {
			// Reset counter if there's an active animation
			idleAnimationCounter = 0;
		}

		lastAnimation = currentAnimation;
	}

	private void checkIdleMovement(Player player) {
		if (!config.movementIdle().isEnabled()) {
			return;
		}

		WorldPoint currentLocation = player.getWorldLocation();

		// Check if player is at the exact same location
		if (lastLocation != null && lastLocation.equals(currentLocation)) {
			idleMovementCounter++;

			// Only trigger after consistent idle state
			if (idleMovementCounter >= IDLE_THRESHOLD) {
				log.info("Idle Movement Alert Triggered");
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
		int threshold = config.getHitpointsThreshold();

		if (currentHP > 0 && currentHP <= threshold) {
			log.info("Low HP Alert Triggered");
			notifier.notify(ColorUtil.wrapWithColorTag(
					"Warning! Low Hitpoints: " + currentHP,
					Color.RED
			));
		}
	}

	@Provides
	IdleAlertConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IdleAlertConfig.class);
	}
}