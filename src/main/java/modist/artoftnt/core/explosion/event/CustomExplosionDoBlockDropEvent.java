package modist.artoftnt.core.explosion.event;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class CustomExplosionDoBlockDropEvent extends CustomExplosionEvent{
    public final ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList; //added to create block drop

    public CustomExplosionDoBlockDropEvent(CustomExplosion explosion, ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList) {
        super(explosion);
        this.objectArrayList = objectArrayList;
    }
}
