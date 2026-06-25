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
class FakeDownloadScreen : Screen(Component.translatable("create-download-genshin.gui.fake.title")) {

    companion object {
        /** 窗口宽度（像素） */
        private const val WINDOW_WIDTH = 320

        /** 窗口高度（像素） */
        private const val WINDOW_HEIGHT = 210

        /** 进度上限（永远到不了100%） */
        private const val MAX_PROGRESS = 0.99

        /** 每次进度回退的概率（8%） */
        private const val REGRESS_CHANCE = 0.08
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
     * 吓唬用户的恐怖警告文案列表（使用本地化key）
     * 每隔几秒轮换显示，增强心理压迫感
     */
    private val warningMessageKeys = listOf(
        "create-download-genshin.gui.fake.warning1",
        "create-download-genshin.gui.fake.warning2",
        "create-download-genshin.gui.fake.warning3",
        "create-download-genshin.gui.fake.warning4",
        "create-download-genshin.gui.fake.warning5",
        "create-download-genshin.gui.fake.warning6",
        "create-download-genshin.gui.fake.warning7",
        "create-download-genshin.gui.fake.warning8"
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
            Button.builder(Component.translatable("create-download-genshin.gui.fake.close_btn")) {
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
        context.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + 2, borderColor)
        context.fill(windowX, windowY + WINDOW_HEIGHT - 2, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, borderColor)
        context.fill(windowX, windowY, windowX + 2, windowY + WINDOW_HEIGHT, borderColor)
        context.fill(windowX + WINDOW_WIDTH - 2, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, borderColor)

        // ④ 标题栏（红色背景 + 白色大字）
        val titleBarHeight = 22
        context.fill(windowX + 2, windowY + 2, windowX + WINDOW_WIDTH - 2, windowY + titleBarHeight, 0xFFCC0000.toInt())
        val centerX = this.width / 2
        context.drawCenteredString(
            this.font,
            Component.translatable("create-download-genshin.gui.fake.titlebar"),
            centerX,
            windowY + 7,
            0xFFFFFFFF.toInt()
        )

        // ⑤ 检测提示（青色，固定显示）
        context.drawCenteredString(
            this.font,
            Component.translatable("create-download-genshin.gui.fake.detected"),
            centerX,
            windowY + titleBarHeight + 8,
            0xFF00FFFF.toInt()
        )

        // ⑥ 警告文案（黄色，每5秒轮换）
        warningTick++
        if (warningTick >= 100) {
            warningTick = 0
            warningIndex = (warningIndex + 1) % warningMessageKeys.size
        }
        context.drawCenteredString(
            this.font,
            Component.translatable(warningMessageKeys[warningIndex]),
            centerX,
            windowY + titleBarHeight + 24,
            0xFFFFFF00.toInt()
        )

        // ⑦ 进度条
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

        // ⑧ 进度百分比文字（白色大号）
        val percentText = Component.translatable("create-download-genshin.gui.fake.progress", "${(progress * 100).toInt()}")
        context.drawCenteredString(this.font, percentText, centerX, barY + barHeight + 8, 0xFFFFFFFF.toInt())

        // ⑨ 伪造的文件信息
        context.drawCenteredString(
            this.font,
            Component.translatable("create-download-genshin.gui.fake.fileinfo"),
            centerX,
            barY + barHeight + 25,
            0xFFAAAAAA.toInt()
        )

        // ⑩ 伪造的剩余时间（用 (1-progress)*500 分钟制造紧迫感）
        val remainingMinutes = ((1.0 - progress) * 500).toInt().coerceAtLeast(1)
        context.drawCenteredString(
            this.font,
            Component.translatable("create-download-genshin.gui.fake.remaining", "$remainingMinutes"),
            centerX,
            barY + barHeight + 42,
            0xFFAAAAAA.toInt()
        )

        // ⑪ 更新虚假进度
        updateFakeProgress()

        // 渲染按钮等子组件
        super.render(context, mouseX, mouseY, delta)
    }

    /**
     * 更新虚假下载进度
     *
     * 规则：
     *   - 每8~20帧更新一次（模拟极慢网速，约0.4~1秒更新一次）
     *   - 正常递增：0.03% ~ 0.12%（非常缓慢）
     *   - 8%概率回退：0.05% ~ 0.15%（模拟网络卡顿）
     *   - 硬上限锁定在99%，永远到不了100%
     */
    private fun updateFakeProgress() {
        tickCount++

        // 控制更新频率：每8~20帧更新一次（约0.4~1秒）
        val updateInterval = 8 + random.nextInt(13)
        if (tickCount % updateInterval != 0) return

        // 计算本次进度变化量
        val delta = if (random.nextDouble() < REGRESS_CHANCE) {
            // 小概率回退（负值）：0.05% ~ 0.15%
            -(0.0005 + random.nextDouble() * 0.001)
        } else {
            // 正常递增（正值）：0.03% ~ 0.12%
            0.0003 + random.nextDouble() * 0.0009
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
