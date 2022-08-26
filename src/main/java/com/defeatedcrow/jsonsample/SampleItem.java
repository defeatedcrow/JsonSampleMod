package com.defeatedcrow.jsonsample;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SampleItem extends Item implements IJsonModelData {

	private CreativeTabs tab;

	public SampleItem() {
		super();
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(CreativeTabs.MISC);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
			List<ItemStack> items = Lists.newArrayList();
			items.add(new ItemStack(this));
			subItems.addAll(items);
		}
	}

	// シンプルなアイコンモデルのItem
	@Override
	public String getParent() {
		return "item/generated";
	}

	// IJsonModelDataのインターフェイスを実装
	@Override
	public Map<String, String> getTexPath() {
		Map<String, String> map = Maps.newLinkedHashMap();
		/*
		 * サンプルとしてバニラのリンゴのテクスチャを使用。 Mapのkeyはparentに使用するモデルの指定に合わせる必要がある。 item/generatedの場合は"layer0"。
		 */
		map.put("layer0", "items/apple");
		return map;
	}

}
