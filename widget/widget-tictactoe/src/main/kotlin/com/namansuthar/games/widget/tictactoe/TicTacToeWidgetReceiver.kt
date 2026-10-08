package com.namansuthar.games.widget.tictactoe

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the Tic-Tac-Toe widget.
 */
class TicTacToeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TicTacToeWidget()
}
