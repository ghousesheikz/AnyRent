package com.shaikhomes.anyrent.ui.utils

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class WhatsappAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (rootInActiveWindow == null) return
        val rootNodeInfo = AccessibilityNodeInfoCompat.wrap(rootInActiveWindow)
        val messageNodeList =
            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp.w4b:id/entry")
        if (messageNodeList.isNullOrEmpty()) return
        val messageField = messageNodeList[0]
        if (messageField == null || messageField.text.isEmpty() || !messageField.text.toString()
                .endsWith("\uD83D\uDE0A")
        ) return
        val sendMessageNodeList =
            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp.w4b:id/send")
        if (sendMessageNodeList.isNullOrEmpty()) return
        val sendMessage = sendMessageNodeList[0]
        if (!sendMessage.isVisibleToUser) return
        sendMessage.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        try {
            Thread.sleep(2000)
            performGlobalAction(GLOBAL_ACTION_BACK)
            Thread.sleep(2000)
        } catch (exp: InterruptedException) {
        }
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }
}