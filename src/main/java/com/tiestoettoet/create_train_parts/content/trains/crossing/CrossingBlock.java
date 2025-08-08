package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import com.tiestoettoet.create_train_parts.AllBlockEntityTypes;
import com.tiestoettoet.create_train_parts.AllBlocks;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlockEntity;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class CrossingBlock extends HorizontalKineticBlock implements IBE<CrossingBlockEntity>, IWrenchable, IHaveBigOutline {
    public static final int placementHelperId = PlacementHelpers.register(new GantryShaftBlock.PlacementHelper());
    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    protected static final VoxelShape NORTH_OPEN;
    protected static final VoxelShape NORTH_OPEN_FLIPPED;
    protected static final VoxelShape NORTH_CLOSED;
    protected static final VoxelShape NORTH_CLOSED_FLIPPED;
    protected static final VoxelShape SOUTH_OPEN;
    protected static final VoxelShape SOUTH_OPEN_FLIPPED;
    protected static final VoxelShape SOUTH_CLOSED;
    protected static final VoxelShape SOUTH_CLOSED_FLIPPED;
    protected static final VoxelShape WEST_OPEN;
    protected static final VoxelShape WEST_OPEN_FLIPPED;
    protected static final VoxelShape WEST_CLOSED;
    protected static final VoxelShape WEST_CLOSED_FLIPPED;
    protected static final VoxelShape EAST_OPEN;
    protected static final VoxelShape EAST_OPEN_FLIPPED;
    protected static final VoxelShape EAST_CLOSED;
    protected static final VoxelShape EAST_CLOSED_FLIPPED;

    public CrossingBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FLIPPED, false).setValue(OPEN, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Boolean flipped = state.getValue(FLIPPED);
        Boolean open = state.getValue(OPEN);
        Direction direction = state.getValue(HORIZONTAL_FACING);
        switch (direction) {
            case NORTH -> {
                if (open) {
                    return flipped ? NORTH_OPEN_FLIPPED : NORTH_OPEN;
                } else {
                    return flipped ? NORTH_CLOSED_FLIPPED : NORTH_CLOSED;
                }
            }
            case SOUTH -> {
                if (open) {
                    return flipped ? SOUTH_OPEN_FLIPPED : SOUTH_OPEN;
                } else {
                    return flipped ? SOUTH_CLOSED_FLIPPED : SOUTH_CLOSED;
                }
            }
            case WEST -> {
                if (open) {
                    return flipped ? WEST_OPEN_FLIPPED : WEST_OPEN;
                } else {
                    return flipped ? WEST_CLOSED_FLIPPED : WEST_CLOSED;
                }
            }
            case EAST -> {
                if (open) {
                    return flipped ? EAST_OPEN_FLIPPED : EAST_OPEN;
                } else {
                    return flipped ? EAST_CLOSED_FLIPPED : EAST_CLOSED;
                }
            }

        }
        return Shapes.block();
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return null; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FLIPPED).add(OPEN));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        Direction facing = context.getHorizontalDirection();

        boolean flipped = false;

        return state.setValue(HORIZONTAL_FACING, facing).setValue(FLIPPED, flipped).setValue(OPEN, false);
    }


    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockState rotated;
        if (context.getClickedFace().getAxis() == Direction.Axis.Y) {
            rotated = getRotatedBlockState(state, context.getClickedFace());
        } else {
            if (context.getClickedFace() == state.getValue(HORIZONTAL_FACING)) {
                rotated = state.cycle(FLIPPED);
            } else {
                rotated = state.setValue(HORIZONTAL_FACING, context.getClickedFace());
            }
        }

        if (!rotated.canSurvive(world, context.getClickedPos()))
            return InteractionResult.PASS;

        KineticBlockEntity.switchToBlockState(world, context.getClickedPos(), updateAfterWrenched(rotated, context));

        return InteractionResult.SUCCESS;
    }

    public static boolean isCrossing(BlockState state) {
        return AllBlocks.CROSSING.has(state);
    }

    @Override
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        Direction direction = state.getValue(HORIZONTAL_FACING);
        BlockPos crossingBase = pos;
        boolean dropBlocks = player == null || !player.isCreative();

        for (int offset = 1; offset < 1028; offset++) {
            BlockPos currentPos = pos.relative(direction, offset);
            BlockState block = worldIn.getBlockState(currentPos);

            if (isArmExtender(block) && direction.getAxis() == block.getValue(BlockStateProperties.FACING)
                    .getAxis())
                continue;

            break;
        }

        for (int offset = 1; offset < 1028; offset++) {
            BlockPos currentPos = pos.relative(direction.getOpposite(), offset);
            BlockState block = worldIn.getBlockState(currentPos);

            if (isArmExtender(block) && direction.getAxis() == block.getValue(BlockStateProperties.FACING)
                    .getAxis()) {
                worldIn.destroyBlock(currentPos, dropBlocks);
                continue;
            }

            break;
        }

        return super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        BlockPos currentPos = pos.below();
        for (int i = 0; i < 16; i++) {
            BlockState blockState = world.getBlockState(currentPos);
            if (AllBlocks.CROSSING.has(blockState)) {
                KineticBlockEntity.switchToBlockState(world, currentPos, Block.updateFromNeighbourShapes(blockState, world, currentPos));
            }
            currentPos = currentPos.below();
        }

        currentPos = pos.above();
        for (int i = 0; i < 16; i++) {
            BlockState blockState = world.getBlockState(currentPos);
            if (AllBlocks.CROSSING.has(blockState)) {
                KineticBlockEntity.switchToBlockState(world, currentPos, Block.updateFromNeighbourShapes(blockState, world, currentPos));
            }
            currentPos = currentPos.above();
        }

        Direction facing = state.getValue(HORIZONTAL_FACING);
        boolean flipped = state.getValue(FLIPPED);
        BlockPos armExtenderPos;
        if (flipped)
            armExtenderPos = pos.relative(facing.getCounterClockWise());
        else
            armExtenderPos = pos.relative(facing.getClockWise());

        BlockState armExtenderState = AllBlocks.ARM_EXTENDER.getDefaultState()
                .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING))
                .setValue(ArmExtenderBlock.FLIPPED, state.getValue(FLIPPED));

        if (world.getBlockState(armExtenderPos).canBeReplaced()) {
            world.setBlock(armExtenderPos, armExtenderState, 3);
        }


    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }

//    @Override
//    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
//                                               BlockHitResult hitResult) {
//        state = state.cycle(OPEN);
//        level.setBlock(pos, state, 10);
//        level.gameEvent(player, state.getValue(OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
//        level.sendBlockUpdated(pos, state, state, 3);
//        return InteractionResult.sidedSuccess(level.isClientSide);
//    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    public static class PlacementHelper implements IPlacementHelper {

        public PlacementHelper() {

        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return AllBlocks.CROSSING::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return AllBlocks.CROSSING::has;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
                                         BlockHitResult ray) {

            Direction offsetDirection = ray.getLocation().subtract(Vec3.atCenterOf(pos)).y < 0 ? Direction.DOWN : Direction.UP;

            BlockPos newPos = pos.relative(offsetDirection);
            BlockState newState = world.getBlockState(newPos);

            if (!newState.canBeReplaced()) {
                newPos = pos.relative(offsetDirection.getOpposite());
                newState = world.getBlockState(newPos);
            }

            if (newState.canBeReplaced()) {

                Direction facing = ray.getDirection();
                if(facing.getAxis()== Direction.Axis.Y)
                    return PlacementOffset.fail();

                Vec3 look = player.getLookAngle();
                Vec3 cross = look.cross(new Vec3(facing.step()));
                boolean flipped = cross.y<0;

                return PlacementOffset.success(newPos, x -> x.setValue(FLIPPED,flipped).setValue(HORIZONTAL_FACING,facing));
            }

            return PlacementOffset.fail();
        }
    }

    @Override
    public Class<CrossingBlockEntity> getBlockEntityClass() {
        return CrossingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CrossingBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CROSSING.get();
    }

    static {
        NORTH_OPEN = Stream.of(
                Stream.of(
                        Block.box(4, 0, 11, 12, 1, 12),
                        Block.box(4, 0, 5, 5, 1, 11),
                        Block.box(11, 0, 5, 12, 1, 11),
                        Block.box(4, 0, 4, 12, 1, 5),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(4, 12, 7, 5, 16, 9),
                        Block.box(0, 17, 7, 4, 21, 9),
                        Block.box(0, 12, 7, 4, 16, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(4, 17, 7, 5, 21, 9),
                        Shapes.join(Block.box(6, 0, 11, 10, 16, 13), Block.box(2, 0, 11, 6, 4, 13), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        NORTH_OPEN_FLIPPED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 11, 12, 1, 12),
                        Block.box(4, 0, 5, 5, 1, 11),
                        Block.box(11, 0, 5, 12, 1, 11),
                        Block.box(4, 0, 4, 12, 1, 5),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(4, 12, 7, 5, 16, 9),
                        Block.box(0, 17, 7, 4, 21, 9),
                        Block.box(0, 12, 7, 4, 16, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(4, 17, 7, 5, 21, 9),
                        Shapes.join(Block.box(6, 0, 3, 10, 16, 5), Block.box(2, 0, 3, 6, 4, 5), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        NORTH_CLOSED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 11, 12, 1, 12),
                        Block.box(4, 0, 5, 5, 1, 11),
                        Block.box(11, 0, 5, 12, 1, 11),
                        Block.box(4, 0, 4, 12, 1, 5),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(4, 12, 7, 5, 16, 9),
                        Block.box(0, 17, 7, 4, 21, 9),
                        Block.box(0, 12, 7, 4, 16, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(4, 17, 7, 5, 21, 9),
                        Shapes.join(Block.box(0, 6, 11, 16, 10, 13), Block.box(0, 10, 11, 4, 14, 13), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        NORTH_CLOSED_FLIPPED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 11, 12, 1, 12),
                        Block.box(4, 0, 5, 5, 1, 11),
                        Block.box(11, 0, 5, 12, 1, 11),
                        Block.box(4, 0, 4, 12, 1, 5),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(4, 12, 7, 5, 16, 9),
                        Block.box(0, 17, 7, 4, 21, 9),
                        Block.box(0, 12, 7, 4, 16, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(4, 17, 7, 5, 21, 9),
                        Shapes.join(Block.box(0, 6, 3, 16, 10, 5), Block.box(0, 10, 3, 4, 14, 5), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST_OPEN = Stream.of(
                Stream.of(
                        Block.box(4, 0, 4, 5, 1, 12),
                        Block.box(5, 0, 4, 11, 1, 5),
                        Block.box(5, 0, 11, 11, 1, 12),
                        Block.box(11, 0, 4, 12, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(7, 12, 4, 9, 16, 5),
                        Block.box(7, 17, 0, 9, 21, 4),
                        Block.box(7, 12, 0, 9, 16, 4)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(7, 17, 4, 9, 21, 5),
                        Shapes.join(Block.box(3, 0, 6, 5, 16, 10), Block.box(3, 0, 2, 5, 4, 6), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST_OPEN_FLIPPED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 4, 5, 1, 12),
                        Block.box(5, 0, 4, 11, 1, 5),
                        Block.box(5, 0, 11, 11, 1, 12),
                        Block.box(11, 0, 4, 12, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(7, 12, 4, 9, 16, 5),
                        Block.box(7, 17, 0, 9, 21, 4),
                        Block.box(7, 12, 0, 9, 16, 4)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(7, 17, 4, 9, 21, 5),
                        Shapes.join(Block.box(11, 0, 6, 13, 16, 10), Block.box(11, 0, 2, 13, 4, 6), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST_CLOSED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 4, 5, 1, 12),
                        Block.box(5, 0, 4, 11, 1, 5),
                        Block.box(5, 0, 11, 11, 1, 12),
                        Block.box(11, 0, 4, 12, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(7, 12, 4, 9, 16, 5),
                        Block.box(7, 17, 0, 9, 21, 4),
                        Block.box(7, 12, 0, 9, 16, 4)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(7, 17, 4, 9, 21, 5),
                        Shapes.join(Block.box(3, 6, 0, 5, 10, 16), Block.box(3, 10, 0, 5, 14, 4), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST_CLOSED_FLIPPED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 4, 5, 1, 12),
                        Block.box(5, 0, 4, 11, 1, 5),
                        Block.box(5, 0, 11, 11, 1, 12),
                        Block.box(11, 0, 4, 12, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(7, 12, 4, 9, 16, 5),
                        Block.box(7, 17, 0, 9, 21, 4),
                        Block.box(7, 12, 0, 9, 16, 4)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(7, 17, 4, 9, 21, 5),
                        Shapes.join(Block.box(11, 6, 0, 13, 10, 16), Block.box(11, 10, 0, 13, 14, 4), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH_OPEN = Stream.of(
                Stream.of(
                        Block.box(4, 0, 4, 12, 1, 5),
                        Block.box(11, 0, 5, 12, 1, 11),
                        Block.box(4, 0, 5, 5, 1, 11),
                        Block.box(4, 0, 11, 12, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(11, 12, 7, 12, 16, 9),
                        Block.box(12, 17, 7, 16, 21, 9),
                        Block.box(12, 12, 7, 16, 16, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(11, 17, 7, 12, 21, 9),
                        Shapes.join(Block.box(6, 0, 3, 10, 16, 5), Block.box(10, 0, 3, 14, 4, 5), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH_OPEN_FLIPPED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 4, 12, 1, 5),
                        Block.box(11, 0, 5, 12, 1, 11),
                        Block.box(4, 0, 5, 5, 1, 11),
                        Block.box(4, 0, 11, 12, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(11, 12, 7, 12, 16, 9),
                        Block.box(12, 17, 7, 16, 21, 9),
                        Block.box(12, 12, 7, 16, 16, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(11, 17, 7, 12, 21, 9),
                        Shapes.join(Block.box(6, 0, 11, 10, 16, 13), Block.box(10, 0, 11, 14, 4, 13), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH_CLOSED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 4, 12, 1, 5),
                        Block.box(11, 0, 5, 12, 1, 11),
                        Block.box(4, 0, 5, 5, 1, 11),
                        Block.box(4, 0, 11, 12, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(11, 12, 7, 12, 16, 9),
                        Block.box(12, 17, 7, 16, 21, 9),
                        Block.box(12, 12, 7, 16, 16, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(11, 17, 7, 12, 21, 9),
                        Shapes.join(Block.box(0, 6, 3, 16, 10, 5), Block.box(12, 10, 3, 16, 14, 5), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH_CLOSED_FLIPPED = Stream.of(
                Stream.of(
                        Block.box(4, 0, 4, 12, 1, 5),
                        Block.box(11, 0, 5, 12, 1, 11),
                        Block.box(4, 0, 5, 5, 1, 11),
                        Block.box(4, 0, 11, 12, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(11, 12, 7, 12, 16, 9),
                        Block.box(12, 17, 7, 16, 21, 9),
                        Block.box(12, 12, 7, 16, 16, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(11, 17, 7, 12, 21, 9),
                        Shapes.join(Block.box(0, 6, 11, 16, 10, 13), Block.box(12, 10, 11, 16, 14, 13), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST_OPEN = Stream.of(
                Stream.of(
                        Block.box(11, 0, 4, 12, 1, 12),
                        Block.box(5, 0, 11, 11, 1, 12),
                        Block.box(5, 0, 4, 11, 1, 5),
                        Block.box(4, 0, 4, 5, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(7, 12, 11, 9, 16, 12),
                        Block.box(7, 17, 12, 9, 21, 16),
                        Block.box(7, 12, 12, 9, 16, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(7, 17, 11, 9, 21, 12),
                        Shapes.join(Block.box(11, 0, 6, 13, 16, 10), Block.box(11, 0, 10, 13, 4, 14), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST_OPEN_FLIPPED = Stream.of(
                Stream.of(
                        Block.box(11, 0, 4, 12, 1, 12),
                        Block.box(5, 0, 11, 11, 1, 12),
                        Block.box(5, 0, 4, 11, 1, 5),
                        Block.box(4, 0, 4, 5, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(7, 12, 11, 9, 16, 12),
                        Block.box(7, 17, 12, 9, 21, 16),
                        Block.box(7, 12, 12, 9, 16, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(7, 17, 11, 9, 21, 12),
                        Shapes.join(Block.box(3, 0, 6, 5, 16, 10), Block.box(3, 0, 10, 5, 4, 14), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST_CLOSED = Stream.of(
                Stream.of(
                        Block.box(11, 0, 4, 12, 1, 12),
                        Block.box(5, 0, 11, 11, 1, 12),
                        Block.box(5, 0, 4, 11, 1, 5),
                        Block.box(4, 0, 4, 5, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(7, 12, 11, 9, 16, 12),
                        Block.box(7, 17, 12, 9, 21, 16),
                        Block.box(7, 12, 12, 9, 16, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(7, 17, 11, 9, 21, 12),
                        Shapes.join(Block.box(11, 6, 0, 13, 10, 16), Block.box(11, 10, 12, 13, 14, 16), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST_CLOSED_FLIPPED = Stream.of(
                Stream.of(
                        Block.box(11, 0, 4, 12, 1, 12),
                        Block.box(5, 0, 11, 11, 1, 12),
                        Block.box(5, 0, 4, 11, 1, 5),
                        Block.box(4, 0, 4, 5, 1, 12),
                        Block.box(5, 1, 5, 11, 12, 11),
                        Block.box(5, 12, 5, 11, 23, 11),
                        Block.box(7, 12, 11, 9, 16, 12),
                        Block.box(7, 17, 12, 9, 21, 16),
                        Block.box(7, 12, 12, 9, 16, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(7, 17, 11, 9, 21, 12),
                        Shapes.join(Block.box(3, 6, 0, 5, 10, 16), Block.box(3, 10, 12, 5, 14, 16), BooleanOp.OR)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    }

    public static boolean isArmExtender(BlockState state) {
        return AllBlocks.ARM_EXTENDER.has(state);
    }


}
