/*
 * Copyright (c) 2026 TutuTeam
 *
 * This source code is licensed under the MIT License found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.tututeam.create_download_genshin.util

import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * 文件存储工具类（common通用包）
 *
 * 职责：
 *   - 管理模组专属数据文件夹（.minecraft/create-download-genshin-data/）
 *   - 提供文件路径拼接、存在性检查、删除等安全操作
 *   - 从URL中智能提取文件名
 */
object FileUtil {

    private val LOGGER = LoggerFactory.getLogger("create-download-genshin/file")

    /**
     * 模组专属数据目录
     * 路径：.minecraft/create-download-genshin-data/
     * 用于存放真实下载模式下载的文件
     */
    val DATA_DIR: Path = FabricLoader.getInstance()
        .gameDir
        .resolve("create-download-genshin-data")

    /**
     * 初始化数据目录
     * 在模组启动时调用，确保目录存在
     */
    fun initDataDir() {
        try {
            Files.createDirectories(DATA_DIR)
            LOGGER.info("数据目录就绪: {}", DATA_DIR)
        } catch (e: IOException) {
            LOGGER.error("创建数据目录失败（权限不足？）: {}", DATA_DIR, e)
        }
    }

    /**
     * 获取下载文件的完整保存路径
     *
     * @param fileName 文件名（含扩展名）
     * @return 数据目录下的文件Path
     */
    fun getDownloadFilePath(fileName: String): Path {
        return DATA_DIR.resolve(fileName)
    }

    /**
     * 从URL中智能提取文件名
     * 处理带查询参数的URL，如：
     *   https://example.com/path/file.exe?v=1.2 → file.exe
     *   https://example.com/path/installer → installer
     *
     * @param url 下载链接
     * @return 提取的文件名，无法提取时返回 "downloaded_file"
     */
    fun extractFileName(url: String): String {
        return try {
            // 去除查询参数和锚点
            val cleanUrl = url.substringBefore("?").substringBefore("#")
            // 取最后一段路径
            val name = cleanUrl.substringAfterLast("/")
            if (name.isNotBlank()) name else "downloaded_file"
        } catch (e: Exception) {
            LOGGER.warn("从URL提取文件名失败: {}", url, e)
            "downloaded_file"
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    fun fileExists(filePath: Path): Boolean {
        return try {
            Files.exists(filePath)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 安全删除文件
     *
     * @param filePath 要删除的文件路径
     * @return 是否删除成功
     */
    fun deleteFile(filePath: Path): Boolean {
        return try {
            if (Files.exists(filePath)) {
                Files.delete(filePath)
                LOGGER.info("文件已删除: {}", filePath)
                true
            } else {
                false
            }
        } catch (e: IOException) {
            LOGGER.error("删除文件失败: {}", filePath, e)
            false
        }
    }

    /**
     * 获取文件大小（字节）
     *
     * @param filePath 文件路径
     * @return 文件大小，文件不存在或异常时返回 -1
     */
    fun getFileSize(filePath: Path): Long {
        return try {
            if (Files.exists(filePath)) Files.size(filePath) else -1L
        } catch (e: IOException) {
            -1L
        }
    }

    /**
     * 格式化字节数为人类可读格式
     * 例如：1536 → "1.5 KB"，2097152 → "2.0 MB"
     *
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 0 -> "未知"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
