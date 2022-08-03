package modist.artoftnt.core.explosion.shape;

import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class VanillaExplosionShape extends AbstractExplosionShape {
    public VanillaExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    @Override
    protected void generateToBlowBlocks() {
        for(int j = 0; j < 16; ++j) {
            for(int k = 0; k < 16; ++k) {
                for(int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (float)j / 15.0F * 2.0F - 1.0F;
                        double d1 = (float)k / 15.0F * 2.0F - 1.0F;
                        double d2 = (float)l / 15.0F * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.actualRadius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double d4 = this.centerVec.x;
                        double d6 = this.centerVec.y;
                        double d8 = this.centerVec.z;

                        for(; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            BlockState blockstate = this.level.getBlockState(blockpos);
                            FluidState fluidstate = this.level.getFluidState(blockpos);
                            if (!this.level.isInWorldBounds(blockpos)) {
                                break;
                            }

                            Optional<Float> optional = EXPLOSION_DAMAGE_CALCULATOR.getBlockExplosionResistance(explosion, this.level, blockpos, blockstate, fluidstate);
                            float resistance = optional.map(aFloat -> (aFloat + 0.3F) * 0.3F).orElse(0.09F);
                            if (!this.level.getFluidState(blockpos).isEmpty()) {
                                resistance = interpolate(resistance * 10, 0.09F, fluidFactor / AdditionType.TEMPERATURE.maxValue);
                            }
                            f -= interpolate(resistance, 0.09F);

                            if (f > 0) {
                                this.toBlowBlocks.put(blockpos, f/this.actualRadius);
                            }

                            d4 += d0 * (double)0.3F;
                            d6 += d1 * (double)0.3F;
                            d8 += d2 * (double)0.3F;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void generateToBlowEntities() {
        float f2 = this.actualRadius * 2.0F;
        int k1 = Mth.floor(this.centerVec.x - (double)f2 - 1.0D);
        int l1 = Mth.floor(this.centerVec.x + (double)f2 + 1.0D);
        int i2 = Mth.floor(this.centerVec.y - (double)f2 - 1.0D);
        int i1 = Mth.floor(this.centerVec.y + (double)f2 + 1.0D);
        int j2 = Mth.floor(this.centerVec.z - (double)f2 - 1.0D);
        int j1 = Mth.floor(this.centerVec.z + (double)f2 + 1.0D);
        List<Entity> list = this.level.getEntities(explosion.getSource(), new AABB(k1, i2, j2, l1, i1, j1));
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, explosion, list, f2);
        Vec3 vec3 = new Vec3(this.x, this.y, this.z);

        for (Entity entity : list) {
            if (!entity.ignoreExplosion()) {
                double d12 = Math.sqrt(entity.distanceToSqr(vec3)) / (double) f2;
                if (d12 <= 1.0D) {
                    double d5 = entity.getX() - this.x;
                    double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                    double d9 = entity.getZ() - this.z;
                    double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d13 != 0.0D) {
                        double d14 = getSeenPercent(vec3, entity);
                        double d10 = (1.0D - d12) * d14;
                        this.toBlowEntities.put(entity, (float) d10);
                    }
                }
            }
        }
    }

}
