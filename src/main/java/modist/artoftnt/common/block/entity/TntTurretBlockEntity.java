package modist.artoftnt.common.block.entity;

import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.TntFrameItem;
import modist.artoftnt.core.turret.TurretActivators;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TntTurretBlockEntity extends CoolDownBlockEntity {
    private static final BlockPos[] FIRE_OFFSETS = BlockPos.betweenClosedStream(-2, -2, -2, 2, 2, 2)
            .filter((p) -> Math.abs(p.getX()) == 2 || Math.abs(p.getY()) == 2 || Math.abs(p.getZ()) == 2)
            .map(BlockPos::immutable).toArray(BlockPos[]::new);
    @Nullable
    private ItemStack presentTnt;
    private Vec3 vec = Vec3.ZERO;
    private int[] tiers = new int[16];
    private ItemStackHandler handler = new ItemStackHandler(16) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return accept(stack);
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            TntTurretBlockEntity.this.updateTiers();
            TntTurretBlockEntity.this.setChanged();
        }
    };
    public TntTurretBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(10, BlockLoader.TNT_TURRET_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        for (int i = 0; i < 16; i++) {
            tiers[i] = -1;
        }
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> handler).cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    public boolean accept(ItemStack stack) {
        return stack.getItem() instanceof TntFrameItem item && item.getTntFrameDataTag(stack) != null;
    }

    public ItemStack tryAddTnt(int slot, ItemStack stack) {
        AtomicReference<ItemStack> ret = new AtomicReference<>(stack);
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(i ->
                ret.set(i.insertItem(slot, stack, false)));
        return ret.get();
    }

    public ItemStack tryExtractTnt(int slot, int count) {
        AtomicReference<ItemStack> ret = new AtomicReference<>(ItemStack.EMPTY);
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(i ->
                ret.set(i.extractItem(slot, count, false)));
        return ret.get();
    }

    public ItemStack tryPutTnt(Vec3 location, ItemStack itemstack) {
        return tryAddTnt(15, itemstack); //TODO
    }
    private void updateTiers() {
        AtomicBoolean changed = new AtomicBoolean(false);
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(is -> {
            for (int i = 0; i < 16; i++) {
                ItemStack stack = is.getStackInSlot(i);
                int old = tiers[i];
                if (stack.getItem() instanceof TntFrameItem item) {
                    tiers[i] = item.tier;
                } else {
                    tiers[i] = -1;
                }
                if(old!=tiers[i]) {
                    changed.set(true);
                }
            }
        });
        if(changed.get()) {
            setChangedAndUpdate();
        }
    }

    public Vec3 getVec() {
        updateDirection();
        return vec;
    }

    private void updateDirection() {
        Vec3 ret = Vec3.ZERO;
        for (BlockPos pos : FIRE_OFFSETS) {
            BlockState state = level.getBlockState(this.getBlockPos().offset(pos));
            ret = ret.add(TurretActivators.getDirection(state, pos));
        }
        this.vec = ret.scale(-1D);
    }

    @Override
    protected void doDispense() {
        if (presentTnt.getItem() instanceof TntFrameItem item) {
            Vec3 vec = this.getVec();
            Vec3 direction = vec.normalize();
            if(!direction.equals(Vec3.ZERO)) {
                double strength = vec.length();
                PrimedTntFrame entity = new PrimedTntFrame(item.getTntFrameDataTag(presentTnt),
                        this.level, this.getBlockPos().getX(), this.getBlockPos().getY(),
                        this.getBlockPos().getZ(), null, item.tier);
                entity.shoot((float) direction.x, (float) direction.y, (float) direction.z,
                        (float) strength, 0.0F);
                level.addFreshEntity(entity);
                presentTnt = ItemStack.EMPTY;
                setChangedAndUpdate();
            }
        }
    }

    @Override
    public boolean tryActivate(int pLevel) {
        int from = pLevel;
        for(int i=0; i<16; i++) {
            ItemStack ret = tryExtractTnt((from + i) % 16, 1);
            if (!ret.isEmpty()) {
                this.presentTnt = ret;
                return true;
            }
        }
        return false;
    }
    @Override
    protected void setCoolDown() {
        if (presentTnt != null && presentTnt.getItem() instanceof TntFrameItem item) {
            this.coolDown = item.getTntFrameData(presentTnt).getCoolDown() + RENDER_TICK;
        }
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        return NonNullList.create();
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ListTag list = pTag.getList("tiers", 3);
        for (int j = 0; j < 16; j++) {
            tiers[j] = list.getInt(j);
        }
        if(pTag.contains("presentTnt")){
            presentTnt = ItemStack.of(pTag.getCompound("presentTnt"));
        }
        this.handler.deserializeNBT(pTag.getCompound("items"));
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ListTag list = new ListTag();
        for (int j = 0; j < 16; j++) {
            list.add(IntTag.valueOf(tiers[j]));
        }
        pTag.put("tiers", list);
        if(presentTnt!=null){
            pTag.put("presentTnt", presentTnt.serializeNBT());
        }
        pTag.put("items", handler.serializeNBT());
    }
}
