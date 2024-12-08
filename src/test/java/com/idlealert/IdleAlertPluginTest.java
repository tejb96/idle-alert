package com.idlealert;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class IdleAlertPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(IdleAlertPlugin.class);
		RuneLite.main(args);
	}
}