package modist.artoftnt.core.explosion.manager;

import modist.artoftnt.core.explosion.CustomExplosion;
import modist.artoftnt.core.explosion.shape.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ExplosionShapes {
    public static final List<Function<CustomExplosion, AbstractExplosionShape>> SHAPES = new ArrayList<>();

    private static void register(Function<CustomExplosion, AbstractExplosionShape> shape){
        SHAPES.add(shape);
    }

    public static AbstractExplosionShape get(int id, CustomExplosion explosion) {
        if(id >= SHAPES.size()){
            return SHAPES.get(0).apply(explosion);
        }
            return SHAPES.get(id).apply(explosion);
    }

    static{
        register(RandomSphereDfsExplosionShape::new); //default

        register(SphereDfsExplosionShape::new);
        register(CubeDfsExplosionShape::new);
        register(OctahedronDfsExplosionShape::new);

        register(SphereSimpleExplosionShape::new);
        register(CubeSimpleExplosionShape::new);
        register(OctahedronSimpleExplosionShape::new);

        register(VanillaExplosionShape::new);
    }
}
