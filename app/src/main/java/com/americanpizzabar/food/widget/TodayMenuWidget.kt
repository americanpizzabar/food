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
import java.time.LocalDate

class TodayMenuWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val meals = try {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "foodai.db")
                .fallbackToDestructiveMigration().build()
            db.mealPlanDao().getMealPlansForDate(LocalDate.now().toString()).first()
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
                    Text(
                        text = "今日の献立",
                        style = TextStyle(color = GlanceTheme.colors.primary)
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    if (meals.isEmpty()) {
                        Text(
                            text = "献立が登録されていません",
                            style = TextStyle(color = GlanceTheme.colors.onBackground)
                        )
                    } else {
                        LazyColumn {
                            items(meals) { meal ->
                                val label = when (meal.mealType) {
                                    "BREAKFAST" -> "朝食"
                                    "LUNCH" -> "昼食"
                                    "DINNER" -> "夕食"
                                    else -> "間食"
                                }
                                Text(
                                    text = "$label: ${meal.recipeName}",
                                    style = TextStyle(color = GlanceTheme.colors.onBackground),
                                    modifier = GlanceModifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Button(
                        text = "献立を計画",
                        onClick = actionStartActivity<MainActivity>()
                    )
                }
            }
        }
    }
}

class TodayMenuWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TodayMenuWidget()
}
