package com.defeatedcrow.jsonsample;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = JsonSampleCore.MODID, name = JsonSampleCore.NAME, version = JsonSampleCore.VERSION)
public class JsonSampleCore {
	public static final String MODID = "json_sample";
	public static final String NAME = "JsonSampleDC";
	public static final String VERSION = "1.0";

	public static Logger logger = LogManager.getLogger("json_sample");

	// config階層のfile
	public static File configDir;

	// run階層のfile
	public static File assetsDir;

	// 開発環境で動かす場合のみtrueにする。
	public static boolean isDebug = true;

	// サンプル用アイテム
	public static Item sampleItem;

	@Instance("json_sample")
	public static JsonSampleCore instance;

	@SidedProxy(clientSide = "com.defeatedcrow.jsonsample.ClientProxySample",
			serverSide = "com.defeatedcrow.jsonsample.CommonProxySample")
	public static CommonProxySample proxy;

	@EventHandler
	public void construct(FMLConstructionEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		// FMLPreInitializationEventにはコンフィグ階層が含まれているので、後のためにここで取得
		configDir = new File(event.getModConfigurationDirectory(), "json_sample/");

		// デフォルトのrunフォルダを取得しておく
		// デバッグ環境かどうかのみを確認している雑な実装のため、デバッグフラグを併用して事故を防ぐ
		if (isDebug && FMLLaunchHandler.isDeobfuscatedEnvironment()) {
			File parent = Minecraft.getMinecraft().mcDataDir.getAbsoluteFile();
			if (parent.getAbsolutePath().contains("run")) {
				// parentから"run/."の部分を除去
				try {
					parent = parent.getCanonicalFile();
					assetsDir = new File(parent.getParent() + "/src/main/resources/assets/json_sample/");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// ちゃんとディレクトリを取れているか確認
		if (configDir != null)
			logger.info("configDir: " + configDir.getAbsolutePath());

		if (assetsDir != null)
			logger.info("assetsDir: " + assetsDir.getAbsolutePath());

		// JSONの独自コンフィグの生成と読み取りを行う。
		SampleConfigMaker.INSTANCE.fromJson();
		SampleConfigMaker.INSTANCE.toJson();

	}

	// Itemの登録
	@SubscribeEvent
	protected void registerItems(RegistryEvent.Register<Item> event) {
		proxy.registerItem();
	}

	// Itemのモデル登録
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		proxy.resisterItemModel();
	}

	// レシピの登録
	@EventHandler
	public void init(FMLInitializationEvent event) {
		RecipeJsonMaker.INSTANCE
				.registerShapedRecipe(null, 0, new ItemStack(sampleItem, 1, 0), new String[] { "XXX", "XYX", "XXX" }, new Object[] { 'X', new ItemStack(Items.APPLE), 'Y', new ItemStack(Items.GOLD_INGOT) });
	}
}
