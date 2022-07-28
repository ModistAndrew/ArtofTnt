package modist.artoftnt.common.block.entity;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TntTurretBlockEntity extends CoolDownBlockEntity {
    private static final BlockPos[] FIRE_OFFSETS = BlockPos.betweenClosedStream(-2, -2, -2, 2, 2, 2)
            .filter((p) -> Math.abs(p.getX()) == 2 || Math.abs(p.getY()) == 2 || Math.abs(p.getZ()) == 2)
            .map(BlockPos::immutable).toArray(BlockPos[]::new);
    @NotNull
    public ItemStack presentTnt = ItemStack.EMPTY;
    private Vec3 vec = Vec3.ZERO;
    private final int[] renderData = new int[16]; //0 for EMPTY, 1-64 for tier 0...
    private final ItemStackHandler handler = new ItemStackHandler(16) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return accept(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            TntTurretBlockEntity.this.updateTiers();
            TntTurretBlockEntity.this.setChanged();
        }
    };

    public TntTurretBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(5, BlockLoader.TNT_TURRET_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
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

    public boolean contains(int slot) {
        return renderData[slot] > 0;
    }

    public int getCount(int slot) {
        return ((renderData[slot] - 1) % 64) + 1;
    }

    public int getTier(int slot) {
        return (renderData[slot] - 1) / 64;
    }

    public float getOffset(float pPartialTick) {
        return coolDown > minCoolDown ? (maxCoolDown - coolDown - pPartialTick) / (maxCoolDown - minCoolDown) :
                (coolDown+pPartialTick) / minCoolDown;
    }

    public float getTntOffset(float pPartialTick) {
        return coolDown > minCoolDown ? 0 :
                1 - (coolDown+pPartialTick) / minCoolDown;
    }

    public boolean accept(ItemStack stack) {
        return stack.getItem() instanceof TntFrameItem;
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

    public ItemStack tryPutOrGetTnt(Vec3 location, ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            return tryExtractTnt(Vec32Slot(location), 64);
        }
        return tryAddTnt(Vec32Slot(location), itemstack);
    }

    public int Vec32Slot(Vec3 location) {
        int x = (int) (location.x * 4);
        int y = (int) (location.z * 4);
        if (x >= 4) x = 3;
        if (y >= 4) y = 3;
        return x + y * 4;
    }

    private void updateTiers() {
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(is -> {
            for (int i = 0; i < 16; i++) {
                ItemStack stack = is.getStackInSlot(i);
                if (stack.getItem() instanceof TntFrameItem item) {
                    renderData[i] = item.tier * 64 + stack.getCount();
                } else {
                    renderData[i] = 0;
                }
            }
        });
        setChangedAndUpdate();
    }

    public Vec3 getVec() {
        updateDirection();
        return vec;
    }

    private void updateDirection() {
        if(this.level!=null) {
            Vec3 ret = Vec3.ZERO;
            float rotation = 0;
            for (BlockPos pos : FIRE_OFFSETS) {
                BlockState state = level.getBlockState(this.getBlockPos().offset(pos));
                ret = ret.add(TurretActivators.getDirection(state, pos));
                rotation = Math.max(rotation, TurretActivators.getRotation(state));
            }
            long l = System.currentTimeMillis() / 10;
            float s = (l % 360);
            float radian = (float) Math.toRadians(s);
            if (ret.equals(Vec3.ZERO)) {
                this.vec = new Vec3(1, 0, 0).xRot(radian).yRot(2 * radian).zRot(3 * radian);
            } else {
                Vector3f v = new Vector3f(ret);
                v.transform(new Quaternion(new Vector3f(getNormal(ret)), rotation, true));
                v.transform(new Quaternion(new Vector3f(ret), s, true));
                v.mul(-1F);
                this.vec = new Vec3(v);
            }
        }
    }

    public static Vec3 getNormal(Vec3 vec){
        return vec.cross(new Vec3(1, 0, 0)).equals(Vec3.ZERO) ?
                vec.cross(new Vec3(0, 1, 0)) : vec.cross(new Vec3(1, 0, 0));
    }
    @Override
    protected void doDispense() {
        if (this.level!=null && presentTnt.getItem() instanceof TntFrameItem item) {
            Vec3 vec = this.getVec();
            Vec3 direction = vec.normalize();
            double strength = vec.length();
            PrimedTntFrame entity = new PrimedTntFrame(item.getTntFrameDataTag(presentTnt),
                    this.level, this.getBlockPos().getX() + direction.x, this.getBlockPos().getY() + direction.y,
                    this.getBlockPos().getZ() + direction.z, null, item.tier);
            entity.shoot((float) direction.x, (float) direction.y, (float) direction.z,
                    (float) strength, 0.0F);
            level.addFreshEntity(entity);
            presentTnt = ItemStack.EMPTY;
            setChangedAndUpdate();
        }
    }

    @Override
    public boolean tryActivate(int pLevel) {
        ItemStack ret = tryExtractTnt(pLevel, 1);
        if (!ret.isEmpty()) {
            this.presentTnt = ret;
            return true;
        }
        ret = tryExtractTnt(0, 1); //try 0
        if (!ret.isEmpty()) {
            this.presentTnt = ret;
            return true;
        }
        return false;
    }

    @Override
    protected void setCoolDown() {
        if (presentTnt.getItem() instanceof TntFrameItem item) {
            this.coolDown = item.getTntFrameData(presentTnt).getCoolDown() + minCoolDown * 2;
        }
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        List<ItemStack> stacks = new ArrayList<>();
        for(int i=0; i<handler.getSlots(); i++){
            stacks.add(handler.getStackInSlot(i).copy());
        }
        return NonNullList.of(ItemStack.EMPTY, stacks.toArray(new ItemStack[0]));
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ListTag list = pTag.getList("tiers", 3);
        for (int j = 0; j < 16; j++) {
            renderData[j] = list.getInt(j);
        }
        if (pTag.contains("presentTnt")) {
            presentTnt = ItemStack.of(pTag.getCompound("presentTnt"));
        }
        this.handler.deserializeNBT(pTag.getCompound("items"));
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ListTag list = new ListTag();
        for (int j = 0; j < 16; j++) {
            list.add(IntTag.valueOf(renderData[j]));
        }
        pTag.put("tiers", list);
        pTag.put("presentTnt", presentTnt.serializeNBT());
        pTag.put("items", handler.serializeNBT());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (int j = 0; j < 16; j++) {
            list.add(IntTag.valueOf(renderData[j]));
        }
        tag.put("tiers", list);
        tag.put("presentTnt", presentTnt.serializeNBT());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.load(tag);
        ListTag list = tag.getList("tiers", 3);
        for (int j = 0; j < 16; j++) {
            renderData[j] = list.getInt(j);
        }
        if (tag.contains("presentTnt")) {
            presentTnt = ItemStack.of(tag.getCompound("presentTnt"));
        }
    }
}
