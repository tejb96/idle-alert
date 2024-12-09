package com.idlealert;

import net.runelite.client.config.*;

@ConfigGroup("idlealert")
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
		return Notification.ON;
	}

	@ConfigItem(
			keyName = "hpalert",
			name = "Low HP Alert",
			description = "Configures if hitpoints alerts are enabled",
			position = 6
	)
	default Notification getHitpointsNotification()
	{
		return Notification.ON;
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
		return 15;
	}

//	@ConfigItem(
//			keyName = "idleDetectionThreshold",
//			name = "Idle Detection Sensitivity",
//			description = "Number of consecutive game ticks to wait before triggering an idle alert",
//			position = 4
//	)
//	@Range(min = 3, max = 15)
//	default int getIdleDetectionThreshold()
//	{
//		return 5;
//	}
}
