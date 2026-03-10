package net.lunade.camera.menu;

import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.registry.CameraPortBlocks;
import net.lunade.camera.registry.CameraPortItems;
import net.lunade.camera.registry.CameraPortMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PrinterMenu extends AbstractContainerMenu {
	public static final int INPUT_SLOT = 0;
	public static final int RESULT_SLOT = 1;
	private static final int INV_SLOT_START = 2;
	private static final int INV_SLOT_END = 29;
	private static final int USE_ROW_SLOT_START = 29;
	private static final int USE_ROW_SLOT_END = 38;
	protected final ContainerLevelAccess access;
	private final DataSlot pictureSlotsSize = DataSlot.standalone();
	private ItemStack input = ItemStack.EMPTY;
	protected String photoId;
	long lastSoundTime;
	final Slot inputSlot;
	final Slot resultSlot;
	Runnable slotUpdateListener = () -> {};
	public final Container inputContainer = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			PrinterMenu.this.slotsChanged(this);
			PrinterMenu.this.slotUpdateListener.run();
		}
	};
	final ResultContainer resultContainer = new ResultContainer();

	public PrinterMenu(int id, Inventory inventory) {
		this(id, inventory, ContainerLevelAccess.NULL);
	}

	public PrinterMenu(int id, Inventory inventory, ContainerLevelAccess access) {
		super(CameraPortMenuTypes.PRINTER, id);
		this.access = access;
		this.inputSlot = addSlot(new PrinterPaperSlot(this.inputContainer, INPUT_SLOT, 8, 18));
		this.resultSlot = addSlot(new PrinterResultSlot(this, this.resultContainer, RESULT_SLOT, 152, 18));
		this.addStandardInventorySlots(inventory, 8, 140);
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
		final ItemStack inputStack = this.inputSlot.getItem();
		if (!inputStack.is(this.input.getItem())) {
			this.input = inputStack.copy();
			this.resultSlot.set(ItemStack.EMPTY);
		}
	}

	void setupResultSlot(Player player) {
		if (this.pictureSlotsSize.get() != 0 && this.inputSlot.getItem().is(Items.PAPER)) {
			final ItemStack stack = new ItemStack(CameraPortItems.PHOTOGRAPH);
			final String photographName = this.photoId.replace("photographs/", "");
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
		final Slot slot = this.slots.get(fromIndex);
		ItemStack clicked = ItemStack.EMPTY;
		if (slot == null || !slot.hasItem()) return clicked;

		final ItemStack item = slot.getItem();
		clicked = item.copy();
		if (fromIndex == RESULT_SLOT) {
			item.onCraftedBy(player, 1);
			if (!this.moveItemStackTo(item, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) return ItemStack.EMPTY;
			slot.onQuickCraft(item, clicked);
		} else if (fromIndex == INPUT_SLOT) {
			if (!this.moveItemStackTo(item, USE_ROW_SLOT_START, USE_ROW_SLOT_END, true)) return ItemStack.EMPTY;
		} else if (item.is(Items.PAPER)) {
			if (!this.moveItemStackTo(item, 0, 1, false)) return ItemStack.EMPTY;
		} else if (fromIndex >= INV_SLOT_START && fromIndex < INV_SLOT_END) {
			if (!this.moveItemStackTo(item, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) return ItemStack.EMPTY;
		} else if (fromIndex >= USE_ROW_SLOT_START && fromIndex < USE_ROW_SLOT_END && !this.moveItemStackTo(item, INV_SLOT_START, INV_SLOT_END, false)) {
			return ItemStack.EMPTY;
		}

		if (item.isEmpty()) slot.set(ItemStack.EMPTY);

		slot.setChanged();
		if (item.getCount() == clicked.getCount()) return ItemStack.EMPTY;

		slot.onTake(player, item);
		this.broadcastChanges();
		this.setupResultSlot(player);

		return clicked;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.resultContainer.removeItemNoUpdate(1);
		this.access.execute((level, pos) -> this.clearContainer(player, this.inputContainer));
	}

	public void setupDataAndResultSlot(Player player, int size, String photoId) {
		this.pictureSlotsSize.set(size);
		this.photoId = photoId;
		this.setupResultSlot(player);
	}
}
