/*
 * Copyright (c) 2026 TutuTeam
 *
 * This source code is licensed under the MIT License found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.tututeam.create_download_genshin.util

import org.slf4j.LoggerFactory
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * 异步下载工具类（common通用包）
 *
 * 职责：
 *   - 从指定URL异步下载文件到本地
 *   - 全程在后台线程执行，绝对不阻塞游戏主线程
 *   - 实时回调下载进度（已下载字节数 / 总字节数）
 *   - 支持下载取消操作
 *   - 完善的异常捕获：网络超时、IO异常、HTTP错误码
 *
 * 使用Java内置 HttpURLConnection，无需额外网络依赖
 */
object DownloadUtil {

    private val LOGGER = LoggerFactory.getLogger("create-download-genshin/download")

    /** 连接超时：15秒 */
    private const val CONNECT_TIMEOUT_MS = 15_000

    /** 读取超时：30秒 */
    private const val READ_TIMEOUT_MS = 30_000

    /** 缓冲区大小：8KB */
    private const val BUFFER_SIZE = 8192

    /**
     * 异步下载文件
     *
     * 所有回调都在后台线程触发，GUI更新需要通过 Minecraft.getInstance().tell {} 切回主线程
     *
     * @param url          下载链接
     * @param savePath     文件保存路径
     * @param onProgress   进度回调 (已下载字节, 总字节)，总字节可能为 -1（服务器未返回Content-Length）
     * @param onComplete   下载成功回调，参数为 true
     * @param onError      下载失败回调，参数为错误描述信息
     * @return 取消标志，调用 .set(true) 可取消正在进行的下载
     */
    fun downloadAsync(
        url: String,
        savePath: Path,
        onProgress: BiConsumer<Long, Long>,
        onComplete: Consumer<Boolean>,
        onError: Consumer<String>
    ): AtomicBoolean {
        val cancelled = AtomicBoolean(false)

        CompletableFuture.runAsync {
            try {
                doDownload(url, savePath, onProgress, cancelled)
                // 下载完成且未被取消时触发成功回调
                if (!cancelled.get()) {
                    onComplete.accept(true)
                }
            } catch (e: Exception) {
                LOGGER.error("文件下载失败: url={}", url, e)
                if (!cancelled.get()) {
                    onError.accept(e.message ?: "未知网络错误")
                }
            }
        }

        return cancelled
    }

    /**
     * 执行实际的下载逻辑
     *
     * @param url        下载链接
     * @param savePath   保存路径
     * @param onProgress 进度回调
     * @param cancelled  取消标志
     */
    private fun doDownload(
        url: String,
        savePath: Path,
        onProgress: BiConsumer<Long, Long>,
        cancelled: AtomicBoolean
    ) {
        var connection: HttpURLConnection? = null

        try {
            // 确保保存目录存在
            savePath.parent?.let { Files.createDirectories(it) }

            // 建立HTTP连接（使用URI避免URL构造函数弃用警告）
            val urlObj = java.net.URI(url).toURL()
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = "GET"
                setRequestProperty("User-Agent", "CreateDownloadGenshin/1.0")
                // 允许重定向
                instanceFollowRedirects = true
            }

            connection.connect()

            // 检查HTTP响应码
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("服务器返回HTTP错误码: $responseCode ${connection.responseMessage}")
            }

            // 获取文件总大小（服务器可能不返回，此时为 -1）
            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            LOGGER.info("开始下载: url={}, 保存路径={}, 总大小={}", url, savePath, totalBytes)

            // 从输入流读取数据写入文件
            connection.inputStream.use { input ->
                FileOutputStream(savePath.toFile()).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        // 检查是否已取消
                        if (cancelled.get()) {
                            LOGGER.info("下载已被用户取消")
                            // 删除不完整的文件
                            FileUtil.deleteFile(savePath)
                            return
                        }

                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        // 回调进度
                        onProgress.accept(downloadedBytes, totalBytes)
                    }
                }
            }

            LOGGER.info("下载完成: {} ({}字节)", savePath, downloadedBytes)

        } finally {
            connection?.disconnect()
        }
    }
}
