package modist.artoftnt.common.item;

import net.minecraft.world.item.Item;

public class TntDefuserItem extends Item { //TODO: normal TNT
    public TntDefuserItem() {
        super(ItemLoader.getProperty().durability(384));
    }
}
