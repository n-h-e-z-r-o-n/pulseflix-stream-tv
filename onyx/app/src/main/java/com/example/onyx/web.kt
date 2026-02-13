package com.example.onyx

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

class web : AppCompatActivity() {
    private lateinit var geckoView: GeckoView
    private lateinit var session: GeckoSession

    private lateinit var cursor: ImageView
    private var xPos = 100f
    private var yPos = 100f
    private val moveStep = 20f

    companion object {
        private var sRuntime: GeckoRuntime? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web)

        // Find the GeckoView in the layout
        geckoView = findViewById(R.id.geckoview)
        cursor = findViewById(R.id.customCursor)

        // Initialize GeckoSession
        session = GeckoSession()

        // Workaround for Bug 1758212: empty ContentDelegate
        // FIX: Remove () if ContentDelegate is an interface in your version
        session.contentDelegate = object : GeckoSession.ContentDelegate {}

        if (sRuntime == null) {
            sRuntime = GeckoRuntime.create(this)

            // Load your extensions
            val extensionsToInstall = listOf(
                "ublock_origin.xpi",
                "dark_reader.xpi",
                "ghostery.xpi"
            )

            extensionsToInstall.forEach { fileName ->
                installExtension(fileName)
            }
        }





        session.open(sRuntime!!)
        geckoView.setSession(session)

        // Load a URL
        session.loadUri("https://vidsrc.to/embed/movie/927085")

    }

    private fun installExtension(assetName: String) {
        sRuntime?.webExtensionController
            ?.install("resource://android/assets/$assetName")
            ?.accept(
                { extension -> Log.d("GeckoView", "Successfully installed: ${extension?.id}") },
                { error -> Log.e("GeckoView", "Error installing $assetName", error) }
            )
    }



    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> yPos -= moveStep
                KeyEvent.KEYCODE_DPAD_DOWN -> yPos += moveStep
                KeyEvent.KEYCODE_DPAD_LEFT -> xPos -= moveStep
                KeyEvent.KEYCODE_DPAD_RIGHT -> xPos += moveStep
                KeyEvent.KEYCODE_DPAD_CENTER -> performClickAtCursor()
            }

            xPos = xPos.coerceIn(0f, geckoView.width - cursor.width.toFloat())
            yPos = yPos.coerceIn(0f, geckoView.height - cursor.height.toFloat())

            cursor.translationX = xPos
            cursor.translationY = yPos

            return true
        }

        return super.dispatchKeyEvent(event)
    }


    private fun performClickAtCursor() {

        // Make sure coordinates stay inside GeckoView
        xPos = xPos.coerceIn(0f, geckoView.width.toFloat())
        yPos = yPos.coerceIn(0f, geckoView.height.toFloat())

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        val pointerProperties = MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_MOUSE
        }

        val pointerCoords = MotionEvent.PointerCoords().apply {
            x = xPos
            y = yPos
            pressure = 1f
            size = 1f
        }

        // ACTION_DOWN (mouse press)
        val downEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            1,
            arrayOf(pointerProperties),
            arrayOf(pointerCoords),
            0,
            0,
            1f,
            1f,
            0,
            0,
            InputDevice.SOURCE_MOUSE,
            0
        )

        // ACTION_UP (mouse release)
        val upEvent = MotionEvent.obtain(
            downTime,
            eventTime + 50, // small delay for realism
            MotionEvent.ACTION_UP,
            1,
            arrayOf(pointerProperties),
            arrayOf(pointerCoords),
            0,
            0,
            1f,
            1f,
            0,
            0,
            InputDevice.SOURCE_MOUSE,
            0
        )

        geckoView.dispatchTouchEvent(downEvent)
        geckoView.dispatchTouchEvent(upEvent)

        downEvent.recycle()
        upEvent.recycle()
    }

}