package com.defeatedcrow.jsonsample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/** config階層に独自のコンフィグファイルを生成し、読み書きを行うクラス。
 * このファイルはコンフィグのため、モデルやレシピと異なり、プレイヤーの遊ぶ実環境で読み書きを行う。 */
public class SampleConfigMaker {

	/*
	 * このクラスに保持させておくインスタンス。
	 */
	private static Config config = null;

	public static final SampleConfigMaker INSTANCE = new SampleConfigMaker();

	/*
	 * コンフィグ参照用
	 */
	public int getInt() {
		return config == null ? 1 : config.intSample;
	}

	public boolean getBool() {
		return config == null ? false : config.boolSample;
	}

	public float getFloat() {
		return config == null ? 1.5F : config.floatSample;
	}

	public String getString() {
		return config == null ? "Empty" : config.stringSample;
	}

	/*
	 * コンフィグの生成と読み取りを行う処理。 ゲーム起動前にプレイヤーが編集するファイルを損なわないよう、
	 * 1. ファイルがある場合に、JSONファイルをロード -> ロードした場合は上書き防止の為、3の処理を飛ばす。
	 * 2. ロードしたファイルをもとにコンフィグを設定。
	 * 3. ファイルが存在しない場合、初回生成としてデフォルト値の状態でJSONファイルを生成。
	 * という順に処理を進める。
	 */

	public static void fromJson() {
		// コンフィグ階層に生成するため、コアクラスでconfigDirが取れているかチェック。
		if (JsonSampleCore.configDir == null)
			return;

		File f = new File(JsonSampleCore.configDir, "/config.json");

		// JSONファイルの読み取り。
		try {
			if (f.exists() && f.canRead()) {
				// Streamは開けたら閉めるを徹底しよう。ご安全に!
				FileInputStream fis = new FileInputStream(f.getPath());
				InputStreamReader isr = new InputStreamReader(fis);
				JsonReader jsr = new JsonReader(isr);

				Gson gson = new Gson();
				Config get = gson.fromJson(jsr, Config.class);

				isr.close();
				fis.close();
				jsr.close();

				// ロードしたConfigを代入
				if (get != null) {
					INSTANCE.config = get;

					// Configの内容の確認ログ
					JsonSampleCore.logger.info("Load JSON config: int " + INSTANCE.getInt() + " bool " + INSTANCE.getBool() + " float " + INSTANCE
							.getFloat() + " Str " + INSTANCE.getString());
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void toJson() {
		// コンフィグ階層に生成するため、コアクラスでconfigDirが取れているかチェック。
		// この処理はプレイヤーの実環境で行うため、デバッグ確認がない。
		if (JsonSampleCore.configDir == null)
			return;

		// configがnullでない場合 = すでに生成されたJSONファイルが有る場合なので、ファイル生成をスキップする。
		if (INSTANCE.config != null)
			return;

		// JSON生成用のConfigをデフォルト値で生成
		INSTANCE.config = INSTANCE.new Config(1, false, 1.5F, "Empty");
		File f = new File(JsonSampleCore.configDir, "/config.json");

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
				gson.toJson(INSTANCE.config, Config.class, jsw);

				osw.close();
				fos.close();
				jsw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public class Config {
		public final int intSample;
		public final boolean boolSample;
		public final float floatSample;
		public final String stringSample;

		private Config(int i, boolean b, float f, String s) {
			intSample = i;
			boolSample = b;
			floatSample = f;
			stringSample = s;
		}
	}

}
