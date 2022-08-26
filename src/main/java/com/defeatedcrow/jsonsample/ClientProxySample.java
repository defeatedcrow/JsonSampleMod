package com.defeatedcrow.jsonsample;

public class ClientProxySample extends CommonProxySample {

	@Override
	public void resisterItemModel() {
		ItemModelMaker.INSTANCE.registerItemModel(JsonSampleCore.sampleItem, JsonSampleCore.MODID, 0);
	}

}
