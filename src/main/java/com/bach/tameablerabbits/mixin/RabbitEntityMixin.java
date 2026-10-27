package com.bach.tameablerabbits.mixin;

import com.bach.tameablerabbits.RabbitTameable;
import com.bach.tameablerabbits.goal.RabbitFollowOwnerGoal;
import com.bach.tameablerabbits.goal.RabbitSitGoal;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(RabbitEntity.class)
public abstract class RabbitEntityMixin extends AnimalEntity implements RabbitTameable {
	@Unique
	private static final TrackedData<Byte> TAMEABLE_FLAGS =
		DataTracker.registerData(RabbitEntity.class, TrackedDataHandlerRegistry.BYTE);
	@Unique
	private static final TrackedData<Optional<UUID>> OWNER_UUID =
		DataTracker.registerData(RabbitEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	@Unique
	private static final byte TAMED_FLAG = 1;
	@Unique
	private static final byte SITTING_FLAG = 2;

	protected RabbitEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void tameableRabbits$initDataTracker(CallbackInfo ci) {
		this.dataTracker.startTracking(TAMEABLE_FLAGS, (byte) 0);
		this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
	}

	@Inject(method = "initGoals", at = @At("TAIL"))
	private void tameableRabbits$initGoals(CallbackInfo ci) {
		RabbitEntity self = (RabbitEntity) (Object) this;
		this.goalSelector.add(1, new RabbitSitGoal(self));
		this.goalSelector.add(4, new RabbitFollowOwnerGoal(self, 1.0, 8.0F, 2.0F));
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void tameableRabbits$writeNbt(NbtCompound nbt, CallbackInfo ci) {
		if (this.getOwnerUuid() != null) {
			nbt.putUuid("Owner", this.getOwnerUuid());
		}
		nbt.putBoolean("Sitting", this.isSitting());
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void tameableRabbits$readNbt(NbtCompound nbt, CallbackInfo ci) {
		UUID uuid = null;
		if (nbt.containsUuid("Owner")) {
			uuid = nbt.getUuid("Owner");
		}
		if (uuid != null) {
			this.setOwnerUuid(uuid);
			this.setTamed(true);
			this.setPersistent();
		}
		this.setSitting(nbt.getBoolean("Sitting"));
	}

	@Inject(method = "handleStatus", at = @At("HEAD"), cancellable = true)
	private void tameableRabbits$handleStatus(byte status, CallbackInfo ci) {
		if (status == EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES) {
			this.tameableRabbits$showEmoteParticle(true);
			ci.cancel();
		} else if (status == EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES) {
			this.tameableRabbits$showEmoteParticle(false);
			ci.cancel();
		}
	}

	@Inject(method = "createChild", at = @At("RETURN"))
	private void tameableRabbits$tameBaby(ServerWorld world, PassiveEntity other, CallbackInfoReturnable<RabbitEntity> cir) {
		RabbitEntity baby = cir.getReturnValue();
		if (baby == null || !this.isTamed()) {
			return;
		}
		RabbitTameable tameableBaby = (RabbitTameable) baby;
		tameableBaby.setOwnerUuid(this.getOwnerUuid());
		tameableBaby.setTamed(true);
		baby.setPersistent();
	}

	@Inject(method = "wantsCarrots", at = @At("HEAD"), cancellable = true)
	private void tameableRabbits$noFarmRaidWhenTamed(CallbackInfoReturnable<Boolean> cir) {
		if (this.isTamed()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "startJump", at = @At("HEAD"), cancellable = true)
	private void tameableRabbits$noJumpWhenSitting(CallbackInfo ci) {
		if (this.isSitting()) {
			ci.cancel();
		}
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (this.getWorld().isClient) {
			boolean swing = this.isOwner(player) || this.isTamed() || this.tameableRabbits$isTamingItem(stack) && !this.isTamed();
			return swing ? ActionResult.CONSUME : super.interactMob(player, hand);
		}

		if (!this.isTamed() && this.tameableRabbits$isTamingItem(stack)) {
			if (!player.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			if (this.random.nextInt(3) == 0) {
				this.setOwner(player);
				this.setTamed(true);
				this.setSitting(true);
				this.setPersistent();
				this.navigation.stop();
				this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
			} else {
				this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
			}
			return ActionResult.SUCCESS;
		}

		if (this.isTamed()) {
			if (this.isBreedingItem(stack) && this.getHealth() < this.getMaxHealth() && !this.isBaby()) {
				this.eat(player, hand, stack);
				this.heal(3.0F);
				return ActionResult.SUCCESS;
			}
			ActionResult result = super.interactMob(player, hand);
			if (result.isAccepted()) {
				return result;
			}
			if (this.isOwner(player) && !this.tameableRabbits$isTamingItem(stack)) {
				this.setSitting(!this.isSitting());
				this.jumping = false;
				this.navigation.stop();
				this.setTarget(null);
				return ActionResult.SUCCESS;
			}
			return result;
		}

		return super.interactMob(player, hand);
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return !this.isTamed() && super.canImmediatelyDespawn(distanceSquared);
	}

	@Override
	public boolean cannotDespawn() {
		return this.isTamed() || super.cannotDespawn();
	}

	@Override
	public boolean isTamed() {
		return (this.dataTracker.get(TAMEABLE_FLAGS) & TAMED_FLAG) != 0;
	}

	@Override
	public void setTamed(boolean tamed) {
		this.tameableRabbits$setFlag(TAMED_FLAG, tamed);
	}

	@Override
	public boolean isSitting() {
		return (this.dataTracker.get(TAMEABLE_FLAGS) & SITTING_FLAG) != 0;
	}

	@Override
	public void setSitting(boolean sitting) {
		this.tameableRabbits$setFlag(SITTING_FLAG, sitting);
	}

	@Override
	@Nullable
	public UUID getOwnerUuid() {
		return this.dataTracker.get(OWNER_UUID).orElse(null);
	}

	@Override
	public void setOwnerUuid(@Nullable UUID uuid) {
		this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
	}

	@Override
	@Nullable
	public LivingEntity getOwner() {
		UUID uuid = this.getOwnerUuid();
		if (uuid == null) {
			return null;
		}
		return this.getWorld().getPlayerByUuid(uuid);
	}

	@Override
	public void setOwner(PlayerEntity player) {
		this.setTamed(true);
		this.setOwnerUuid(player.getUuid());
	}

	@Override
	public boolean isOwner(LivingEntity entity) {
		return entity == this.getOwner();
	}

	@Unique
	private void tameableRabbits$setFlag(byte flag, boolean value) {
		byte flags = this.dataTracker.get(TAMEABLE_FLAGS);
		if (value) {
			this.dataTracker.set(TAMEABLE_FLAGS, (byte) (flags | flag));
		} else {
			this.dataTracker.set(TAMEABLE_FLAGS, (byte) (flags & ~flag));
		}
	}

	@Unique
	private boolean tameableRabbits$isTamingItem(ItemStack stack) {
		return stack.isOf(Items.CARROT) || stack.isOf(Items.GOLDEN_CARROT) || stack.isOf(Items.DANDELION);
	}

	@Unique
	private void tameableRabbits$showEmoteParticle(boolean positive) {
		ParticleEffect particle = positive ? ParticleTypes.HEART : ParticleTypes.SMOKE;
		for (int i = 0; i < 7; i++) {
			double dx = this.random.nextGaussian() * 0.02;
			double dy = this.random.nextGaussian() * 0.02;
			double dz = this.random.nextGaussian() * 0.02;
			this.getWorld().addParticle(particle, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), dx, dy, dz);
		}
	}
}
