package com.bach.tameablerabbits;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface RabbitTameable {
	boolean isTamed();

	void setTamed(boolean tamed);

	boolean isSitting();

	void setSitting(boolean sitting);

	@Nullable
	UUID getOwnerUuid();

	void setOwnerUuid(@Nullable UUID uuid);

	@Nullable
	LivingEntity getOwner();

	void setOwner(PlayerEntity player);

	boolean isOwner(LivingEntity entity);
}
