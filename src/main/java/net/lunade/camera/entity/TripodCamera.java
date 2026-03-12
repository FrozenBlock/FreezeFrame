/*
 * Copyright 2026 FrozenBlock
 * This file is part of Camera Port.
 *
 * This program is free software; you can modify it under
 * the terms of version 1 of the FrozenBlock Modding Oasis License
 * as published by FrozenBlock Modding Oasis.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * FrozenBlock Modding Oasis License for more details.
 *
 * You should have received a copy of the FrozenBlock Modding Oasis License
 * along with this program; if not, see <https://github.com/FrozenBlock/Licenses>.
 */

package net.lunade.camera.entity;

import net.lunade.camera.networking.packet.CameraTakeScreenshotPacket;
import net.lunade.camera.registry.CameraPortSounds;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class TripodCamera extends Mob {
	private static final EntityDataAccessor<Float> TRACKED_HEIGHT = SynchedEntityData.defineId(TripodCamera.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> TIMER = SynchedEntityData.defineId(TripodCamera.class, EntityDataSerializers.INT);
	private EntityReference<Player> photographer = null;
	public long lastHit;
	public float prevTimer;
	public float timer;
	private boolean goingUp = false;

	public TripodCamera(EntityType<? extends Mob> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
		this.getNavigation().setCanFloat(false);
	}

	public static AttributeSupplier.Builder createTripodCameraAttributes() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.FOLLOW_RANGE, 80D)
			.add(Attributes.ATTACK_KNOCKBACK)
			.add(Attributes.MOVEMENT_SPEED, 0D)
			.add(Attributes.KNOCKBACK_RESISTANCE, 100D)
			.add(Attributes.STEP_HEIGHT, 0D);
	}

	@Override
	protected EntityDimensions getDefaultDimensions(Pose pose) {
		return EntityDimensions.scalable(this.getBoundingBoxRadius() * 2F, this.getTrackedHeight()).scale(this.getAgeScale());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(TRACKED_HEIGHT, 1.75F);
		entityData.define(TIMER, 0);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		super.onSyncedDataUpdated(accessor);
		if (accessor == TRACKED_HEIGHT) this.refreshDimensions();
	}

	@Override
	public void tick() {
		super.tick();
		this.prevTimer = this.getTimer();

		handlePhotograph: {
			if (!(this.level() instanceof ServerLevel level) || this.getTimer() <= 0) break handlePhotograph;

			this.setTimer(this.getTimer() - 1);
			if (!(EntityReference.getPlayer(this.photographer, this.level()) instanceof ServerPlayer photographer)) {
				this.setTimer(0);
				this.photographer = null;
				break handlePhotograph;
			}

			this.getLookControl().setLookAt(photographer);
			if (this.getTimer() != 1) break handlePhotograph;

			this.photographer = null;
			level.playSound(photographer, this.getX(), this.getY(), this.getZ(), CameraPortSounds.CAMERA_SNAP, this.getSoundSource(), 0.5F, 1F);
			// TODO: film & photographer
			CameraTakeScreenshotPacket.sendTo(photographer, this, "");
		}

		this.timer = Math.max(0, this.prevTimer -= 1);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (this.level().isClientSide()) return InteractionResult.SUCCESS;

		if (player.isShiftKeyDown()) {
			if (this.canBeAdjusted()) {
				float change = this.goingUp ? 0.095F : -0.095F;
				float newHeight = (this.getTrackedHeight() + change);
				if (newHeight >= getMaxHeight()) {
					this.goingUp = false;
					newHeight = this.getMaxHeight();
				} else if (newHeight <= this.getMinHeight()) {
					this.goingUp = true;
					newHeight = this.getMinHeight();
				}
				this.setTrackedHeight(newHeight);
				this.level().playSound(null, getX(), getEyeY(), getZ(), CameraPortSounds.CAMERA_ADJUST, SoundSource.NEUTRAL, this.getSoundVolume(), this.getTrackedHeight());
				return InteractionResult.SUCCESS;
			}
		} else if (this.getTimer() <= 0) {
			this.setTimer(60);
			this.photographer = EntityReference.of(player);
			this.playSound(CameraPortSounds.CAMERA_PRIME, this.getSoundVolume(), this.getVoicePitch());
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public float getMaxHeight() {
		return 1.75F;
	}

	public float getMinHeight() {
		return 0.8F;
	}

	public float getBoundingBoxRadius() {
		return 0.3F;
	}

	public boolean canBeAdjusted() {
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (this.isRemoved()) return false;
		if (!level.getGameRules().get(GameRules.MOB_GRIEFING) && source.getEntity() instanceof Mob) return false;
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			this.kill(level);
			return false;
		}
		if (this.isInvulnerableTo(level, source)) return false;
		if (source.is(DamageTypeTags.IS_EXPLOSION)) {
			this.brokenByAnything(level, source);
			this.kill(level);
			return false;
		}
		if (source.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
			if (this.isOnFire()) {
				this.causeDamage(level, source, 0.15F);
			} else {
				this.igniteForSeconds(5F);
			}
			return false;
		}

		if (source.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
			this.causeDamage(level, source, 4F);
			return false;
		}

		boolean canBreak = source.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
		boolean alwaysKills = source.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
		if (!canBreak && !alwaysKills) return false;

		if (source.getEntity() instanceof Player player && !player.getAbilities().mayBuild) return false;

		if (source.isCreativePlayer()) {
			this.playBrokenSound();
			this.showBreakingParticles();
			this.kill(level);
			return true;
		}

		long gameTime = level.getGameTime();
		if (gameTime - this.lastHit > 5L && !alwaysKills) {
			level.broadcastEntityEvent(this, EntityEvent.ARMORSTAND_WOBBLE);
			this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
			this.lastHit = gameTime;
		} else {
			this.brokenByPlayer(level, source);
			this.showBreakingParticles();
			this.kill(level);
		}

		return true;
	}

	private void showBreakingParticles() {
		if (!(this.level() instanceof ServerLevel level)) return;

		level.sendParticles(
			new ItemParticleOption(ParticleTypes.ITEM, Items.STICK),
			this.getX(),
			this.getY(0.6666666666666666D),
			this.getZ(),
			10,
			this.getBbWidth() / 4F,
			this.getBbHeight() / 4F,
			this.getBbWidth() / 4F,
			0.05D
		);
	}

	private void causeDamage(ServerLevel level, DamageSource source, float amount) {
		float health = this.getHealth();
		health -= amount;
		if (health <= 0.5F) {
			this.brokenByAnything(level, source);
			this.kill(level);
		} else {
			this.setHealth(health);
			this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
		}
	}

	private void brokenByPlayer(ServerLevel level, DamageSource source) {
		ItemStack itemStack = this.getPickResult();
		itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
		Block.popResource(this.level(), this.blockPosition(), itemStack);
		this.brokenByAnything(level, source);
	}

	private void brokenByAnything(ServerLevel level, DamageSource source) {
		this.playBrokenSound();
		this.dropAllDeathLoot(level, source);
	}

	private void playBrokenSound() {
		this.level().playSound(null, this.getX(), this.getY(), this.getZ(), CameraPortSounds.CAMERA_BREAK, this.getSoundSource(), 1F, 1F);
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id != EntityEvent.ARMORSTAND_WOBBLE) {
			super.handleEntityEvent(id);
			return;
		}

		if (!this.level().isClientSide()) return;

		this.level().playLocalSound(
			this.getX(),
			this.getY(),
			this.getZ(),
			CameraPortSounds.CAMERA_HIT,
			this.getSoundSource(),
			0.3F,
			1F,
			false
		);
		this.lastHit = this.level().getGameTime();
	}

	@Override
	public void kill(ServerLevel level) {
		this.remove(Entity.RemovalReason.KILLED);
		this.gameEvent(GameEvent.ENTITY_DIE);
	}

	@Override
	public void doPush(Entity entity) {
	}

	@Override
	protected float getJumpPower() {
		return 0F;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		return false;
	}

	@NotNull
	@Override
	public Fallsounds getFallSounds() {
		return new Fallsounds(CameraPortSounds.CAMERA_FALL, CameraPortSounds.CAMERA_FALL);
	}

	@Override
	public SoundEvent getHurtSound(DamageSource source) {
		return CameraPortSounds.CAMERA_HIT;
	}

	@Override
	public SoundEvent getDeathSound() {
		return CameraPortSounds.CAMERA_BREAK;
	}

	@Override
	public boolean canBeSeenAsEnemy() {
		return false;
	}

	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
	}

	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("ticksToPhoto", this.getTimer());
		if (this.photographer != null) output.store("photographer", UUIDUtil.CODEC, this.photographer.getUUID());
		output.putFloat("currentHeight", this.getTrackedHeight());
		output.putBoolean("goingUp", this.goingUp);
	}

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setTimer(input.getIntOr("ticksToPhoto", 0));
		this.photographer = EntityReference.read(input, "photographer");
		this.setTrackedHeight(input.getFloatOr("currentHeight", 1.75F));
		this.goingUp = input.getBooleanOr("goingUp", false);
	}

	public float getLerpedTimer(float partialTicks) {
		return Mth.lerp(partialTicks, this.prevTimer, this.timer);
	}

	public float getTrackedHeight() {
		return this.entityData.get(TRACKED_HEIGHT);
	}

	public void setTrackedHeight(float value) {
		this.entityData.set(TRACKED_HEIGHT, value);
	}

	public int getTimer() {
		return this.entityData.get(TIMER);
	}

	public void setTimer(int value) {
		this.entityData.set(TIMER, value);
	}
}
