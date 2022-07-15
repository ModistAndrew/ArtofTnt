package modist.artoftnt.core.explosion;

import modist.artoftnt.core.explosion.AbstractExplosionShape;
import modist.artoftnt.core.explosion.CustomExplosion;
import modist.artoftnt.core.explosion.shape.CubeExplosionShape;
import modist.artoftnt.core.explosion.shape.SphereExplosionShape;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Stack;
import java.util.function.Function;

public class ExplosionShapes {
    public static final HashMap<Item, Function<CustomExplosion, AbstractExplosionShape>> SHAPES = new HashMap<>();

    private static void register(Item addition, Function<CustomExplosion, AbstractExplosionShape> shape){
        SHAPES.put(addition, shape);
    }



    public static AbstractExplosionShape get(Stack<ItemStack> items, CustomExplosion explosion) {
        if(items.isEmpty()){
            return getByItem(Items.AIR, explosion);
        }
            return getByItem(items.get(0).getItem(), explosion);
    }

    public static AbstractExplosionShape getByItem(Item item, CustomExplosion explosion) {
        return SHAPES.get(item).apply(explosion);
    }

    static{
        register(Items.AIR, CubeExplosionShape::new);
        register(Items.GOLDEN_APPLE, SphereExplosionShape::new);
    }
}
