package com.namansuthar.games.widget.widget2048

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the 2048 widget.
 * Handles widget lifecycle events.
 */
class Game2048WidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = Game2048Widget()
}
