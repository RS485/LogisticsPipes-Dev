package logisticspipes.items;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLogisticsChips extends LogisticsItem {

	public static final int ITEM_CHIP_BASIC = 0;
	public static final int ITEM_CHIP_BASIC_RAW = 1;
	public static final int ITEM_CHIP_ADVANCED = 2;
	public static final int ITEM_CHIP_ADVANCED_RAW = 3;
	public static final int ITEM_CHIP_FPGA = 4;
	public static final int ITEM_CHIP_FPGA_RAW = 5;

	public static final int MAX_DMG = 6;

	//private IIcon[] _icons;

	public ItemLogisticsChips() {
		setHasSubtypes(true);
		setUnlocalizedName("logisticsChips");
		setRegistryName("logisticsChips");
		if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			registerIcons();
		}
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons() {
		for (int i = 0; i < MAX_DMG; i++) {
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation("logisticspipes:" + getLPUnlocalizedNameFromData(i).replace("item.logisticsChips.", "chips/"), "inventory"));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		String name = getLPUnlocalizedNameFromData(par1ItemStack.getItemDamage());
		if (name != null) return name;
		return super.getUnlocalizedName(par1ItemStack);
	}

	private String getLPUnlocalizedNameFromData(int dmg) {
		switch (dmg) {
			case ITEM_CHIP_BASIC:
				return "item.logisticsChips.basicMicrocontroller";
			case ITEM_CHIP_BASIC_RAW:
				return "item.logisticsChips.basicMicrocontrollerRaw";
			case ITEM_CHIP_ADVANCED:
				return "item.logisticsChips.advancedMicrocontroller";
			case ITEM_CHIP_ADVANCED_RAW:
				return "item.logisticsChips.advancedMicrocontrollerRaw";
			case ITEM_CHIP_FPGA:
				return "item.logisticsChips.fpga";
			case ITEM_CHIP_FPGA_RAW:
				return "item.logisticsChips.fpgaRaw";
		}
		return null;
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.REDSTONE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getSubItems(Item par1, CreativeTabs par2, @SuppressWarnings("rawtypes") List par3) {
		par3.add(new ItemStack(this, 1, ITEM_CHIP_BASIC));
		par3.add(new ItemStack(this, 1, ITEM_CHIP_BASIC_RAW));
		par3.add(new ItemStack(this, 1, ITEM_CHIP_ADVANCED));
		par3.add(new ItemStack(this, 1, ITEM_CHIP_ADVANCED_RAW));
		par3.add(new ItemStack(this, 1, ITEM_CHIP_FPGA));
		par3.add(new ItemStack(this, 1, ITEM_CHIP_FPGA_RAW));
	}
}
