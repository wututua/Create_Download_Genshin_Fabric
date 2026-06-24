/*
 * Copyright (c) 2026 TutuTeam
 *
 * This source code is licensed under the MIT License found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.tututeam.create_download_genshin.client.event

import com.tututeam.create_download_genshin.client.gui.FakeDownloadScreen
import com.tututeam.create_download_genshin.client.gui.RealDownloadScreen
import com.tututeam.create_download_genshin.config.ModConfig
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

/**
 * 客户端事件监听器（client客户端专属）
 *
 * 职责：
 *   - 监听玩家进入单人世界 / 加入多人服务器的事件
 *   - 根据配置自动弹出对应模式的下载进度条GUI
 *   - 触发时机：ClientPlayConnectionEvents.JOIN（网络连接建立成功时）
 *
 * 注意：本类只在客户端执行，不会在服务端加载，可安全调用Minecraft客户端API
 */
object ClientEvents {

    private val LOGGER = LoggerFactory.getLogger("create-download-genshin/events")

    /**
     * 注册所有客户端事件监听器
     * 在 CreateDownloadGenshinClient.onInitializeClient() 中调用
     */
    fun register() {
        // 监听玩家加入服务器/进入单人世界事件
        // 此事件在玩家成功连接到服务器后触发
        ClientPlayConnectionEvents.JOIN.register { handler, sender, client ->
            LOGGER.info("玩家已成功连接到世界，准备弹出下载窗口")

            // 延迟到游戏主线程执行，确保世界完全加载后再弹出GUI
            // 避免在世界未就绪时弹窗导致渲染异常
            client.execute {
                try {
                    showDownloadScreen(client)
                } catch (e: Exception) {
                    LOGGER.error("弹出下载窗口时发生异常", e)
                }
            }
        }

        LOGGER.info("客户端事件监听器注册完成")
    }

    /**
     * 根据配置显示对应的下载界面
     *
     * 前置条件：仅当检测到已安装机械动力模组（Create）时才弹出弹窗
     *
     * 读取 mod_config.json 中的 enableRealDownload 字段：
     *   - false（默认）：显示恶搞虚假下载弹窗（FakeDownloadScreen）
     *   - true：显示真实下载进度窗口（RealDownloadScreen）
     *
     * @param client Minecraft客户端实例
     */
    private fun showDownloadScreen(client: Minecraft) {
        // 检测是否安装了机械动力模组（Create）
        // 如果未安装，不做任何操作，直接返回
        if (!isCreateModLoaded()) {
            LOGGER.info("未检测到机械动力模组（Create），跳过弹窗")
            return
        }

        LOGGER.info("已检测到机械动力模组（Create），准备弹出下载窗口")

        val config = ModConfig.getConfig()

        val screen = if (config.enableRealDownload) {
            LOGGER.info("当前模式：真实下载模式 → 弹出RealDownloadScreen")
            RealDownloadScreen()
        } else {
            LOGGER.info("当前模式：恶搞虚假下载模式 → 弹出FakeDownloadScreen")
            FakeDownloadScreen()
        }

        // 使用Minecraft客户端API弹出GUI屏幕
        client.setScreen(screen)
    }

    /**
     * 检测是否安装了机械动力模组（Create）
     *
     * 通过 Fabric Loader 的模组加载器检测 create 模组是否存在于 mods 目录
     * Create mod 的 mod ID 为 "create"
     *
     * @return true 表示已安装 Create mod，false 表示未安装
     */
    private fun isCreateModLoaded(): Boolean {
        return try {
            FabricLoader.getInstance().isModLoaded("create")
        } catch (e: Exception) {
            LOGGER.error("检测Create模组时发生异常", e)
            false
        }
    }
}
