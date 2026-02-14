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
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

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

            /*
            val extensions = listOf(

                Pair(
                    "resource://android/assets/ublock_origin/",
                    "uBlock0@raymondhill.net"
                ),

                Pair(
                    "resource://android/assets/darkreader/",
                    "addon@darkreader.org"
                ),

                Pair(
                    "resource://android/assets/ghostery/",
                    "firefox@ghostery.com"
                )

            )

            extensions.forEach { (path, id) ->

                sRuntime!!.webExtensionController
                    .ensureBuiltIn(path, id)
                    .accept(

                        { extension ->

                            Log.d("GeckoView-extension", "Extension installed: ${extension?.id}")

                        },

                        { error ->

                            Log.e("GeckoView-extension", "Extension install failed", error)

                        }

                    )
            }

             */


            sRuntime!!.getWebExtensionController()
                .ensureBuiltIn("resource://android/assets/ublock_origin-1/", "uBlock0@raymondhill.net")
                .accept(
                    { extension -> Log.d("GeckoView-extension", "Extension installed: ${extension?.id}")},
                    { error -> Log.e("GeckoView-extension", "Extension install failed", error)}
                )



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

            cursor.animate()
                .translationX(xPos)
                .translationY(yPos)
                .setDuration(40)
                .start()

            sendHoverEvent()

            return true
        }

        return super.dispatchKeyEvent(event)
    }


    private fun performClickAtCursor() {

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        val props = MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_MOUSE
        }

        val coords = MotionEvent.PointerCoords().apply {
            x = xPos
            y = yPos
            pressure = 1f
            size = 1f
        }

        // Mouse button press
        val press = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_BUTTON_PRESS,
            1,
            arrayOf(props),
            arrayOf(coords),
            MotionEvent.BUTTON_PRIMARY,
            MotionEvent.BUTTON_PRIMARY,
            1f,
            1f,
            0,
            0,
            InputDevice.SOURCE_MOUSE,
            0
        )

        // Mouse button release
        val release = MotionEvent.obtain(
            downTime,
            eventTime + 50,
            MotionEvent.ACTION_BUTTON_RELEASE,
            1,
            arrayOf(props),
            arrayOf(coords),
            0,
            MotionEvent.BUTTON_PRIMARY,
            1f,
            1f,
            0,
            0,
            InputDevice.SOURCE_MOUSE,
            0
        )

        geckoView.dispatchGenericMotionEvent(press)
        geckoView.dispatchGenericMotionEvent(release)

        press.recycle()
        release.recycle()

        sendHoverEvent()
    }

    private fun sendHoverEvent() {

        val eventTime = SystemClock.uptimeMillis()

        val pointerProperties = MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_MOUSE
        }

        val pointerCoords = MotionEvent.PointerCoords().apply {
            x = xPos
            y = yPos
            pressure = 0f
            size = 1f
        }

        val hoverEvent = MotionEvent.obtain(
            eventTime,
            eventTime,
            MotionEvent.ACTION_HOVER_MOVE,
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

        geckoView.dispatchGenericMotionEvent(hoverEvent)

        hoverEvent.recycle()
    }

}