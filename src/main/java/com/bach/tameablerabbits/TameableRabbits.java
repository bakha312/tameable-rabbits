package com.bach.tameablerabbits;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TameableRabbits implements ModInitializer {
	public static final String MOD_ID = "tameable_rabbits";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Tameable Rabbits loaded. Feed a rabbit a carrot to tame it.");
	}
}
