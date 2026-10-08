package com.namansuthar.games.widget.snake

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the Snake widget.
 */
class SnakeWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = SnakeWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Start auto-play for each widget
        appWidgetIds.forEach { widgetId ->
            SnakeWidget.scheduleAutoPlay(context, widgetId.toString(), 1)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        // Cancel auto-play for deleted widgets
        appWidgetIds.forEach { widgetId ->
            SnakeWidget.cancelAutoPlay(context, widgetId.toString())
        }
    }
}
