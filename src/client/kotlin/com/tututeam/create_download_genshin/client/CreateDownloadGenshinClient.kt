/*
 * Copyright (c) 2026 TutuTeam
 *
 * This source code is licensed under the MIT License found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.tututeam.create_download_genshin.client

import com.tututeam.create_download_genshin.client.event.ClientEvents
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

/**
 * 模组客户端入口（client客户端专属包）
 *
 * 仅在客户端执行的初始化逻辑：
 *   - 注册客户端事件监听器（玩家进入世界时弹出下载GUI）
 *
 * 此入口不会在服务端执行，因此可以安全调用Minecraft客户端API
 */
object CreateDownloadGenshinClient : ClientModInitializer {

	private val LOGGER = LoggerFactory.getLogger("create-download-genshin/client")

	override fun onInitializeClient() {
		LOGGER.info("[原神下载器] 客户端初始化开始...")

		// 注册客户端事件监听器
		// 监听玩家进入世界事件，自动弹出下载进度条GUI
		ClientEvents.register()

		LOGGER.info("[原神下载器] 客户端初始化完成！")
	}
}
