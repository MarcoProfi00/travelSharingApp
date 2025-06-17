package com.example.travelsharingapp.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.travelsharingapp.data.repository.WidgetPreferenceKeys
import com.example.travelsharingapp.data.repository.dataStoreInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.getValue
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.Row
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.example.travelsharingapp.R

class TravelJoinedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val travelsJson by context.dataStoreInstance.data
                .map { prefs -> prefs[WidgetPreferenceKeys.UPCOMING_TRAVELS_JSON] }
                .collectAsState(initial = null)

            if (travelsJson == null) {
                LoadingState()
            } else {
                val travels: List<WidgetTravelInfo> = if (travelsJson!!.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        val type = object : TypeToken<List<WidgetTravelInfo>>() {}.type
                        Gson().fromJson(travelsJson, type) ?: emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
                TravelsWidgetContent(travels = travels)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun LoadingState() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                //.background(ColorProvider(R.color.widget_background))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Loading trips...",
                style = TextStyle(color = ColorProvider(R.color.widget_on_surface))
            )
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun TravelsWidgetContent(travels: List<WidgetTravelInfo>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(R.color.widget_background))
                .padding(16.dp)
        ) {
            TitleBar(
                startIcon = ImageProvider(R.drawable.outline_logo),
                title = "Upcoming Travels",
                textColor = ColorProvider(R.color.widget_on_surface),
                iconColor = ColorProvider(R.color.widget_primary),
                modifier = GlanceModifier.fillMaxWidth()
            )

            if (travels.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No upcoming trips found.",
                        style = TextStyle(color = ColorProvider(R.color.widget_on_surface_variant))
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items (
                        count = travels.size,
                        itemId = { id -> travels[id].hashCode().toLong() },
                        itemContent = { id ->
                            val travel = travels[id]

                            Column {
                                TravelWidgetItem(travel)
                                Spacer(modifier = GlanceModifier.height(8.dp))
                            }
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun TravelWidgetItem(travel: WidgetTravelInfo) {
        val deepLinkUri = "myapp://travelsharingapp.example.com/travelProposalInfo/${travel.proposalId}".toUri()

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ImageProvider(R.drawable.rounded_background))
                .clickable(
                    actionStartActivity(
                        Intent(Intent.ACTION_VIEW, deepLinkUri)
                    )
                )
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_event),
                    contentDescription = "Date",
                    modifier = GlanceModifier.height(16.dp).width(16.dp),
                    colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(R.color.widget_primary))
                )

                Spacer(modifier = GlanceModifier.width(12.dp))

                Column(
                    modifier = GlanceModifier.fillMaxWidth()
                ) {
                    Text(
                        text = travel.name,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorProvider(R.color.widget_on_surface)
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Text(
                        text = travel.countdownText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(R.color.widget_on_surface_variant)
                        )
                    )
                }
            }
        }
    }
}