package modist.artoftnt.common.block.entity;

import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.TntFrameItem;
import modist.artoftnt.core.turret.TurretActivators;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

public class TntTurretBlockEntity extends SuckItemBlockEntity {
    private static final BlockPos[] FIRE_OFFSETS = BlockPos.betweenClosedStream(-2, -2, -2, 2, 2, 2).filter((p) -> {
        return Math.abs(p.getX()) == 2 || Math.abs(p.getY()) == 2 || Math.abs(p.getZ()) == 2;
    }).map(BlockPos::immutable).toArray(BlockPos[]::new);
    private TntFrameData data;
    private Vec3 vec = Vec3.ZERO;

    public TntTurretBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(10, BlockLoader.TNT_TURRET_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }

    public Vec3 getVec(){
        updateDirection();
        return vec;
    }

    private void updateDirection(){
        Vec3 ret = Vec3.ZERO;
        for(BlockPos pos : FIRE_OFFSETS){
            BlockState state = level.getBlockState(this.getBlockPos().offset(pos));
                ret = ret.add(TurretActivators.getDirection(state, pos));
        }
        this.vec = ret;
    }

    @Override
    protected void doDispense() {
        if(getTop().getItem() instanceof TntFrameItem item) {
            updateDirection();
            Vec3 direction = vec.normalize();
            double strength = vec.length();
            PrimedTntFrame entity = new PrimedTntFrame(item.getTntFrameDataTag(getTop()),
                    this.level, this.getBlockPos().getX(), this.getBlockPos().getY(),
                    this.getBlockPos().getZ(), null, item.tier);
            entity.shoot( (float)direction.x, (float)direction.y, (float)direction.z,
                    (float)strength, 0.0F); //inaccuracy already in vector
            level.addFreshEntity(entity);
        }
    }

    @Override
    protected boolean suckItemFrom(IItemHandler handler) {
        for(int i=0; i < handler.getSlots(); i++){
            if(handler.extractItem(i, 1, true).getItem() instanceof TntFrameItem){
                this.stacks.add(handler.extractItem(i, 1, false));
                if(getTop().getItem() instanceof TntFrameItem item) {
                    this.data = item.getTntFrameData(getTop());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void setCoolDown() {
        if(data!=null){
            this.coolDown = data.getCoolDown() + RENDER_TICK;
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if(getTop().getItem() instanceof TntFrameItem item) {
            this.data = item.getTntFrameData(getTop());
        }
    }

    private ItemStack getTop(){
        return this.stacks.isEmpty() ? ItemStack.EMPTY : this.stacks.get(0);
    }
}
