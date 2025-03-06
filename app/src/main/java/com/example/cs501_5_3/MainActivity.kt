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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.cs501_5_3.ui.theme.CS501_5_3Theme
import java.security.Permission

class MainActivity : ComponentActivity() {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CS501_5_3Theme {

                val permission = rememberSaveable { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RecorderSound(
                        modifier = Modifier.padding(innerPadding),
                        permission
                    )
                }
            }
        }
    }
}



//@Composable
//fun GrantPermission(permission : MutableState<Boolean>){
//    val launcher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean ->
//        permission.value = isGranted
//    }
//    launcher.launch(Manifest.permission.RECORD_AUDIO)
//}

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun RecorderSound( modifier: Modifier = Modifier,permission: MutableState<Boolean>) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permission.value = isGranted
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
    if (!permission.value) {
        Toast.makeText(LocalContext.current, "UNABLE TO USE MIC", Toast.LENGTH_SHORT).show()
    }else{
        val mic = makeRecorder()
        Text(
            text = "$mic",
            modifier = modifier
        )
    }
}



@RequiresPermission(Manifest.permission.RECORD_AUDIO)
fun makeRecorder() : AudioRecord{
    val minBufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    val audioRecord = AudioRecord.Builder()
        .setAudioSource(MediaRecorder.AudioSource.MIC) // Use microphone
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