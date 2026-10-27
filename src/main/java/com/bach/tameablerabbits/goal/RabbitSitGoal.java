package com.bach.tameablerabbits.goal;

import com.bach.tameablerabbits.RabbitTameable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.RabbitEntity;

import java.util.EnumSet;

public class RabbitSitGoal extends Goal {
	private final RabbitEntity rabbit;
	private final RabbitTameable tameable;

	public RabbitSitGoal(RabbitEntity rabbit) {
		this.rabbit = rabbit;
		this.tameable = (RabbitTameable) rabbit;
		this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
	}

	@Override
	public boolean canStart() {
		if (!this.tameable.isTamed()) {
			return false;
		}
		if (this.rabbit.isInsideWaterOrBubbleColumn()) {
			return false;
		}
		if (!this.rabbit.isOnGround()) {
			return false;
		}
		LivingEntity owner = this.tameable.getOwner();
		if (owner == null) {
			return this.tameable.isSitting();
		}
		if (this.rabbit.squaredDistanceTo(owner) < 144.0 && owner.getAttacker() != null) {
			return false;
		}
		return this.tameable.isSitting();
	}

	@Override
	public boolean shouldContinue() {
		return this.tameable.isSitting();
	}

	@Override
	public void start() {
		this.rabbit.getNavigation().stop();
	}

	@Override
	public void tick() {
		this.rabbit.getNavigation().stop();
		this.rabbit.setJumping(false);
	}
}
