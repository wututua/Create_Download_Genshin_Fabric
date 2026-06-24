/*
 * Copyright (c) 2026 TutuTeam
 *
 * This source code is licensed under the MIT License found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.tututeam.create_download_genshin.client.gui

import com.tututeam.create_download_genshin.config.ModConfig
import com.tututeam.create_download_genshin.util.DownloadUtil
import com.tututeam.create_download_genshin.util.FileUtil
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * 真实下载进度GUI窗口（client客户端专属）
 *
 * 真实下载模式下的核心功能：
 *   - 进入世界自动弹出正规下载进度GUI
 *   - 异步请求指定网络URL下载安装包，全程不阻塞游戏主线程
 *   - 实时刷新下载进度、下载速度、预计剩余时间
 *   - 下载成功后自动调用系统程序打开安装包
 *   - 网络超时、文件权限不足、下载失败时弹出游戏内错误提示
 *
 * 注意：本类只能在客户端使用，严禁在服务端加载
 */
class RealDownloadScreen : Screen(Component.literal("\u539f\u795e\u5b89\u88c5\u5305\u4e0b\u8f7d")) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger("create-download-genshin/gui")

        /** 窗口宽度 */
        private const val WINDOW_WIDTH = 360

        /** 窗口高度 */
        private const val WINDOW_HEIGHT = 240
    }

    /**
     * 下载状态枚举
     */
    private enum class DownloadState {
        /** 下载中 */
        DOWNLOADING,

        /** 下载完成 */
        COMPLETED,

        /** 下载失败 */
        ERROR
    }

    /** 当前下载状态 */
    private var state: DownloadState = DownloadState.DOWNLOADING

    /** 已下载字节数（线程安全） */
    private val downloadedBytes = AtomicLong(0)

    /** 文件总字节数（线程安全，-1表示未知） */
    private val totalBytes = AtomicLong(-1)

    /** 下载取消标志 */
    private var cancelFlag: AtomicBoolean? = null

    /** 错误信息（下载失败时显示） */
    private var errorMessage: String = ""

    /** 下载开始时间戳（毫秒） */
    private var startTime: Long = 0L

    /** 上次速度计算时间戳 */
    private var lastSpeedTime: Long = 0L

    /** 上次速度计算时的已下载字节数 */
    private var lastSpeedBytes: Long = 0L

    /** 当前下载速度（字节/秒） */
    private var downloadSpeed: Long = 0L

    /** 按钮引用 */
    private var actionButton: Button? = null

    override fun init() {
        super.init()

        // 添加操作按钮（下载中=取消，完成/失败=关闭）
        val buttonWidth = 120
        val buttonHeight = 25
        val buttonX = (this.width - buttonWidth) / 2
        val windowY = (this.height - WINDOW_HEIGHT) / 2
        val buttonY = windowY + WINDOW_HEIGHT - 35

        actionButton = addRenderableWidget(
            Button.builder(Component.literal("\u53d6\u6d88\u4e0b\u8f7d")) {
                handleButtonClick()
            }.bounds(buttonX, buttonY, buttonWidth, buttonHeight).build()
        )

        // 启动下载
        startDownload()
    }

    /**
     * 启动异步下载
     * 从配置中读取URL，调用DownloadUtil在后台线程执行
     */
    private fun startDownload() {
        val config = ModConfig.getConfig()
        val url = config.downloadUrl

        // 从URL提取文件名
        val fileName = FileUtil.extractFileName(url)
        val savePath = FileUtil.getDownloadFilePath(fileName)

        // 初始化时间戳
        startTime = System.currentTimeMillis()
        lastSpeedTime = startTime
        state = DownloadState.DOWNLOADING

        LOGGER.info("启动下载: url={}, savePath={}", url, savePath)

        // 调用异步下载工具
        cancelFlag = DownloadUtil.downloadAsync(
            url = url,
            savePath = savePath,
            onProgress = { downloaded, total ->
                // 回调在后台线程触发，使用AtomicLong保证线程安全
                downloadedBytes.set(downloaded)
                totalBytes.set(total)
            },
            onComplete = { success ->
                if (success) {
                    state = DownloadState.COMPLETED
                    LOGGER.info("下载完成，准备打开安装包")
                    // 自动打开下载的文件
                    openDownloadedFile(savePath.toFile())
                }
            },
            onError = { error ->
                state = DownloadState.ERROR
                errorMessage = error
                LOGGER.error("下载失败: {}", error)
            }
        )
    }

    /**
     * 处理按钮点击
     * 根据当前状态决定行为：取消下载 或 关闭窗口
     */
    private fun handleButtonClick() {
        when (state) {
            DownloadState.DOWNLOADING -> cancelDownload()
            DownloadState.COMPLETED, DownloadState.ERROR -> onClose()
        }
    }

    /**
     * 取消正在进行的下载
     */
    private fun cancelDownload() {
        cancelFlag?.set(true)
        LOGGER.info("用户取消了下载")
        onClose()
    }

    /**
     * 使用系统默认程序打开下载的文件
     *
     * @param file 下载完成的文件
     */
    private fun openDownloadedFile(file: File) {
        try {
            if (Desktop.isDesktopSupported()) {
                // 优先使用Desktop API（跨平台）
                Desktop.getDesktop().open(file)
            } else {
                // 备用方案：根据操作系统调用系统命令
                val osName = System.getProperty("os.name").lowercase()
                when {
                    osName.contains("win") -> Runtime.getRuntime().exec(arrayOf("cmd", "/c", "start", "", file.absolutePath))
                    osName.contains("mac") -> Runtime.getRuntime().exec(arrayOf("open", file.absolutePath))
                    osName.contains("nux") || osName.contains("nix") -> Runtime.getRuntime().exec(arrayOf("xdg-open", file.absolutePath))
                    else -> LOGGER.warn("无法识别操作系统，跳过自动打开: {}", osName)
                }
            }
        } catch (e: Exception) {
            LOGGER.error("打开下载文件失败", e)
            // 打开失败不影响下载成功的状态，只是提示用户手动打开
            errorMessage = "文件下载成功，但无法自动打开：${e.message}\n请手动前往 ${file.parent} 打开文件"
            state = DownloadState.ERROR
        }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // ① 全屏半透明黑色遮罩
        context.fill(0, 0, this.width, this.height, 0xCC000000.toInt())

        // 计算窗口位置（居中）
        val windowX = (this.width - WINDOW_WIDTH) / 2
        val windowY = (this.height - WINDOW_HEIGHT) / 2

        // ② 窗口背景（深灰色）
        context.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF1E1E1E.toInt())

        // ③ 边框颜色随状态变化
        val borderColor = when (state) {
            DownloadState.DOWNLOADING -> 0xFF4488FF.toInt() // 蓝色（进行中）
            DownloadState.COMPLETED -> 0xFF00CC00.toInt()   // 绿色（成功）
            DownloadState.ERROR -> 0xFFFF0000.toInt()       // 红色（失败）
        }
        // 绘制边框
        context.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + 2, borderColor)
        context.fill(windowX, windowY + WINDOW_HEIGHT - 2, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, borderColor)
        context.fill(windowX, windowY, windowX + 2, windowY + WINDOW_HEIGHT, borderColor)
        context.fill(windowX + WINDOW_WIDTH - 2, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, borderColor)

        // ④ 标题栏
        val titleBarHeight = 24
        context.fill(windowX + 2, windowY + 2, windowX + WINDOW_WIDTH - 2, windowY + titleBarHeight, 0xFF2D2D2D.toInt())

        val centerX = this.width / 2

        // 根据状态渲染不同内容
        when (state) {
            DownloadState.DOWNLOADING -> renderDownloadingState(context, windowX, windowY, centerX)
            DownloadState.COMPLETED -> renderCompletedState(context, windowX, windowY, centerX)
            DownloadState.ERROR -> renderErrorState(context, windowX, windowY, centerX)
        }

        super.render(context, mouseX, mouseY, delta)
    }

    /**
     * 渲染【下载中】状态
     */
    private fun renderDownloadingState(context: GuiGraphics, windowX: Int, windowY: Int, centerX: Int) {
        // 标题
        context.drawCenteredString(this.font, "\u6b63\u5728\u4e0b\u8f7d\u539f\u795e\u5b89\u88c5\u5305...", centerX, windowY + 7, 0xFFFFFFFF.toInt())

        // 检测提示（青色）
        context.drawCenteredString(
            this.font,
            "\u68c0\u6d4b\u5230\u60a8\u5df2\u5b89\u88c5\u673a\u68b0\u52a8\u529b\uff08Create\uff09\u6a21\u7ec4",
            centerX,
            windowY + 30,
            0xFF00FFFF.toInt()
        )

        // 进度条
        val barX = windowX + 20
        val barY = windowY + 55
        val barWidth = WINDOW_WIDTH - 40
        val barHeight = 22

        // 背景
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333.toInt())

        // 计算进度
        val total = totalBytes.get()
        val downloaded = downloadedBytes.get()
        val progress = if (total > 0) downloaded.toDouble() / total else 0.0
        val fillWidth = (barWidth * progress).toInt().coerceAtMost(barWidth)

        // 填充（蓝色渐变）
        if (fillWidth > 0) {
            context.fill(barX, barY, barX + fillWidth, barY + barHeight, 0xFF4488FF.toInt())
        }

        // 边框
        val barBorderColor = 0xFF666666.toInt()
        context.fill(barX - 1, barY - 1, barX + barWidth + 1, barY, barBorderColor)
        context.fill(barX - 1, barY + barHeight, barX + barWidth + 1, barY + barHeight + 1, barBorderColor)
        context.fill(barX - 1, barY, barX, barY + barHeight, barBorderColor)
        context.fill(barX + barWidth, barY, barX + barWidth + 1, barY + barHeight, barBorderColor)

        // 百分比文字
        val percent = (progress * 100).toInt().coerceIn(0, 100)
        context.drawCenteredString(this.font, "${percent}%", centerX, barY + 5, 0xFFFFFFFF.toInt())

        // 文件大小信息
        val sizeText = "${FileUtil.formatBytes(downloaded)} / ${if (total > 0) FileUtil.formatBytes(total) else "\u672a\u77e5"}"
        context.drawCenteredString(this.font, sizeText, centerX, barY + barHeight + 8, 0xFFCCCCCC.toInt())

        // 下载速度（每秒更新）
        updateDownloadSpeed()
        context.drawCenteredString(
            this.font,
            "\u4e0b\u8f7d\u901f\u5ea6\uff1a${FileUtil.formatBytes(downloadSpeed)}/s",
            centerX,
            barY + barHeight + 25,
            0xFFAAAAAA.toInt()
        )

        // 预计剩余时间
        if (downloadSpeed > 0 && total > 0) {
            val remainingBytes = total - downloaded
            val remainingSeconds = remainingBytes / downloadSpeed
            context.drawCenteredString(
                this.font,
                "\u9884\u8ba1\u5269\u4f59\uff1a${formatTime(remainingSeconds)}",
                centerX,
                barY + barHeight + 42,
                0xFFAAAAAA.toInt()
            )
        }
    }

    /**
     * 渲染【下载完成】状态
     */
    private fun renderCompletedState(context: GuiGraphics, windowX: Int, windowY: Int, centerX: Int) {
        context.drawCenteredString(this.font, "\u4e0b\u8f7d\u5b8c\u6210\uff01", centerX, windowY + 7, 0xFF00CC00.toInt())
        context.drawCenteredString(this.font, "\u68c0\u6d4b\u5230\u60a8\u5df2\u5b89\u88c5\u673a\u68b0\u52a8\u529b\uff08Create\uff09\u6a21\u7ec4", centerX, windowY + 30, 0xFF00FFFF.toInt())
        context.drawCenteredString(this.font, "\u2714 \u5b89\u88c5\u5305\u5df2\u81ea\u52a8\u6253\u5f00", centerX, windowY + 55, 0xFFFFFFFF.toInt())
        context.drawCenteredString(this.font, "\u8bf7\u6309\u7167\u5b89\u88c5\u5411\u5bfc\u5b8c\u6210\u5b89\u88c5", centerX, windowY + 75, 0xFFCCCCCC.toInt())

        // 更新按钮文字为"关闭"
        actionButton?.message = Component.literal("\u5173\u95ed")
    }

    /**
     * 渲染【下载失败】状态
     */
    private fun renderErrorState(context: GuiGraphics, windowX: Int, windowY: Int, centerX: Int) {
        context.drawCenteredString(this.font, "\u2716 \u4e0b\u8f7d\u5931\u8d25", centerX, windowY + 7, 0xFFFF0000.toInt())
        context.drawCenteredString(this.font, "\u68c0\u6d4b\u5230\u60a8\u5df2\u5b89\u88c5\u673a\u68b0\u52a8\u529b\uff08Create\uff09\u6a21\u7ec4", centerX, windowY + 28, 0xFF00FFFF.toInt())
        context.drawCenteredString(this.font, "\u9519\u8bef\u4fe1\u606f\uff1a", centerX, windowY + 50, 0xFFFFFFFF.toInt())

        // 错误信息可能很长，截断显示
        val maxDisplayLen = 25
        val displayError = if (errorMessage.length > maxDisplayLen) {
            errorMessage.substring(0, maxDisplayLen) + "..."
        } else {
            errorMessage.ifBlank { "\u672a\u77e5\u9519\u8bef" }
        }
        context.drawCenteredString(this.font, displayError, centerX, windowY + 70, 0xFFFF8888.toInt())
        context.drawCenteredString(this.font, "\u8bf7\u68c0\u67e5\u7f51\u7edc\u8fde\u63a5\u540e\u91cd\u8bd5", centerX, windowY + 90, 0xFFAAAAAA.toInt())

        // 更新按钮文字为"关闭"
        actionButton?.message = Component.literal("\u5173\u95ed")
    }

    /**
     * 更新下载速度计算
     * 每秒计算一次，避免数字跳动过于频繁
     */
    private fun updateDownloadSpeed() {
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastSpeedTime

        if (elapsed >= 1000) {
            val currentBytes = downloadedBytes.get()
            downloadSpeed = if (elapsed > 0) {
                (currentBytes - lastSpeedBytes) * 1000 / elapsed
            } else {
                0L
            }
            lastSpeedBytes = currentBytes
            lastSpeedTime = currentTime
        }
    }

    /**
     * 格式化秒数为人类可读的时间字符串
     *
     * @param seconds 秒数
     * @return 如 "5秒"、"3分20秒"、"1时5分"
     */
    private fun formatTime(seconds: Long): String {
        return when {
            seconds < 0 -> "\u8ba1\u7b97\u4e2d..."
            seconds < 60 -> "${seconds}\u79d2"
            seconds < 3600 -> "${seconds / 60}\u5206${seconds % 60}\u79d2"
            else -> "${seconds / 3600}\u65f6${(seconds % 3600) / 60}\u5206"
        }
    }

    /**
     * 不暂停游戏世界模拟
     */
    override fun isPauseScreen(): Boolean = false

    override fun onClose() {
        // 关闭窗口时取消正在进行的下载
        if (state == DownloadState.DOWNLOADING) {
            cancelFlag?.set(true)
        }
        super.onClose()
    }
}
