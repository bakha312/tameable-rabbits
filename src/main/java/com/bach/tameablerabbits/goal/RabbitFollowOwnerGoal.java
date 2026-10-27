package com.bach.tameablerabbits.goal;

import com.bach.tameablerabbits.RabbitTameable;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class RabbitFollowOwnerGoal extends Goal {
	private final RabbitEntity rabbit;
	private final RabbitTameable tameable;
	private LivingEntity owner;
	private final double speed;
	private final EntityNavigation navigation;
	private int updateCountdownTicks;
	private final float maxDistance;
	private final float minDistance;
	private float oldWaterPathfindingPenalty;

	public RabbitFollowOwnerGoal(RabbitEntity rabbit, double speed, float minDistance, float maxDistance) {
		this.rabbit = rabbit;
		this.tameable = (RabbitTameable) rabbit;
		this.speed = speed;
		this.navigation = rabbit.getNavigation();
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
	public boolean canStart() {
		LivingEntity livingEntity = this.tameable.getOwner();
		if (livingEntity == null) {
			return false;
		}
		if (livingEntity.isSpectator()) {
			return false;
		}
		if (this.tameable.isSitting()) {
			return false;
		}
		if (this.rabbit.squaredDistanceTo(livingEntity) < (double) (this.minDistance * this.minDistance)) {
			return false;
		}
		this.owner = livingEntity;
		return true;
	}

	@Override
	public boolean shouldContinue() {
		if (this.navigation.isIdle()) {
			return false;
		}
		if (this.tameable.isSitting()) {
			return false;
		}
		return !(this.rabbit.squaredDistanceTo(this.owner) <= (double) (this.maxDistance * this.maxDistance));
	}

	@Override
	public void start() {
		this.updateCountdownTicks = 0;
		this.oldWaterPathfindingPenalty = this.rabbit.getPathfindingPenalty(PathNodeType.WATER);
		this.rabbit.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
	}

	@Override
	public void stop() {
		this.owner = null;
		this.navigation.stop();
		this.rabbit.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
	}

	@Override
	public void tick() {
		this.rabbit.getLookControl().lookAt(this.owner, 10.0F, this.rabbit.getMaxLookPitchChange());
		if (--this.updateCountdownTicks <= 0) {
			this.updateCountdownTicks = this.getTickCount(10);
			if (!this.rabbit.isLeashed() && !this.rabbit.hasVehicle()) {
				if (this.rabbit.squaredDistanceTo(this.owner) >= 144.0) {
					this.tryTeleport();
				} else {
					this.navigation.startMovingTo(this.owner, this.speed);
				}
			}
		}
	}

	private void tryTeleport() {
		BlockPos blockPos = this.owner.getBlockPos();
		for (int i = 0; i < 10; i++) {
			int x = blockPos.getX() + this.getRandomInt(-3, 3);
			int y = blockPos.getY() + this.getRandomInt(-1, 1);
			int z = blockPos.getZ() + this.getRandomInt(-3, 3);
			if (this.tryTeleportTo(x, y, z)) {
				return;
			}
		}
	}

	private boolean tryTeleportTo(int x, int y, int z) {
		if (Math.abs(x - this.owner.getX()) < 2.0 && Math.abs(z - this.owner.getZ()) < 2.0) {
			return false;
		}
		if (!this.canTeleportTo(new BlockPos(x, y, z))) {
			return false;
		}
		this.rabbit.refreshPositionAndAngles(x + 0.5, y, z + 0.5, this.rabbit.getYaw(), this.rabbit.getPitch());
		this.navigation.stop();
		return true;
	}

	private boolean canTeleportTo(BlockPos pos) {
		PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this.rabbit.getWorld(), pos.mutableCopy());
		if (pathNodeType != PathNodeType.WALKABLE) {
			return false;
		}
		BlockState blockState = this.rabbit.getWorld().getBlockState(pos.down());
		if (blockState.getBlock() instanceof LeavesBlock) {
			return false;
		}
		BlockPos blockPos = pos.subtract(this.rabbit.getBlockPos());
		return this.rabbit.getWorld().isSpaceEmpty(this.rabbit, this.rabbit.getBoundingBox().offset(blockPos));
	}

	private int getRandomInt(int min, int max) {
		return this.rabbit.getRandom().nextInt(max - min + 1) + min;
	}
}
