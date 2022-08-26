package com.defeatedcrow.jsonsample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

/**
 * シンプルなItemモデルのJSONファイル生成を行うクラス。 複雑な形状のモデルは別途作成してparent指定して利用する想定。
 */
public class ItemModelMaker {

	public static final ItemModelMaker INSTANCE = new ItemModelMaker();

	/**
	 * ItemのJSONモデル生成と登録をまとめて行うメソッド。
	 */
	public void registerItemModel(Item item, String modid, int maxMetadata) {
		if (item == null)
			return;
		for (int m = 0; m < maxMetadata + 1; m++) {
			String fileName = item.getRegistryName().getResourcePath() + "_" + m;
			// JSONの生成
			this.BuildJsonModel(item, modid, fileName);
			// Model登録
			ModelLoader.setCustomModelResourceLocation(item, m, new ModelResourceLocation(modid + ":" + fileName, "inventory"));
		}
	}

	private static void BuildJsonModel(Item item, String modid, String fileName) {
		// デバッグ環境でなければ実行しない
		if (!JsonSampleCore.isDebug || JsonSampleCore.assetsDir == null)
			return;

		// modelsフォルダに生成する
		File dir = new File(JsonSampleCore.assetsDir, "models/item/");

		// 生成にIJsonModelDataインターフェイスを使うので、実装チェックをする。
		if (dir != null && item != null && item instanceof IJsonModelData) {
			IJsonModelData data = (IJsonModelData) item;
			ItemModel model = INSTANCE.new ItemModel(data.getParent(), data.getTexPath());

			File f = new File(dir + "/" + fileName + ".json");

			// すでにファイルが有る場合は何もしない。
			if (f.exists())
				return;

			// ファイルを生成する。
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}

			try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			// JSONファイルの生成。
			try {
				if (f.canWrite()) {
					// Streamは開けたら閉めるを徹底しよう。ご安全に!
					FileOutputStream fos = new FileOutputStream(f.getPath());
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					JsonWriter jsw = new JsonWriter(osw);

					// ここでインデントのスペースの数を調整でき、ファイルの内容が適宜改行されるようになる。
					jsw.setIndent("  ");
					Gson gson = new Gson();
					gson.toJson(model, model.getClass(), jsw);

					osw.close();
					fos.close();
					jsw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		JsonSampleCore.logger.info("Output JSON model: " + fileName);
	}

	public class ItemModel {
		final String parent;
		final Map<String, String> textures;

		private ItemModel(String p, Map<String, String> tex) {
			parent = p;
			textures = tex;
		}
	}

}
