package com.defeatedcrow.jsonsample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** クラフトレシピのJSONファイル生成を行うクラス。 ForgeのShapedRecipeやShapelessRecipeをそのまま使うのではなく、自作クラスを作成してJSON変換用にしている。 */
public class RecipeJsonMaker {

	public static final RecipeJsonMaker INSTANCE = new RecipeJsonMaker();

	/** ForgeのShapedOreRecipeのJSONを生成する処理。 result、keysの書き方は1.11以前のForgeでのレシピ登録の書式に準じる。(サンプルを参照のこと。)
	 *
	 * @param domain : recipeファイルを生成時、フォルダ分けして整理したい場合に使用する
	 * @param num : recipeファイル名の末尾に番号をふる。同じアイテムのレシピを複数用意する場合に使用。
	 * @param resurt : 完成品ItemStack
	 * @param pattern : ShapedRecipeのクラフトグリッド上での並べ方を表す。使用する文字はkeysのcharと一致させること。
	 * @param keys : char、アイテム(ItemStack or String)の順に並べる。 */
	public static void registerShapedRecipe(@Nullable String domain, int num, ItemStack result, String[] pattern, Object... keys) {
		/*
		 * 引数のkeysはcharとアイテムが交互に並んでいる。 これをJSONファイルのkeysの書式に合わせる。 並び順を固定できるようLinkedHashMapを使用する。
		 */
		Map<String, Object> keyMap = Maps.newLinkedHashMap();
		for (int i = 0; i < keys.length; i += 2) {
			int i1 = i / 2;
			Character c = (Character) keys[i];
			Object o = keys[i + 1];
			// objectがString(OreDic)、ItemStackの場合で書式が異なるので場合分けする
			if (o instanceof String) {
				InputOre in = new InputOre((String) o);
				keyMap.put(c.toString(), in);
			} else if (o instanceof ItemStack && !((ItemStack) o).isEmpty()) {
				InputItem in = new InputItem((ItemStack) o);
				keyMap.put(c.toString(), in);
			}
		}
		buildShapedRecipe(domain, num, result, pattern, keyMap);
	}

	private static void buildShapedRecipe(@Nullable String domain, int num, ItemStack result, String[] pattern, Map<String, Object> keyMap) {
		// デバッグ環境でなければ実行しない
		if (!JsonSampleCore.isDebug || JsonSampleCore.assetsDir == null)
			return;

		// recipeフォルダに生成する
		File dir = new File(JsonSampleCore.assetsDir, "recipes/");

		if (dir != null && !result.isEmpty()) {
			Map<String, Object> resultMap = convertResult(result);
			Shaped recipe = INSTANCE.new Shaped(pattern, keyMap, resultMap);

			// ファイル名を整える。ここでは完成品ItemStackのResystryNameとメタデータで作っている。
			String filename = result.getItem().getRegistryName().getResourcePath() + "_" + result.getItemDamage();
			if (num > 0) {
				// numで番号をふる場合はファイル名末尾に番号を足す。
				filename = filename + "_" + num;
			}

			File f = new File(dir + "/" + filename + ".json");
			if (domain != null) {
				// domainを使用する場合は、生成ファイルがdomain名のフォルダに入るようにする。
				f = new File(dir + "/" + domain + "/" + filename + ".json");
			}

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
					gson.toJson(recipe, recipe.getClass(), jsw);

					osw.close();
					fos.close();
					jsw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			JsonSampleCore.logger.info("Output JSON recipe: " + filename);
		}
	}

	/** ForgeのShapelessOreRecipeのJSONを生成する処理。 Shapelessは材料(keys)がList型である。
	 *
	 * @param domain : recipeファイルを生成時、フォルダ分けして整理したい場合に使用する
	 * @param num : recipeファイル名の末尾に番号をふる。同じアイテムのレシピを複数用意する場合に使用。
	 * @param resurt : 完成品ItemStack
	 * @param keys : ItemStackまたはString(OreDic)を使用できる。 */
	public static void registerShapelessRecipe(String domain, int num, ItemStack result, Object... keys) {
		// デバッグ環境でなければ実行しない
		if (!JsonSampleCore.isDebug || JsonSampleCore.assetsDir == null)
			return;

		// recipeフォルダに生成する
		File dir = new File(JsonSampleCore.assetsDir, "recipes/");

		if (dir != null && !result.isEmpty()) {

			List<Object> key = convertIngredients(keys);
			Map<String, Object> res = convertResult(result);
			Shapeless recipe = INSTANCE.new Shapeless(key, res);

			// ファイル名を整える。ここでは完成品ItemStackのResystryNameとメタデータで作っている。
			String filename = result.getItem().getRegistryName().getResourcePath() + "_" + result.getItemDamage();
			if (num > 0) {
				// numで番号をふる場合はファイル名末尾に番号を足す。
				filename = filename + "_" + num;
			}

			File f = new File(dir + "/" + filename + ".json");
			if (domain != null) {
				// domainを使用する場合は、生成ファイルがdomain名のフォルダに入るようにする。
				f = new File(dir + "/" + domain + "/" + filename + ".json");
			}

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
					gson.toJson(recipe, recipe.getClass(), jsw);

					osw.close();
					fos.close();
					jsw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			JsonSampleCore.logger.info("Output JSON recipe: " + filename);
		}
	}

	/* 材料がItemStackとOreDicで書式が違うという面倒な仕様への対策 */

	private static class InputOre {
		final String type = "forge:ore_dic";
		final String ore;

		InputOre(String name) {
			ore = name;
		}
	}

	private static class InputItem {
		final String item;
		final int data;

		InputItem(ItemStack itemIn) {
			ResourceLocation rn = itemIn.getItem().getRegistryName();
			String nameIn = rn.getResourceDomain() + ":" + rn.getResourcePath();
			item = nameIn;
			data = itemIn.getItemDamage();
		}
	}

	private static List<Object> convertIngredients(Object... obj) {
		List<Object> ingredients = Lists.newArrayList();
		for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof ItemStack && !((ItemStack) obj[i]).isEmpty()) {
				ItemStack item = (ItemStack) obj[i];
				InputItem in = new InputItem(item);
				ingredients.add(in);
			} else if (obj[i] instanceof String) {
				String name = (String) obj[i];
				InputOre in = new InputOre(name);
				ingredients.add(in);
			}
		}
		return ingredients;
	}

	// resurtはcount(個数)を持つのでInputItemと別の書式
	private static Map<String, Object> convertResult(ItemStack item) {
		Map<String, Object> map = Maps.newLinkedHashMap();
		ResourceLocation rn = item.getItem().getRegistryName();
		String name = rn.getResourceDomain() + ":" + rn.getResourcePath();
		map.put("item", name);
		map.put("count", item.getCount());
		map.put("data", item.getItemDamage());
		return map;
	}

	// このクラスの構造がJsonファイルの内容になる
	private class Shaped {
		final String type = "forge:ore_shaped";
		final String[] pattern;
		final Map<String, Object> key;
		final Map<String, Object> result;

		private Shaped(String[] s, Map<String, Object> k, final Map<String, Object> res) {
			pattern = s;
			key = k;
			result = res;
		}
	}

	// このクラスの構造がJsonファイルの内容になる
	private class Shapeless {
		final String type = "forge:ore_shapeless";
		final List<Object> ingredients;
		final Map<String, Object> result;

		private Shapeless(List<Object> i, final Map<String, Object> res) {
			ingredients = i;
			result = res;
		}
	}

}
