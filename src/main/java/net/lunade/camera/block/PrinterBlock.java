package net.lunade.camera.block;

import com.mojang.serialization.MapCodec;
import net.lunade.camera.menu.PrinterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrinterBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<PrinterBlock> CODEC = simpleCodec(PrinterBlock::new);
	private static final Component CONTAINER_TITLE = Component.translatable("container.printer");

	public PrinterBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player entity, BlockHitResult hitResult) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;

		entity.openMenu(state.getMenuProvider(level, pos));
		//entity.awardStat(Stats.INTERACT_WITH_LOOM);
		//TODO: Might want to add an award?
		return InteractionResult.CONSUME;
	}

	@Override
	protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider(
			(id, inventory, player) -> new PrinterMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
			CONTAINER_TITLE
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
