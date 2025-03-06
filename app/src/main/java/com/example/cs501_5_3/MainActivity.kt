package com.example.cs501_5_3

import android.Manifest
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.cs501_5_3.ui.theme.CS501_5_3Theme
import java.security.Permission
import kotlin.math.*
import kotlin.text.compareTo

class MainActivity : ComponentActivity() {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CS501_5_3Theme {

                val permission = rememberSaveable { mutableStateOf(false) }
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    permission.value = isGranted
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RecorderSound(
                        modifier = Modifier.padding(innerPadding),
                        permission,
                        launcher
                    )
                }
            }
        }
    }
}

fun ask(launcher: ActivityResultLauncher<String>){
    launcher.launch(Manifest.permission.RECORD_AUDIO)
}


@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun RecorderSound( modifier: Modifier = Modifier,permission: MutableState<Boolean>,launcher: ActivityResultLauncher<String>) {

    ask(launcher)
    val permissionStatus = ContextCompat.checkSelfPermission(
        LocalContext.current,
        Manifest.permission.RECORD_AUDIO
    )
    LaunchedEffect(permissionStatus) {
        permission.value = permissionStatus == PackageManager.PERMISSION_GRANTED
    }
    if (permission.value) {
        Text("Permission granted", modifier)
        Start()

    } else {
        ask(launcher)
    }

}

@Composable
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
fun Start() {
    val mic = makeRecorder()
    val threshold = 85
    val minBufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val buffer = remember { ShortArray(minBufferSize) }
    val db = remember { mutableStateOf(-80.0) }
        mic.startRecording()
            val rms = remember{ mutableStateOf(calculateRms(buffer)) }
            db.value = convertToDb(rms.value)

    val alertMessage = remember{mutableStateOf("")}
        val readResult = remember { mutableStateOf(mic.read(buffer, 0, buffer.size)) }

        if (readResult.value > 0) {
            val rms = calculateRms(buffer)
            val db = convertToDb(rms)
            println("Volume in dB: $db")
        }


        alertMessage.value = if (db.value > threshold) {
            "Noise level too high! (>$threshold dB)"
        } else {
            ""
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            Text("Current Sound Level (dB): ${db.value}")


            val progress = ((db.value + 80) / 200f).coerceIn(0.0, 1.0)
            val color = when {
                db.value < 60 -> Color.Green
                db.value in 60.0..80.0 -> Color.Yellow
                else -> Color.Red
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.toFloat())
                        .background(color)
                )
            }

            if (alertMessage.value.isNotEmpty()) {
                Text(text = alertMessage.value, color = Color.Red)
            }
        }







}

fun calculateRms(buffer: ShortArray): Double {
    var sum = 0.0
    for (sample in buffer) {
        sum += sample.toDouble() * sample.toDouble()
    }
    return sqrt(sum / buffer.size)
}

fun convertToDb(rms: Double): Double {
    val reference = 32768.0
    return 20 * log10(rms / reference)
}
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
fun makeRecorder() : AudioRecord{
    val minBufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    val audioRecord = AudioRecord.Builder()
        .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build()
        )
        .setBufferSizeInBytes(minBufferSize)
        .build()
    return audioRecord
}


