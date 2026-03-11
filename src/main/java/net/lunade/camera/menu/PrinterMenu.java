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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.TransmuteRecipe;

public class PrinterMenu extends AbstractContainerMenu {
	public static final int SOURCE_SLOT = 0;
	public static final int PAPER_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	private static final int INV_SLOT_START = 3;
	private static final int INV_SLOT_END = 30;
	private static final int USE_ROW_SLOT_START = 30;
	private static final int USE_ROW_SLOT_END = 39;
	private static final ItemStackTemplate PHOTOGRAPH_COPY_TEMPLATE = new ItemStackTemplate(CameraPortItems.PHOTOGRAPH, 1);
	protected final ContainerLevelAccess access;
	private final Player player;
	private final DataSlot pictureSlotsSize = DataSlot.standalone();
	protected String photoId;
	long lastSoundTime;
	final Slot sourceSlot;
	final Slot paperSlot;
	final Slot resultSlot;
	Runnable slotUpdateListener = () -> {};
	public final Container inputContainer = new SimpleContainer(2) {
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
		this.player = inventory.player;
		this.sourceSlot = addSlot(new PrinterSourceSlot(this.inputContainer, SOURCE_SLOT, 14, 15));
		this.paperSlot = addSlot(new PrinterPaperSlot(this.inputContainer, PAPER_SLOT, 44, 109));
		this.resultSlot = addSlot(new PrinterResultSlot(this, this.resultContainer, RESULT_SLOT, 116, 109));
		this.addStandardInventorySlots(inventory, 8, 140);
		this.addDataSlot(this.pictureSlotsSize);
	}

	public boolean hasSourceItem() {
		return this.sourceSlot.hasItem();
	}

	public ItemStack getSourceItem() {
		return this.sourceSlot.getItem();
	}

	public boolean hasPaper() {
		return this.paperSlot.hasItem() && this.paperSlot.getItem().is(Items.PAPER);
	}

	public boolean hasPhotographSlots() {
		return this.pictureSlotsSize.get() != 0;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, CameraPortBlocks.PRINTER);
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		this.setupResultSlot(this.player);
		this.broadcastChanges();
	}

	void setupResultSlot(Player player) {
		if (!this.hasPaper() || !this.hasSourceItem()) {
			this.resultSlot.set(ItemStack.EMPTY);
			this.broadcastChanges();
			return;
		}

		final ItemStack sourceStack = this.getSourceItem();
		ItemStack stack = ItemStack.EMPTY;
		if (sourceStack.is(CameraPortItems.PHOTOGRAPH)) {
			final PhotographComponent photographComponent = sourceStack.get(CameraPortItems.PHOTO_COMPONENT);
			stack = TransmuteRecipe.createWithOriginalComponents(PHOTOGRAPH_COPY_TEMPLATE, sourceStack);
			stack.set(CameraPortItems.PHOTO_COMPONENT, photographComponent.asCopy());
		} else if (this.hasPhotographSlots() && sourceStack.is(CameraPortItems.CAMERA)) {
			stack = new ItemStack(CameraPortItems.PHOTOGRAPH);
			final String photographName = this.photoId.replace("photographs/", "");
			stack.set(
				CameraPortItems.PHOTO_COMPONENT,
				new PhotographComponent(
					CameraPortConstants.id("photographs/" + photographName),
					player.getPlainTextName()
				)
			);
		}

		this.resultSlot.set(stack);
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
			if (!this.moveItemStackTo(item, INV_SLOT_START, USE_ROW_SLOT_END, true)) return ItemStack.EMPTY;
			slot.onQuickCraft(item, clicked);
		} else if (fromIndex == SOURCE_SLOT) {
			if (!this.moveItemStackTo(item, INV_SLOT_START, USE_ROW_SLOT_END, false)) return ItemStack.EMPTY;
		} else if (item.is(CameraPortItems.CAMERA)) {
			if (!this.moveItemStackTo(item, SOURCE_SLOT, SOURCE_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (item.is(CameraPortItems.PHOTOGRAPH) && item.has(CameraPortItems.PHOTO_COMPONENT) && !item.get(CameraPortItems.PHOTO_COMPONENT).isCopy()) {
			if (!this.moveItemStackTo(item, SOURCE_SLOT, SOURCE_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (fromIndex == PAPER_SLOT) {
			if (!this.moveItemStackTo(item, INV_SLOT_START, USE_ROW_SLOT_END, false)) return ItemStack.EMPTY;
		} else if (item.is(Items.PAPER)) {
			if (!this.moveItemStackTo(item, PAPER_SLOT, PAPER_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (fromIndex >= INV_SLOT_START && fromIndex < INV_SLOT_END) {
			if (!this.moveItemStackTo(item, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) return ItemStack.EMPTY;
		} else if (fromIndex >= USE_ROW_SLOT_START && fromIndex < USE_ROW_SLOT_END && !this.moveItemStackTo(item, INV_SLOT_START, INV_SLOT_END, false)) {
			return ItemStack.EMPTY;
		}

		if (item.isEmpty()) slot.set(ItemStack.EMPTY);

		slot.setChanged();
		if (item.getCount() == clicked.getCount()) return ItemStack.EMPTY;

		slot.onTake(player, item);
		this.setupResultSlot(player);
		this.broadcastChanges();

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
