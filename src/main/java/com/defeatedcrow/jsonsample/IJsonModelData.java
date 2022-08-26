package com.defeatedcrow.jsonsample;

import java.util.Map;

/**
 * 他のモデルを流用し、parentとtextures指定のみを含むシンプルなモデルファイルの自動生成用インターフェイス。
 */
public interface IJsonModelData {

	String getParent();

	Map<String, String> getTexPath();

}
