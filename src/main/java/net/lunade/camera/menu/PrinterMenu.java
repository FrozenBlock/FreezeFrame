package net.lunade.camera.menu;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.lib.file.transfer.FileTransferPacket;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.CameraPortMain;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.registry.CameraPortBlocks;
import net.lunade.camera.registry.CameraPortItems;
import net.lunade.camera.registry.CameraPortMenuTypes;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PrinterMenu extends AbstractContainerMenu {
	private final ContainerLevelAccess access;
	private final DataSlot pictureSlotsSize = DataSlot.standalone();
	private ItemStack input = ItemStack.EMPTY;
	private String temp;
	long lastSoundTime;
	final Slot inputSlot;
	final Slot resultSlot;
	Runnable slotUpdateListener = () -> {};
	public final Container container = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			PrinterMenu.this.slotsChanged(this);
			PrinterMenu.this.slotUpdateListener.run();
		}
	};
	final ResultContainer resultContainer = new ResultContainer();

	public PrinterMenu(int id, Inventory playerInventory) {
		this(id, playerInventory, ContainerLevelAccess.NULL);
	}

	public PrinterMenu(int id, Inventory playerInventory, ContainerLevelAccess context) {
		super(CameraPortMenuTypes.PRINTER, id);

		this.access = context;
		this.inputSlot = addSlot(
			new Slot(this.container, 0, 8, 18) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return stack.is(Items.PAPER);
				}
			}
		);

		this.resultSlot = addSlot(
			new Slot(this.resultContainer, 1, 152, 18) {

				@Override
				public boolean mayPlace(ItemStack stack) {
					return false;
				}

				@Override
				public void onTake(Player player, ItemStack stack) {
					stack.onCraftedBy(player, stack.getCount());
					final ItemStack input = PrinterMenu.this.inputSlot.remove(1);
					if (!input.isEmpty()) PrinterMenu.this.setupResultSlot(player);

					PrinterMenu.this.access.execute((level, blockPos) -> {
						long l = level.getGameTime();
						if (PrinterMenu.this.lastSoundTime != l) {
							level.playSound(null, blockPos, CameraPortMain.CAMERA_SNAP, SoundSource.BLOCKS, 1F, 1F);
							PrinterMenu.this.lastSoundTime = l;
						}
					});
					if (player instanceof ServerPlayer serverPlayer) {
						serverPlayer.connection.send(
							new ClientboundCustomPayloadPacket(
								FileTransferPacket.createRequest(
									"photographs",
									PrinterMenu.this.temp.replace("photographs/", "") + ".png"
								)
							)
						);
					}
					super.onTake(player, stack);
				}
			});

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 198));
		}

		this.addDataSlot(this.pictureSlotsSize);
	}

	public boolean hasInputItem() {
		return this.inputSlot.hasItem() && pictureSlotsSize.get() != 0;
	}

	public ItemStack getInputItem() {
		return this.inputSlot.getItem();
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, CameraPortBlocks.PRINTER);
	}

	@Override
	public void slotsChanged(Container container) {
		ItemStack itemstack = this.inputSlot.getItem();
		if (!itemstack.is(this.input.getItem())) {
			this.input = itemstack.copy();
			this.resultSlot.set(ItemStack.EMPTY);
		}
	}

	void setupResultSlot(Player player) {
		if (this.pictureSlotsSize.get() != 0 && this.inputSlot.getItem().is(Items.PAPER)) {
			final ItemStack stack = new ItemStack(CameraPortItems.PHOTOGRAPH);
			final String photographName = this.temp.replace("photographs/", "");
			stack.set(
				CameraPortItems.PHOTO_COMPONENT,
				new PhotographComponent(
					CameraPortConstants.id("photographs/" + photographName),
					player.getPlainTextName()
				)
			);
			this.resultSlot.set(stack);
		} else {
			this.resultSlot.set(ItemStack.EMPTY);
		}
		this.broadcastChanges();
	}

	public void registerUpdateListener(Runnable listener) {
		this.slotUpdateListener = listener;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != this.resultContainer && super.canTakeItemForPickAll(stack, slot);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int fromIndex) {
		Slot slot = this.slots.get(fromIndex);
		ItemStack itemStack = ItemStack.EMPTY;
		if (slot.hasItem()) {
			ItemStack itemStack1 = slot.getItem();
			Item item = itemStack1.getItem();
			itemStack = itemStack1.copy();
			if (fromIndex == 1) {
				item.onCraftedBy(itemStack1, player);
				if (!this.moveItemStackTo(itemStack1, 2, 38, false)) return ItemStack.EMPTY;
				slot.onQuickCraft(itemStack1, itemStack);
			} else if (fromIndex == 0) {
				if (!this.moveItemStackTo(itemStack1, 2, 38, true)) return ItemStack.EMPTY;
			} else if (itemStack1.is(Items.PAPER)) {
				if (!this.moveItemStackTo(itemStack1, 0, 1, false)) return ItemStack.EMPTY;
			} else if (fromIndex >= 2 && fromIndex < 29) {
				if (!this.moveItemStackTo(itemStack1, 29, 38, false)) return ItemStack.EMPTY;
			} else if (fromIndex >= 29 && fromIndex < 38 && !this.moveItemStackTo(itemStack1, 2, 29, false))
				return ItemStack.EMPTY;

			if (itemStack1.isEmpty()) slot.set(ItemStack.EMPTY);

			slot.setChanged();
			if (itemStack1.getCount() == itemStack.getCount()) return ItemStack.EMPTY;

			slot.onTake(player, itemStack1);
			this.broadcastChanges();
			this.setupResultSlot(player);
		}
		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.resultContainer.removeItemNoUpdate(1);
		this.access.execute((level, pos) -> this.clearContainer(player, this.container));
	}

	public void setupData(Player player, int size, String id) {
		this.pictureSlotsSize.set(size);
		this.temp = id;
		this.setupResultSlot(player);
	}

	@Environment(EnvType.CLIENT)
	public void onClient(String selected) {
		this.temp = selected;
	}
}
