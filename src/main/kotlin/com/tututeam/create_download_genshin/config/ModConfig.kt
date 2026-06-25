/*
 * Copyright (c) 2026 TutuTeam
 *
 * This source code is licensed under the MIT License found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.tututeam.create_download_genshin.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * 模组配置管理类（common通用包）
 *
 * 职责：
 *   - 模组首次启动自动生成默认配置文件
 *   - 配置文件损坏或字段缺失时自动恢复默认恶搞配置
 *   - 提供线程安全的配置读取接口
 *
 * 配置文件路径：.minecraft/config/create-download-genshin/mod_config.json
 * 模式修改需要重启游戏生效
 */
object ModConfig {

    private val LOGGER = LoggerFactory.getLogger("create-download-genshin/config")

    /** Gson实例，带格式化输出便于人类阅读 */
    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

    /** 配置文件完整路径 */
    private val CONFIG_FILE: File = FabricLoader.getInstance()
        .configDir
        .resolve("create-download-genshin")
        .resolve("mod_config.json")
        .toFile()

    /** 当前生效的配置实例（初始化时加载，默认为恶搞模式） */
    @Volatile
    private var currentConfig: ConfigData = ConfigData()

    /**
     * 配置数据类
     *
     * @property enableRealDownload 是否启用真实下载模式
     *   - false（默认）：恶搞虚假下载模式，进度永远停在99%
     *   - true：真实下载模式，异步下载指定URL文件
     * @property downloadUrl 真实下载模式的目标URL
     *   - 仅在 enableRealDownload=true 时生效
     * @property downloadFileName 下载文件保存时的文件名
     *   - 仅在 enableRealDownload=true 时生效
     */
    data class ConfigData(
        val enableRealDownload: Boolean = false,
        val downloadUrl: String = "https://ys-api.mihoyo.com/event/download_porter/link/ys_cn/official/pc_backup320",
        val downloadFileName: String = "yuanshen.exe"
    )

    /**
     * 初始化配置
     * 模组 onInitialize 阶段调用，必须在任何业务逻辑之前完成
     */
    fun init() {
        try {
            if (CONFIG_FILE.exists()) {
                loadConfig()
            } else {
                LOGGER.info("配置文件不存在，将创建默认配置")
                createDefaultConfig()
            }
        } catch (e: Exception) {
            LOGGER.error("配置初始化异常，回退到默认恶搞配置", e)
            currentConfig = ConfigData()
        }
    }

    /**
     * 获取当前配置（线程安全）
     */
    fun getConfig(): ConfigData = currentConfig

    /**
     * 从磁盘加载配置文件
     * 如果JSON解析失败或关键字段缺失，自动恢复默认值
     */
    private fun loadConfig() {
        try {
            val json = CONFIG_FILE.readText(Charsets.UTF_8)
            val loaded = GSON.fromJson(json, ConfigData::class.java)

            if (loaded != null) {
                currentConfig = loaded
                LOGGER.info("配置加载成功 | 真实下载模式: {}", loaded.enableRealDownload)
            } else {
                LOGGER.warn("配置文件内容为空，恢复默认配置")
                currentConfig = ConfigData()
                saveConfig()
            }
        } catch (e: JsonSyntaxException) {
            LOGGER.error("配置文件JSON格式损坏，自动恢复默认恶搞配置", e)
            currentConfig = ConfigData()
            saveConfig()
        } catch (e: IOException) {
            LOGGER.error("读取配置文件IO异常", e)
            currentConfig = ConfigData()
        } catch (e: Exception) {
            LOGGER.error("加载配置时发生未知异常", e)
            currentConfig = ConfigData()
        }
    }

    /**
     * 创建默认配置文件
     * 确保目录存在后写入默认恶搞模式配置
     */
    private fun createDefaultConfig() {
        try {
            // 确保 config/create-download-genshin/ 目录存在
            CONFIG_FILE.parentFile?.mkdirs()
            // 写入默认配置（恶搞模式）
            CONFIG_FILE.writeText(GSON.toJson(ConfigData()), Charsets.UTF_8)
            LOGGER.info("默认配置已创建: {}", CONFIG_FILE.absolutePath)
        } catch (e: IOException) {
            LOGGER.error("创建默认配置文件失败（权限不足？）", e)
        }
    }

    /**
     * 将当前配置保存到磁盘
     * 通常在配置被代码修改后调用
     */
    private fun saveConfig() {
        try {
            CONFIG_FILE.parentFile?.mkdirs()
            CONFIG_FILE.writeText(GSON.toJson(currentConfig), Charsets.UTF_8)
            LOGGER.info("配置已保存")
        } catch (e: IOException) {
            LOGGER.error("保存配置文件失败", e)
        }
    }
}
