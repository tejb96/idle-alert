package com.idlealert;

import net.runelite.client.config.*;

@ConfigGroup("example")
public interface IdleAlertConfig extends Config
{
	@ConfigItem(
			keyName = "animationidle",
			name = "Idle Animation Alert",
			description = "Configures if idle animation alerts are enabled",
			position = 1
	)
	default Notification animationIdle()
	{
		return Notification.ON;
	}

	@ConfigItem(
			keyName = "movementidle",
			name = "Idle Movement Alert",
			description = "Configures if idle movement alerts are enabled e.g. running, walking",
			position = 3
	)
	default Notification movementIdle()
	{
		return Notification.OFF;
	}

	@ConfigItem(
			keyName = "hpalert",
			name = "Low HP Alert",
			description = "Configures if hitpoints alerts are enabled",
			position = 6
	)
	default Notification getHitpointsNotification()
	{
		return Notification.OFF;
	}

	@ConfigItem(
			keyName = "hp",
			name = "Low Hitpoints Alert",
			description = "The amount of hitpoints to send a notification at.",
			position = 7
	)
	@Range(min = 1)
	default int getHitpointsThreshold()
	{
		return 1;
	}
}
