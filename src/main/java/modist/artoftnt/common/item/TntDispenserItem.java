package modist.artoftnt.common.item;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import modist.artoftnt.ArtofTntConfig;
import modist.artoftnt.core.addition.TntFrameData;
import modist.artoftnt.common.entity.PrimedTntFrame;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class TntDispenserItem extends ProjectileWeaponItem {
    public TntDispenserItem() {
        super(ItemLoader.getProperty().durability(384));
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack dispenser = pPlayer.getItemInHand(pHand);
        int multiShot = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, dispenser);
        int infinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, dispenser);
        int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, dispenser) + 1;
        int count = multiShot > 0 ? 3 : 1;
        boolean success = false;
        for (int i = 0; i < count; i++) {
            ItemStack itemstack = getItemStack(pPlayer, 2 - i, dispenser);
            if (!itemstack.isEmpty() && itemstack.getItem() instanceof TntFrameItem item) {
                TntFrameData data = item.getTntFrameData(itemstack);
                int coolDown = data.getCoolDown();
                if (!pLevel.isClientSide) {
                    PrimedTntFrame entity = new PrimedTntFrame(item.getTntFrameDataTag(itemstack),
                            pLevel, pPlayer.getX(), pPlayer.getEyeY() - (double) 0.1F, pPlayer.getZ(), pPlayer, item.tier);
                    shoot(i, pPlayer, entity, punch, 1F);
                    pLevel.addFreshEntity(entity);
                    dispenser.hurtAndBreak(entity.getWeight(), pPlayer, (p_40858_) -> p_40858_.broadcastBreakEvent(pHand));
                }
                if (!pPlayer.getAbilities().instabuild) {
                    if(infinity <= 0 || data.getWeight() > ArtofTntConfig.MAX_INFINITY_WEIGHT.get()) {
                        itemstack.shrink(1);
                    }
                    pPlayer.getCooldowns().addCooldown(this, coolDown);
                }
                pPlayer.setDeltaMovement(pPlayer.getDeltaMovement().add
                            (pPlayer.getViewVector(1.0F).scale(-coolDown/ 10F))); //recoil
                success = true;
            }
        }
        return success ? InteractionResultHolder.sidedSuccess(dispenser, pLevel.isClientSide()) : InteractionResultHolder.fail(dispenser);
    }

    //from: 2 is bottom...0 is top
    private ItemStack getItemStack(Player pPlayer, int from, ItemStack dispenser) {
        Inventory inventory = pPlayer.getInventory();
        int length = Inventory.getSelectionSize();
        int height = inventory.getContainerSize() / length - 1;
        for (int i = 0; i < height; i++) {
            int slot = inventory.selected + ((i + from) % height + 1) * length;
            ItemStack itemStack1 = pPlayer.getInventory().getItem(slot);
            if (this.getAllSupportedProjectiles().test(itemStack1)) {
                return net.minecraftforge.common.ForgeHooks.getProjectile(pPlayer, dispenser, itemStack1);
            }
        }
        return pPlayer.getProjectile(dispenser);
    }

    public void shoot(int index, Player pShooter, Projectile projectile, float pVelocity, float pInaccuracy) {
        float pProjectileAngle = switch (index) {
            case 1 -> 10.0F;
            case 2 -> -10.0F;
            default -> 0F;
        };
        Vec3 vec31 = pShooter.getUpVector(1.0F);
        Quaternion quaternion = new Quaternion(new Vector3f(vec31), pProjectileAngle, true);
        Vec3 vec3 = pShooter.getViewVector(1.0F);
        Vector3f vector3f = new Vector3f(vec3);
        vector3f.transform(quaternion);
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), pVelocity, pInaccuracy);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return i -> i.getItem() instanceof TntFrameItem;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category.equals(EnchantmentCategory.BREAKABLE) ||
                enchantment.equals(Enchantments.INFINITY_ARROWS) ||
                enchantment.equals(Enchantments.KNOCKBACK) ||
                enchantment.equals(Enchantments.MULTISHOT) ||
                enchantment.equals(Enchantments.PUNCH_ARROWS);
    }

}
