package ru.dradefire.activityserviceconnection

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import ru.dradefire.activityserviceconnection.MainService.Companion.remote_service_connected
import ru.dradefire.activityserviceconnection.MainService.Companion.remote_service_disconnected
import ru.dradefire.activityserviceconnection.ui.theme.ActivityServiceConnectionTheme


class MainActivity : ComponentActivity() {
    private var mService: Messenger? = null
    private var mIsBound = false
    private var mMessenger: Messenger? = null

    private val viewModel by viewModels<MainViewModel>()

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            viewModel.setMainText("Attached")

            kotlin.runCatching {

                val msg1 = Message.obtain(
                    null,
                    MainService.MSG_REGISTER_CLIENT
                )
                msg1.replyTo = mMessenger
                mService?.send(msg1)

                val msg2 = Message.obtain(null, MainService.MSG_SET_VALUE, 0, 0)
                mService?.send(msg2)
            }.onFailure {
                it.printStackTrace()
                // Problem
            }

            Toast.makeText(this@MainActivity, remote_service_connected, Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            viewModel.setMainText("Disconnected")

            Toast.makeText(this@MainActivity, remote_service_disconnected, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMessenger = Messenger(IncomingHandler(viewModel));

        setContent {
            val mainText by viewModel.mainText.collectAsState()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }

            ActivityServiceConnectionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Text(
                            textAlign = TextAlign.Center,
                            text = mainText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding),
                        )

                        TextButton(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding),
                            onClick = {
                                doBindService()
                            },
                        ) {
                            Text(
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(innerPadding),
                                text = "Bind",
                            )
                        }

                        TextButton(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding),
                            onClick = {
                                doUnbindService()
                            },
                        ) {
                            Text(
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(innerPadding),
                                text = "Unbind",
                            )
                        }

                        TextButton(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding),
                            onClick = {
                                setValue()
                            },
                        ) {
                            Text(
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(innerPadding),
                                text = "Set value",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setValue() {
        if (mService != null) {
            kotlin.runCatching {
                val msg = Message.obtain(null, MainService.MSG_SET_VALUE)
                msg.replyTo = mMessenger
                mService?.send(msg)
            }.onFailure {
                it.printStackTrace()
                // Problem
            }
        }
    }

    private fun doBindService() {
        bindService(Intent(this, MainService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        mIsBound = true
        viewModel.setMainText("Binding")
    }

    private fun doUnbindService() {
        if (mIsBound) {
            viewModel.setMainText("Unbinding")

            if (mService != null) {
                kotlin.runCatching {
                    val msg = Message.obtain(null, MainService.MSG_UNREGISTER_CLIENT)
                    msg.replyTo = mMessenger
                    mService?.send(msg)
                }.onFailure {
                    it.printStackTrace()
                    // Problem
                }
            }

            unbindService(mConnection)
            mIsBound = false
            viewModel.setMainText("Unbinded")
        }
    }

    class IncomingHandler(private val viewModel: MainViewModel) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MainService.MSG_SET_VALUE -> viewModel.setMainText("Received from service: " + msg.arg1)
                else -> super.handleMessage(msg)
            }
        }
    }
}
