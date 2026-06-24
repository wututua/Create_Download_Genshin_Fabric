/*
 * Copyright (c) 2026 TutuTeam
 *
 * This source code is licensed under the MIT License found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.tututeam.create_download_genshin.client.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.util.Random

/**
 * 恶搞虚假下载弹窗（client客户端专属）
 *
 * 默认模式下的核心吓唬功能：
 *   - 进入世界瞬间自动弹出仿高危下载弹窗
 *   - 红色警告边框 + 恐怖文案，模拟系统级紧急下载
 *   - 进度随机缓慢递增，小概率出现进度回退模拟网络卡顿
 *   - 进度永远锁定在99%，不会走到100%完成，达到永久吓唬效果
 *   - 用户可点击关闭按钮或按ESC关闭弹窗
 *
 * 注意：本类只能在客户端使用，严禁在服务端加载
 */
class FakeDownloadScreen : Screen(Component.literal("\u26a0 \u7cfb\u7edf\u8b66\u544a \u26a0")) {

    companion object {
        /** 窗口宽度（像素） */
        private const val WINDOW_WIDTH = 320

        /** 窗口高度（像素） */
        private const val WINDOW_HEIGHT = 210

        /** 进度上限（永远到不了100%） */
        private const val MAX_PROGRESS = 0.99

        /** 每次进度回退的概率（5%） */
        private const val REGRESS_CHANCE = 0.05
    }

    /** 随机数生成器 */
    private val random = Random()

    /** 当前下载进度 [0.0, 0.99] */
    private var progress: Double = 0.0

    /** 渲染帧计数器（用于控制进度更新频率） */
    private var tickCount: Int = 0

    /** 当前显示的警告文案索引 */
    private var warningIndex: Int = 0

    /** 警告文案切换计数器（每60帧切换一次 ≈ 3秒） */
    private var warningTick: Int = 0

    /** 关闭按钮引用 */
    private var closeButton: Button? = null

    /**
     * 吓唬用户的恐怖警告文案列表
     * 每隔几秒轮换显示，增强心理压迫感
     */
    private val warningMessages = listOf(
        "\u8b66\u544a\uff1a\u68c0\u6d4b\u5230\u60a8\u7684\u7535\u8111\u672a\u5b89\u88c5\u300a\u539f\u795e\u300b\uff01",
        "\u7cfb\u7edf\u6b63\u5728\u5f3a\u5236\u4e0b\u8f7d\u539f\u795e\u5b89\u88c5\u5305...\u8bf7\u52ff\u5173\u95ed\u7535\u8111",
        "\u8bf7\u52ff\u5173\u95ed\u6b64\u7a97\u53e3\uff0c\u5426\u5219\u53ef\u80fd\u5bfc\u81f4\u7cfb\u7edf\u6587\u4ef6\u635f\u574f\uff01",
        "\u4e0b\u8f7d\u5b8c\u6210\u540e\u5c06\u81ea\u52a8\u5b89\u88c5\u300a\u539f\u795e\u300b\uff0c\u8bf7\u8010\u5fc3\u7b49\u5f85...",
        "\u60a8\u7684\u7535\u8111\u5c06\u88ab\u5f3a\u5236\u7ed1\u5b9a\u7c73\u54c8\u6e38\u8d26\u53f7\uff0c\u65e0\u6cd5\u89e3\u7ed1",
        "\u68c0\u6d4b\u5230\u5927\u91cf\u7cfb\u7edf\u6587\u4ef6\u9700\u8981\u66ff\u6362\uff0c\u8bf7\u52ff\u4e2d\u65ad\u64cd\u4f5c",
        "\u6b63\u5728\u4fee\u6539\u7cfb\u7edf\u6ce8\u518c\u8868...\u8bf7\u52ff\u65ad\u5f00\u7535\u6e90",
        "\u68c0\u6d4b\u5230\u60a8\u7684\u786c\u76d8\u7a7a\u95f4\u4e0d\u8db3\uff0c\u6b63\u5728\u6e05\u7406\u975e\u5fc5\u8981\u6587\u4ef6..."
    )

    override fun init() {
        super.init()

        // 计算窗口位置（居中）
        val windowX = (this.width - WINDOW_WIDTH) / 2
        val windowY = (this.height - WINDOW_HEIGHT) / 2

        // 添加关闭按钮
        val buttonWidth = 120
        val buttonHeight = 20
        val buttonX = (this.width - buttonWidth) / 2
        val buttonY = windowY + WINDOW_HEIGHT - 30

        closeButton = addRenderableWidget(
            Button.builder(Component.literal("\u5173\u95ed\u4e0b\u8f7d")) {
                // 点击关闭按钮 → 关闭弹窗
                this.onClose()
            }.bounds(buttonX, buttonY, buttonWidth, buttonHeight).build()
        )
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // ① 全屏半透明黑色遮罩（制造压迫感）
        context.fill(0, 0, this.width, this.height, 0xCC000000.toInt())

        // 计算窗口左上角坐标
        val windowX = (this.width - WINDOW_WIDTH) / 2
        val windowY = (this.height - WINDOW_HEIGHT) / 2

        // ② 窗口主体背景（深灰色）
        context.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF1A1A1A.toInt())

        // ③ 红色警告边框（四边各2像素宽）
        val borderColor = 0xFFFF0000.toInt()
        // 上边框
        context.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + 2, borderColor)
        // 下边框
        context.fill(windowX, windowY + WINDOW_HEIGHT - 2, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, borderColor)
        // 左边框
        context.fill(windowX, windowY, windowX + 2, windowY + WINDOW_HEIGHT, borderColor)
        // 右边框
        context.fill(windowX + WINDOW_WIDTH - 2, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, borderColor)

        // ④ 标题栏（红色背景 + 白色大字）
        val titleBarHeight = 22
        context.fill(windowX + 2, windowY + 2, windowX + WINDOW_WIDTH - 2, windowY + titleBarHeight, 0xFFCC0000.toInt())
        val centerX = this.width / 2
        context.drawCenteredString(
            this.font,
            "\u26a0 \u7cfb\u7edf\u7d27\u6025\u8b66\u544a \u26a0",
            centerX,
            windowY + 7,
            0xFFFFFFFF.toInt()
        )

        // ⑤ 检测提示（青色，固定显示）
        context.drawCenteredString(
            this.font,
            "\u68c0\u6d4b\u5230\u60a8\u5df2\u5b89\u88c5\u673a\u68b0\u52a8\u529b\uff08Create\uff09\u6a21\u7ec4",
            centerX,
            windowY + titleBarHeight + 8,
            0xFF00FFFF.toInt()
        )

        // ⑥ 警告文案（黄色，每3秒轮换）
        warningTick++
        if (warningTick >= 60) {
            warningTick = 0
            warningIndex = (warningIndex + 1) % warningMessages.size
        }
        context.drawCenteredString(
            this.font,
            warningMessages[warningIndex],
            centerX,
            windowY + titleBarHeight + 24,
            0xFFFFFF00.toInt()
        )

        // ⑥ 进度条
        val barX = windowX + 20
        val barY = windowY + 70
        val barWidth = WINDOW_WIDTH - 40
        val barHeight = 22

        // 进度条背景（深灰色）
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333.toInt())

        // 进度条填充（颜色随进度变化：绿→黄→橙→红）
        val fillWidth = (barWidth * progress).toInt()
        val progressColor = when {
            progress < 0.3 -> 0xFF00CC00.toInt()   // 绿色
            progress < 0.6 -> 0xFFCCCC00.toInt()   // 黄色
            progress < 0.85 -> 0xFFCC8800.toInt()   // 橙色
            else -> 0xFFCC0000.toInt()               // 红色（接近99%时更吓人）
        }
        if (fillWidth > 0) {
            context.fill(barX, barY, barX + fillWidth, barY + barHeight, progressColor)
        }

        // 进度条白色边框
        val barBorderColor = 0xFFAAAAAA.toInt()
        context.fill(barX - 1, barY - 1, barX + barWidth + 1, barY, barBorderColor)
        context.fill(barX - 1, barY + barHeight, barX + barWidth + 1, barY + barHeight + 1, barBorderColor)
        context.fill(barX - 1, barY, barX, barY + barHeight, barBorderColor)
        context.fill(barX + barWidth, barY, barX + barWidth + 1, barY + barHeight, barBorderColor)

        // ⑦ 进度百分比文字（白色大号）
        val percentText = "\u4e0b\u8f7d\u8fdb\u5ea6\uff1a${(progress * 100).toInt()}%"
        context.drawCenteredString(this.font, percentText, centerX, barY + barHeight + 8, 0xFFFFFFFF.toInt())

        // ⑧ 伪造的文件信息
        context.drawCenteredString(
            this.font,
            "\u539f\u795e\u5b89\u88c5\u5305 v5.0.0 - 23.8 GB",
            centerX,
            barY + barHeight + 25,
            0xFFAAAAAA.toInt()
        )

        // ⑨ 伪造的剩余时间（用 (1-progress)*100 分钟制造紧迫感）
        val remainingMinutes = ((1.0 - progress) * 100).toInt().coerceAtLeast(1)
        context.drawCenteredString(
            this.font,
            "\u9884\u8ba1\u5269\u4f59\u65f6\u95f4\uff1a${remainingMinutes}\u5206\u949f",
            centerX,
            barY + barHeight + 42,
            0xFFAAAAAA.toInt()
        )

        // ⑩ 更新虚假进度
        updateFakeProgress()

        // 渲染按钮等子组件
        super.render(context, mouseX, mouseY, delta)
    }

    /**
     * 更新虚假下载进度
     *
     * 规则：
     *   - 每2~5帧更新一次（模拟不稳定网速）
     *   - 正常递增：0.1% ~ 0.5%
     *   - 5%概率回退：0.1% ~ 0.3%（模拟网络卡顿）
     *   - 硬上限锁定在99%，永远到不了100%
     */
    private fun updateFakeProgress() {
        tickCount++

        // 控制更新频率：每2~5帧更新一次
        val updateInterval = 2 + random.nextInt(4)
        if (tickCount % updateInterval != 0) return

        // 计算本次进度变化量
        val delta = if (random.nextDouble() < REGRESS_CHANCE) {
            // 小概率回退（负值）
            -(0.001 + random.nextDouble() * 0.002)
        } else {
            // 正常递增（正值）
            0.001 + random.nextDouble() * 0.004
        }

        // 应用变化量并钳制在 [0.0, 0.99] 范围内
        progress = (progress + delta).coerceIn(0.0, MAX_PROGRESS)
    }

    /**
     * 不暂停游戏世界模拟
     * 弹窗打开时游戏继续运行，增强沉浸感
     */
    override fun isPauseScreen(): Boolean = false

    override fun onClose() {
        super.onClose()
    }
}
