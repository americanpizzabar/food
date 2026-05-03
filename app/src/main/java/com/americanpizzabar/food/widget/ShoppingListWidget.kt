package com.americanpizzabar.food.widget

import android.content.Context
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.room.Room
import com.americanpizzabar.food.MainActivity
import com.americanpizzabar.food.data.local.AppDatabase
import kotlinx.coroutines.flow.first

class ShoppingListWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val items = try {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "foodai.db")
                .fallbackToDestructiveMigration().build()
            db.shoppingDao().getUncheckedItems().first()
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "買い物リスト",
                            style = TextStyle(color = GlanceTheme.colors.primary),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Text(
                            text = "${items.size}品",
                            style = TextStyle(color = GlanceTheme.colors.onBackground)
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    if (items.isEmpty()) {
                        Text(
                            text = "買い物リストは空です",
                            style = TextStyle(color = GlanceTheme.colors.onBackground)
                        )
                    } else {
                        LazyColumn {
                            items(items.take(5)) { item ->
                                Text(
                                    text = "• ${item.name} ${item.amount}${item.unit}",
                                    style = TextStyle(color = GlanceTheme.colors.onBackground),
                                    modifier = GlanceModifier.padding(vertical = 2.dp)
                                )
                            }
                            if (items.size > 5) {
                                item {
                                    Text(
                                        text = "他${items.size - 5}品...",
                                        style = TextStyle(color = GlanceTheme.colors.onBackground)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Button(
                        text = "アプリを開く",
                        onClick = actionStartActivity<MainActivity>()
                    )
                }
            }
        }
    }
}

class ShoppingListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = ShoppingListWidget()
}
