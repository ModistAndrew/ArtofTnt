package modist.artoftnt.core.explosion.handler;

import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import modist.artoftnt.core.explosion.event.CustomExplosionBlockEvent;
import modist.artoftnt.core.explosion.event.CustomExplosionEntityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonExplosionEntityEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void royaltyEvent(CustomExplosionEntityEvent event) {
        if (!event.data.shouldAttack(event.explosion.getSourceMob(), event.entity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void damageEvent(CustomExplosionEntityEvent event) {
        CustomExplosion explosion = event.explosion;
        float percentage = event.percentage;
        float damage = event.data.getValue(AdditionType.DAMAGE);
        if (damage > 0) {
            event.entity.hurt(explosion.getDamageSource(),
                    damage * ((int) ((percentage * percentage + percentage) / 2.0D * 7.0D + 1.0D)));
        }
    }

    @SubscribeEvent
    public static void potionEvent(CustomExplosionEntityEvent event) {
        CustomExplosion explosion = event.explosion;
        if (event.entity instanceof LivingEntity le) {
            for (ItemStack stack : event.data.getItems(AdditionType.POTION)) {
                Potion potion = PotionUtils.getPotion(stack);
                if (potion != Potions.EMPTY) {
                    if (le.isAffectedByPotions()) {
                        for (MobEffectInstance mobeffectinstance : potion.getEffects()) {
                            mobeffectinstance = new MobEffectInstance(mobeffectinstance.getEffect(), (int) (mobeffectinstance.getDuration() * event.percentage),
                                    mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible());
                            if (mobeffectinstance.getEffect().isInstantenous()) {
                                mobeffectinstance.getEffect().applyInstantenousEffect(explosion.getSource(), explosion.getSourceMob(),
                                        le, mobeffectinstance.getAmplifier(), 0.5D);
                            } else {
                                le.addEffect(new MobEffectInstance(mobeffectinstance), explosion.getSource());
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void punchEvent(CustomExplosionEntityEvent event) {
        CustomExplosion explosion = event.explosion;
        Vec3 pos = explosion.getPosition().subtract(explosion.getVec()); //fix punch
        float punch = event.data.getValue(AdditionType.PUNCH) -
                event.data.getValue(AdditionType.DRAW);
        float punchH = punch + event.data.getValue(AdditionType.PUNCH_HORIZONTAL);
        float punchV = punch + event.data.getValue(AdditionType.PUNCH_VERTICAL);
        Entity entity = event.entity;
        float percentage = event.percentage;
        if (punchH != 0 || punchV != 0) {
            double d5 = entity.getX() - pos.x;
            double d7 = (entity instanceof PrimedTnt || entity instanceof PrimedTntFrame ? entity.getY() : entity.getEyeY()) - pos.y;
            //tnt can be blown higher
            double d9 = entity.getZ() - pos.z;
            double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9); //distance
            if (d13 != 0.0D) {
                d5 /= d13;
                d7 /= d13;
                d9 /= d13; //normalize
                double d11 = percentage;
                if (entity instanceof LivingEntity) {
                    d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, percentage);
                    //enchantment
                }
                entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11 * punchH, d7 * d11 * punchV, d9 * d11 * punchH));
                if (entity instanceof Player player) {
                    if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                        explosion.getHitPlayers().put(player, new Vec3(d5 * percentage * punchH, d7 * percentage * punchV, d9 * percentage * punchH));
                        //for players, also do on the client side
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void flameEvent(CustomExplosionEntityEvent event) {
        float flame = event.data.getValue(AdditionType.FLAME);
        if (flame > 0) {
            if (!event.entity.fireImmune()) {
                event.entity.setRemainingFireTicks((int) (flame * 200 * event.percentage));
            }
        }
    }

    @SubscribeEvent
    public static void lightningEvent(CustomExplosionEntityEvent event) {
        CustomExplosion explosion = event.explosion;
        float lightning = event.data.getValue(AdditionType.LIGHTNING);
        if (lightning > 0) {
            BlockPos pos = event.entity.blockPosition();
            if (event.entity instanceof LivingEntity && explosion.level.canSeeSky(pos) && explosion.random.nextInt(4) < lightning * event.percentage) {
                CommonExplosionBlockEventHandler.summonLightningBolt(explosion, pos);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void teleportEvent(CustomExplosionEntityEvent event) {
        float teleportDistance = event.data.getValue(AdditionType.TELEPORT) * event.percentage;
        int teleportVertical = (int) teleportDistance;
        if (teleportVertical > 0 && event.entity instanceof LivingEntity entity) {
            Level level = entity.level;
            for (int i = 0; i < 16; ++i) {
                double d3 = entity.getX() + (event.explosion.random.nextDouble() - 0.5D) * teleportDistance;
                double d4 = Mth.clamp(entity.getY() + (double) (event.explosion.random.nextInt(teleportVertical) - teleportVertical / 2),
                        level.getMinBuildHeight(), level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1);
                double d5 = entity.getZ() + (event.explosion.random.nextDouble() - 0.5D) * teleportDistance;
                if (entity.isPassenger()) {
                    entity.stopRiding();
                }
                if (entity.randomTeleport(d3, d4, d5, true)) {
                    break;
                }
            }
        }
    }
}
