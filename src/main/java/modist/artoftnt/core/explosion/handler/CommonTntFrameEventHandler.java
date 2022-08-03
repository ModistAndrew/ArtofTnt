package modist.artoftnt.core.explosion.handler;

import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.TargetMarkerItem;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.event.PrimedTntFrameHitBlockEvent;
import modist.artoftnt.core.explosion.event.PrimedTntFrameTickEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonTntFrameEventHandler {
    @SubscribeEvent
    public static void punchEvent(PrimedTntFrameTickEvent event) {
        PrimedTntFrame tnt = event.tnt;
        float punch = event.data.getValue(AdditionType.TNT_PUNCH) - event.data.getValue(AdditionType.TNT_DRAW);
        if (punch != 0) {
            float range = event.data.getValue(AdditionType.RANGE);
            List<Entity> list = tnt.level.getEntities(tnt,
                    new AABB(tnt.position().add(-range, -range, -range), tnt.position().add(range, range, range)));
            for (Entity e : list) {
                if (!event.data.shouldAttack(tnt.getOwner(), e)) {
                    continue;
                }
                float distancePercentage = (float) (tnt.position().distanceTo(e.position()) / range);
                if (distancePercentage <= 1F) {
                    float p = Math.max(-distancePercentage, punch * (1 - distancePercentage)); //avoid draw too much
                    e.setDeltaMovement(e.getDeltaMovement().add(e.position().subtract(tnt.position()).normalize().
                            multiply(p, p, p)));
                }
            }
        }
    }

    @SubscribeEvent
    public static void gravityEvent(PrimedTntFrameTickEvent event) {
        PrimedTntFrame tnt = event.tnt;
        float gravity = 1 - event.data.getValue(AdditionType.LIGHTNESS);
        if (!tnt.isNoGravity() && (gravity < 0 || !tnt.isOnGround())) {
            tnt.setDeltaMovement(tnt.getDeltaMovement().add(0.0D,
                    -0.04D * gravity,
                    0.0D));
        }
    }

    @SubscribeEvent
    public static void elasticityEventPre(PrimedTntFrameHitBlockEvent.Pre event) {
        PrimedTntFrame tnt = event.tnt;
        float elasticity = event.data.getValue(AdditionType.ELASTICITY);
        float slipperiness = elasticity;
        if (elasticity > AdditionType.ELASTICITY.initialValue) {
            switch (event.result.getDirection().getAxis()) {
                case X ->
                        tnt.setDeltaMovement(tnt.getDeltaMovement().multiply(-elasticity, slipperiness, slipperiness));
                case Y ->
                        tnt.setDeltaMovement(tnt.getDeltaMovement().multiply(slipperiness, -elasticity, slipperiness));
                case Z ->
                        tnt.setDeltaMovement(tnt.getDeltaMovement().multiply(slipperiness, slipperiness, -elasticity));
            }
        }
    }

    @SubscribeEvent
    public static void targetEvent(PrimedTntFrameTickEvent event) {
        PrimedTntFrame tnt = event.tnt;
        event.data.getItems(AdditionType.TARGET).forEach(is -> {
            if (is.getItem() instanceof TargetMarkerItem item) {
                Vec3 target = item.getPos(tnt.level, tnt.position(), is);
                if (target == null && tnt.getOwner() != null) {
                    target = tnt.getOwner().position(); //self
                }
                if (target != null) {
                    Vec3 d = target.subtract(tnt.position());
                    tnt.setDeltaMovement(tnt.getDeltaMovement().add(d.normalize().scale(0.2))); //default, use count to add
                }
            }
        });
    }

    @SubscribeEvent
    public static void monsterTargetEvent(PrimedTntFrameTickEvent event) {
        PrimedTntFrame tnt = event.tnt;
        float monsterFactor = event.data.getValue(AdditionType.MOB_TARGET);
        if (monsterFactor > 0) {
            final float[] minDistance = {Float.MAX_VALUE};
            List<Entity> list = new ArrayList<>();
            if(tnt.getOwner() instanceof LivingEntity le){
                if(le.getLastHurtMob()!=null && le.distanceTo(le.getLastHurtMob()) < monsterFactor * 2){
                    list.add(le.getLastHurtMob()); //first
                }
            }
                list.addAll(tnt.level.getEntitiesOfClass(Mob.class, event.tnt.getBoundingBox().inflate(monsterFactor), e -> {
                    if (!(e instanceof Enemy)) {
                        return false;
                    }
                    float distance = e.distanceTo(tnt);
                    if (distance < minDistance[0]) {
                        minDistance[0] = distance; //nearest
                        return true;
                    }
                    return false;
                }));
            if (!list.isEmpty()) {
                Vec3 target = list.get(0).position();
                Vec3 d = target.subtract(tnt.position());
                tnt.setDeltaMovement(tnt.getDeltaMovement().add(d.normalize().scale(0.02 * monsterFactor)));
            }
        }
    }
}