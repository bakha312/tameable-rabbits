package com.bach.tameablerabbits.mixin;

import com.bach.tameablerabbits.RabbitTameable;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.RabbitEntityModel;
import net.minecraft.entity.passive.RabbitEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RabbitEntityModel.class)
public abstract class RabbitEntityModelMixin<T extends RabbitEntity> {
	@Shadow
	@Final
	private ModelPart leftHaunch;
	@Shadow
	@Final
	private ModelPart rightHaunch;
	@Shadow
	@Final
	private ModelPart leftFrontLeg;
	@Shadow
	@Final
	private ModelPart rightFrontLeg;
	@Shadow
	@Final
	private ModelPart body;

	@Inject(method = "setAngles(Lnet/minecraft/entity/passive/RabbitEntity;FFFFF)V", at = @At("TAIL"))
	private void tameableRabbits$sitPose(T rabbit, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
		if (!(rabbit instanceof RabbitTameable tameable) || !tameable.isSitting()) {
			return;
		}
		this.body.pivotY = 20.0F;
		this.leftHaunch.pitch = -0.4F;
		this.rightHaunch.pitch = -0.4F;
		this.leftFrontLeg.pitch = -1.3F;
		this.rightFrontLeg.pitch = -1.3F;
		this.leftFrontLeg.pivotY = 18.2F;
		this.rightFrontLeg.pivotY = 18.2F;
	}
}
