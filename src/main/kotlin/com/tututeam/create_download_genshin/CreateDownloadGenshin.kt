/*
 * Copyright (c) 2026 TutuTeam
 *
 * This source code is licensed under the MIT License found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.tututeam.create_download_genshin

import com.tututeam.create_download_genshin.config.ModConfig
import com.tututeam.create_download_genshin.util.FileUtil
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory

/**
 * 模组主入口（common通用包）
 *
 * 在Minecraft模组加载阶段初始化：
 *   1. 加载/创建配置文件（.minecraft/config/create-download-genshin/mod_config.json）
 *   2. 初始化数据目录（.minecraft/create-download-genshin-data/）
 *
 * 此入口在客户端和服务端都会执行，因此只放通用逻辑
 * 客户端专属逻辑（事件监听、GUI）在 CreateDownloadGenshinClient 中
 */
object CreateDownloadGenshin : ModInitializer {

	const val MOD_ID: String = "create-download-genshin"

	private val LOGGER = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		LOGGER.info("[原神下载器] 模组初始化开始...")

		// 步骤1：初始化配置文件
		// 首次启动自动生成默认配置，配置损坏时自动恢复恶搞模式
		ModConfig.init()
		LOGGER.info("[原神下载器] 配置加载完成 | 真实下载模式: {}", ModConfig.getConfig().enableRealDownload)

		// 步骤2：初始化数据目录
		// 用于存放真实下载模式下载的文件
		FileUtil.initDataDir()

		LOGGER.info("[原神下载器] 模组初始化完成！")
	}

	/**
	 * 创建模组命名空间下的ResourceLocation
	 *
	 * @param path 资源路径
	 * @return 如 "create-download-genshin:xxx" 的ResourceLocation
	 */
	fun id(path: String): ResourceLocation = ResourceLocation(MOD_ID, path)
}
