package modist.artoftnt.common.item;

import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.common.entity.PrimedTntFrame;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

//TODO knock back tnt?
public class TntDispenserItem extends ProjectileWeaponItem {
    public TntDispenserItem() {
        super(ItemLoader.getProperty().durability(384));
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack dispenser = pPlayer.getItemInHand(pHand);
        ItemStack itemstack = pPlayer.getProjectile(dispenser);
        Predicate<ItemStack> predicate = this.getAllSupportedProjectiles();
        //reset tnt
        Inventory inventory = pPlayer.getInventory();
        for(int i = inventory.selected; i < inventory.getContainerSize(); i+=Inventory.getSelectionSize()) {
            ItemStack itemStack1 = pPlayer.getInventory().getItem(i);
            if (predicate.test(itemStack1)) {
                itemstack = net.minecraftforge.common.ForgeHooks.getProjectile(pPlayer, dispenser, itemStack1);
            }
        } //from bottom to top
        if (!itemstack.isEmpty() && itemstack.getItem() instanceof TntFrameItem item) {
            TntFrameData data = item.getTntFrameData(itemstack);
            int coolDown = data.getCoolDown();
            if(!pLevel.isClientSide) {
                PrimedTntFrame entity = new PrimedTntFrame(item.getTntFrameDataTag(itemstack),
                        pLevel, pPlayer.getX(), pPlayer.getEyeY() - (double) 0.1F, pPlayer.getZ(), pPlayer, item.tier);
                entity.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.0F, 1.0F);
                pLevel.addFreshEntity(entity);
                dispenser.hurtAndBreak(entity.getWeight(), pPlayer, (p_40858_) -> p_40858_.broadcastBreakEvent(pHand));
            }
            if (!pPlayer.getAbilities().instabuild) {
                itemstack.shrink(1);
                pPlayer.getCooldowns().addCooldown(this, coolDown);
            }
            return InteractionResultHolder.sidedSuccess(dispenser, pLevel.isClientSide());
        }
        return InteractionResultHolder.fail(dispenser);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return i -> i.getItem() instanceof TntFrameItem;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

}
