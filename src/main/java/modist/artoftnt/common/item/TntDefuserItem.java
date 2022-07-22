package modist.artoftnt.common.item;

import net.minecraft.world.item.Item;

public class TntDefuserItem extends Item { //see CommonEventHandler
    public TntDefuserItem() {
        super(ItemLoader.getProperty().durability(384));
    }
}
