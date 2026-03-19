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

import net.frozenblock.lib.sound.impl.networking.FrozenLibSoundPackets;
import net.lunade.camera.component.CameraContents;
import net.lunade.camera.item.CameraItem;
import net.lunade.camera.networking.packet.CameraTakeScreenshotPacket;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortSounds;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TripodCamera extends Mob {
	private static final EntityDataAccessor<Float> TRACKED_HEIGHT = SynchedEntityData.defineId(TripodCamera.class, EntityDataSerializers.FLOAT);
	private CameraContents cameraContents = CameraContents.EMPTY;
	private EntityReference<Player> photographer = null;
	public long lastHit;
	private long photographAtTick;
	public float prevTimer;
	public float timer;
	private boolean goingUp = false;

	public TripodCamera(EntityType<? extends TripodCamera> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createTripodCameraAttributes() {
		return createMobAttributes().add(Attributes.STEP_HEIGHT, 0D);
	}

	@Override
	protected EntityDimensions getDefaultDimensions(Pose pose) {
		return EntityDimensions.scalable(this.getBoundingBoxRadius() * 2F, this.getTrackedHeight()).scale(this.getAgeScale());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(TRACKED_HEIGHT, 1.75F);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		super.onSyncedDataUpdated(accessor);
		if (accessor == TRACKED_HEIGHT) this.refreshDimensions();
	}

	@Nullable
	@Override
	public <T> T get(DataComponentType<? extends T> type) {
		if (type == CameraPortDataComponents.CAMERA_CONTENTS) return castComponentValue(type, this.cameraContents);
		return super.get(type);
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		this.applyImplicitComponentIfPresent(components, CameraPortDataComponents.CAMERA_CONTENTS);
		super.applyImplicitComponents(components);
	}

	@Override
	protected <T> boolean applyImplicitComponent(final DataComponentType<T> type, final T value) {
		if (type == CameraPortDataComponents.CAMERA_CONTENTS) {
			this.cameraContents = castComponentValue(CameraPortDataComponents.CAMERA_CONTENTS, value);
			return true;
		}
		return super.applyImplicitComponent(type, value);
	}

	@Override
	public void tick() {
		super.tick();
		this.prevTimer = this.timer;
		this.timer = Math.max(0, this.prevTimer -= 1);

		handlePhotograph: {
			if (!(this.level() instanceof ServerLevel level) || this.photographAtTick == 0L) break handlePhotograph;

			if (!(EntityReference.getPlayer(this.photographer, this.level()) instanceof ServerPlayer photographer)) {
				this.photographAtTick = 0L;
				this.photographer = null;
				break handlePhotograph;
			}

			this.getLookControl().setLookAt(photographer);
			if (level.getGameTime() < this.photographAtTick) break handlePhotograph;

			this.photographer = null;
			this.photographAtTick = 0L;

			boolean isSuccess = true;
			final String fileName = CameraItem.makeFileName(photographer);
			final CameraContents cameraContents = CameraItem.addPhotograph(this.cameraContents, photographer, fileName);
			if (cameraContents == null || cameraContents == this.cameraContents) isSuccess = false;

			final float pitch = isSuccess ? 0.95F + level.getRandom().nextFloat() * 0.1F : 0.8F + level().getRandom().nextFloat() * 0.4F;
			if (!this.isSilent()) {
				level.playSound(
					photographer,
					this.getX(), this.getY(), this.getZ(),
					isSuccess ? CameraPortSounds.CAMERA_SNAP : CameraPortSounds.CAMERA_SNAP_FAIL,
					this.getSoundSource(),
					this.getSoundVolume(),
					pitch
				);
			}
			FrozenLibSoundPackets.createAndSendLocalPlayerSound(
				photographer,
				BuiltInRegistries.SOUND_EVENT.wrapAsHolder(isSuccess ? CameraPortSounds.CAMERA_SNAP : CameraPortSounds.CAMERA_SNAP_FAIL),
				0.5F,
				pitch
			);

			if (isSuccess) {
				CameraTakeScreenshotPacket.sendTo(photographer, this, fileName);
				this.setComponent(CameraPortDataComponents.CAMERA_CONTENTS, cameraContents);
			}
		}
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
		if (player.isSpectator()) return InteractionResult.SUCCESS;
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
				this.playSound(CameraPortSounds.CAMERA_ADJUST, this.getSoundVolume(), 0.9F + (this.getTrackedHeight() * 0.1F));
				return InteractionResult.SUCCESS;
			}
		} else if (this.photographAtTick == 0) {
			if (this.cameraContents.isEmpty() && this.tryInsertItem(player.getItemInHand(hand))) {
				CameraItem.playInsertSound(this);
				return InteractionResult.SUCCESS;
			} else if (!this.cameraContents.hasSpaceForPhotograph() && this.tryTakeItem(player, hand)) {
				CameraItem.playRemoveOneSound(this);
				return InteractionResult.SUCCESS;
			}

			final ItemStack pickResult = this.getPickResult();
			if (pickResult != null && player.getCooldowns().isOnCooldown(pickResult)) {
				this.playSound(
					this.cameraContents.isEmpty() || this.cameraContents.hasSpaceForPhotograph() ? CameraPortSounds.CAMERA_PRIME_FAIL : CameraPortSounds.CAMERA_PRIME_FAIL_FULL,
					this.getSoundVolume(),
					0.9F + this.level().getRandom().nextFloat() * 0.2F
				);
				return InteractionResult.FAIL;
			}

			if (!this.cameraContents.hasSpaceForPhotograph()) {
				this.playSound(
					this.cameraContents.isEmpty() ? CameraPortSounds.CAMERA_PRIME_FAIL : CameraPortSounds.CAMERA_PRIME_FAIL_FULL,
					this.getSoundVolume(),
					0.9F + this.level().getRandom().nextFloat() * 0.2F
				);
				return InteractionResult.FAIL;
			}

			CameraItem.setAllCamerasOnCooldown(player, 60);
			this.photographer = EntityReference.of(player);
			this.photographAtTick = this.level().getGameTime() + 60L;
			this.level().broadcastEntityEvent(this, EntityEvent.TENDRILS_SHIVER);
			this.playSound(CameraPortSounds.CAMERA_PRIME, this.getSoundVolume(), 1F);
			return InteractionResult.SUCCESS;
		} else {
			this.playSound(CameraPortSounds.CAMERA_ALREADY_PRIMED, this.getSoundVolume(), 0.8F + this.level().getRandom().nextFloat() * 0.4F);
		}

		return super.interact(player, hand, location);
	}

	public boolean tryInsertItem(ItemStack other) {
		if (other.isEmpty()) return false;

		final CameraContents initialContents = this.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (initialContents == null) return false;

		final CameraContents.Mutable contents = new CameraContents.Mutable(initialContents);
		if (contents.tryInsert(other) <= 0) return false;

		this.setComponent(CameraPortDataComponents.CAMERA_CONTENTS, contents.toImmutable());
		return true;
	}

	public boolean tryTakeItem(Player player, InteractionHand hand) {
		final ItemStack heldStack = player.getItemInHand(hand);
		if (!heldStack.isEmpty()) return false;

		final CameraContents initialContents = this.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (initialContents == null) return false;

		final CameraContents.Mutable contents = new CameraContents.Mutable(initialContents);
		final ItemStack removed = contents.removeOne();
		if (removed == null) return false;

		player.setItemInHand(hand, removed);
		this.setComponent(CameraPortDataComponents.CAMERA_CONTENTS, contents.toImmutable());
		return true;
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		if (this.photographer != null && this.photographer.matches(player)) this.photographer = null;
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

		boolean allowIncrementalBreaking = source.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
		boolean shouldKill = source.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
		if (!allowIncrementalBreaking && !shouldKill) return false;

		if (source.getEntity() instanceof Player player && !player.getAbilities().mayBuild) return false;

		if (source.isCreativePlayer()) {
			this.playBrokenSound();
			this.showBreakingParticles();
			this.kill(level);
			return true;
		}

		long time = level.getGameTime();
		if (time - this.lastHit > 5L && !shouldKill) {
			level.broadcastEntityEvent(this, EntityEvent.ARMORSTAND_WOBBLE);
			this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
			this.lastHit = time;
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

	private void causeDamage(ServerLevel level, DamageSource source, float dmg) {
		float health = this.getHealth();
		health -= dmg;
		if (health <= 0.5F) {
			this.brokenByAnything(level, source);
			this.kill(level);
		} else {
			this.setHealth(health);
			this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
		}
	}

	private void brokenByPlayer(ServerLevel level, DamageSource source) {
		final ItemStack pickResult = this.getPickResult();
		pickResult.set(DataComponents.CUSTOM_NAME, this.getCustomName());
		Block.popResource(this.level(), this.blockPosition(), pickResult);
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
		if (id == EntityEvent.ARMORSTAND_WOBBLE) {
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
			return;
		}

		if (id == EntityEvent.TENDRILS_SHIVER) {
			if (!this.level().isClientSide()) return;
			this.timer = 60F;
			this.prevTimer = 60F;
			return;
		}

		super.handleEntityEvent(id);
	}

	@Override
	public void kill(ServerLevel level) {
		this.remove(Entity.RemovalReason.KILLED);
		this.gameEvent(GameEvent.ENTITY_DIE);
	}

	@Override
	public boolean skipAttackInteraction(Entity source) {
		return source instanceof Player player && !this.level().mayInteract(player, this.blockPosition());
	}

	@Override
	public void thunderHit(ServerLevel level, LightningBolt lightningBolt) {
	}

	@Override
	public boolean attackable() {
		return false;
	}

	@Override
	public void doPush(Entity entity) {
	}

	@Override
	@Nullable
	public ItemStack getPickResult() {
		final ItemStack stack = super.getPickResult();
		if (stack == null) return stack;
		final CameraContents cameraContents = this.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (cameraContents != null) stack.set(CameraPortDataComponents.CAMERA_CONTENTS, cameraContents);
		return stack;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		return false;
	}

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
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putFloat("CurrentHeight", this.getTrackedHeight());
		output.putBoolean("HeightAdjustmentMovesUp", this.goingUp);
		output.store("CameraContents", CameraContents.CODEC, this.cameraContents);
	}

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setTrackedHeight(input.getFloatOr("CurrentHeight", 1.75F));
		this.goingUp = input.getBooleanOr("HeightAdjustmentMovesUp", false);
		this.cameraContents = input.read("CameraContents", CameraContents.CODEC).orElse(CameraContents.EMPTY);
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
}
