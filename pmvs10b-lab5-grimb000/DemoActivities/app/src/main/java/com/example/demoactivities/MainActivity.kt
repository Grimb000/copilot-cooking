package com.example.demoactivities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.demoactivities.ui.theme.DemoActivitiesTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoActivitiesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GestureDemoScreen()
                }
            }
        }
    }
}

@Composable
private fun GestureDemoScreen() {
    var gestureName by remember { mutableStateOf("No gesture yet") }
    var details by remember { mutableStateOf("Tap, long press, drag, double tap, or pinch inside the box.") }
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Compose gestures", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Gesture: $gestureName")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = details)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Drag: (${dragX.roundToInt()}, ${dragY.roundToInt()})  Zoom: %.2f".format(zoom))
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .border(1.dp, Color.Gray)
                .background(Color(0xFFF5F5F5))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            gestureName = "Tap"
                            details = "Tap at (${it.x.roundToInt()}, ${it.y.roundToInt()})"
                        },
                        onDoubleTap = {
                            gestureName = "Double tap"
                            details = "Double tap at (${it.x.roundToInt()}, ${it.y.roundToInt()})"
                        },
                        onLongPress = {
                            gestureName = "Long press"
                            details = "Long press at (${it.x.roundToInt()}, ${it.y.roundToInt()})"
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount.x
                        dragY += dragAmount.y
                        gestureName = "Drag"
                        details = "Moved by (${dragAmount.x.roundToInt()}, ${dragAmount.y.roundToInt()})"
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        zoom *= zoomChange
                        gestureName = "Pinch"
                        details = "Zoom changed to %.2f".format(zoom)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Use gestures here",
                textAlign = TextAlign.Center
            )
        }
    }
}
