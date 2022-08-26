package com.defeatedcrow.jsonsample;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommonProxySample {

	public void registerItem() {
		JsonSampleCore.sampleItem = new SampleItem().setUnlocalizedName("sample_item").setRegistryName(JsonSampleCore.MODID, "sample_item");
		ForgeRegistries.ITEMS.register(JsonSampleCore.sampleItem);
	}

	public void resisterItemModel() {

	}

}
