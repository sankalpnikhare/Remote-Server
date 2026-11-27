package com.example.remoteserver

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.remoteserver.ui.theme.RemoteServerTheme
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


// ---------------------------------------------------------
// COLORS
// ---------------------------------------------------------

private val CreamBackground = Color(0xFFF8F7F2)
private val CardWhite = Color(0xFFFFFFFF)
private val BorderWarm = Color(0xFFEAE7DF)

private val TextMain = Color(0xFF1C1917)
private val TextMuted = Color(0xFF78716C)

private val Charcoal = Color(0xFF1C1917)

private val Amber = Color(0xFFD97706)
private val AmberLight = Color(0xFFFEF3C7)

private val Emerald = Color(0xFF059669)
private val EmeraldLight = Color(0xFFD1FAE5)

private val Indigo = Color(0xFF4F46E5)
private val IndigoLight = Color(0xFFE0E7FF)

private val Rose = Color(0xFFE11D48)
private val RoseLight = Color(0xFFFFE4E6)


// ---------------------------------------------------------
// MAIN ACTIVITY
// ---------------------------------------------------------

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            RemoteServerTheme {

                RemoteControlApp(
                    activity = this
                )
            }
        }
    }
}


// ---------------------------------------------------------
// MAIN APP
// ---------------------------------------------------------

@Composable
fun RemoteControlApp(
    activity: Activity
) {

    var authenticated by remember {
        mutableStateOf<Boolean?>(null)
    }

    var checkingAuth by remember {
        mutableStateOf(true)
    }

    DisposableEffect(Unit) {

        val executor =
            Executors.newSingleThreadExecutor()

        executor.execute {

            try {

                val result =
                    RemoteApi.checkAuthentication()

                activity.runOnUiThread {

                    authenticated = result
                    checkingAuth = false
                }

            } catch (e: Exception) {

                activity.runOnUiThread {

                    authenticated = false
                    checkingAuth = false
                }
            }
        }

        onDispose {
            executor.shutdownNow()
        }
    }


    if (checkingAuth || authenticated == null) {

        LoadingScreen()

        return
    }


    if (authenticated == false) {

        LoginScreen(
            activity = activity,
            onLoginSuccess = {

                authenticated = true
            }
        )

        return
    }


    DashboardScreen(
        activity = activity
    )
}


// ---------------------------------------------------------
// LOADING SCREEN
// ---------------------------------------------------------

@Composable
fun LoadingScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .windowInsetsPadding(
                WindowInsets.safeDrawing
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(Charcoal),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint = Color.White,
                    modifier =
                        Modifier.size(30.dp)
                )
            }

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Text(
                text = "Remote Control",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            CircularProgressIndicator(
                color = Charcoal,
                strokeWidth = 2.dp
            )
        }
    }
}


// ---------------------------------------------------------
// LOGIN SCREEN
// ---------------------------------------------------------

@Composable
fun LoginScreen(
    activity: Activity,
    onLoginSuccess: () -> Unit
) {

    var key by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf("")
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .windowInsetsPadding(
                WindowInsets.safeDrawing
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(24.dp),

            color =
                CardWhite,

            border =
                BorderStroke(
                    1.dp,
                    BorderWarm
                ),

            shadowElevation = 8.dp
        ) {

            Column(
                modifier =
                    Modifier.padding(26.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(
                            RoundedCornerShape(18.dp)
                        )
                        .background(Charcoal),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Lock,

                        contentDescription =
                            null,

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(28.dp)
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )


                Text(
                    text =
                        "Restricted Access",

                    fontSize =
                        22.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        TextMain
                )


                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )


                Text(
                    text =
                        "Enter your remote key to access the control center.",

                    fontSize =
                        13.sp,

                    color =
                        TextMuted,

                    textAlign =
                        TextAlign.Center
                )


                Spacer(
                    modifier =
                        Modifier.height(22.dp)
                )


                // -------------------------------------------------
                // PASSWORD / REMOTE KEY FIELD
                // -------------------------------------------------

                OutlinedTextField(

                    value =
                        key,

                    onValueChange = {

                        key = it
                        error = ""
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine =
                        true,

                    visualTransformation =
                        PasswordVisualTransformation(),

                    label = {
                        Text("Remote key")
                    }
                )


                if (error.isNotEmpty()) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            error,

                        color =
                            Rose,

                        fontSize =
                            12.sp,

                        textAlign =
                            TextAlign.Center
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )


                Button(

                    onClick = {

                        if (key.isBlank()) {

                            error =
                                "Please enter your remote key."

                            return@Button
                        }


                        loading = true


                        Thread {

                            try {

                                val result =
                                    RemoteApi.login(key)

                                activity.runOnUiThread {

                                    loading = false

                                    if (result.success) {

                                        key = ""

                                        onLoginSuccess()

                                    } else {

                                        error =
                                            result.message
                                    }
                                }

                            } catch (e: Exception) {

                                activity.runOnUiThread {

                                    loading = false

                                    error =
                                        e.message
                                            ?: "Connection failed"
                                }
                            }

                        }.start()
                    },


                    modifier =
                        Modifier.fillMaxWidth(),


                    enabled =
                        !loading,


                    shape =
                        RoundedCornerShape(14.dp),


                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Charcoal
                        )
                ) {

                    if (loading) {

                        CircularProgressIndicator(

                            modifier =
                                Modifier.size(20.dp),

                            color =
                                Color.White,

                            strokeWidth =
                                2.dp
                        )

                    } else {

                        Text(
                            text =
                                "Unlock Control Center",

                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


// ---------------------------------------------------------
// DASHBOARD
// ---------------------------------------------------------

@Composable
fun DashboardScreen(
    activity: Activity
) {

    var online by remember {
        mutableStateOf(false)
    }

    var battery by remember {
        mutableStateOf<Int?>(null)
    }

    var charging by remember {
        mutableStateOf(false)
    }

    var screenshot by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var capturing by remember {
        mutableStateOf(false)
    }

    var confirmAction by remember {
        mutableStateOf<String?>(null)
    }

    var showScreenshot by remember {
        mutableStateOf(false)
    }


    // ---------------------------------------------------------
    // STATUS POLLING
    // ---------------------------------------------------------

    DisposableEffect(Unit) {

        val scheduler =
            Executors.newSingleThreadScheduledExecutor()


        val task = Runnable {

            try {

                val status =
                    RemoteApi.getStatus()

                activity.runOnUiThread {

                    online =
                        status.internet

                    battery =
                        status.battery

                    charging =
                        status.charging
                }

            } catch (e: Exception) {

                activity.runOnUiThread {

                    online = false
                    battery = null
                    charging = false
                }
            }
        }


        // Run status check in background
        scheduler.execute(task)


        // Then every 10 seconds
        scheduler.scheduleAtFixedRate(
            task,
            10,
            10,
            TimeUnit.SECONDS
        )


        onDispose {

            scheduler.shutdownNow()
        }
    }


    // ---------------------------------------------------------
    // CONFIRMATION DIALOG
    // ---------------------------------------------------------

    if (confirmAction != null) {

        val action =
            confirmAction!!

        val stopping =
            action == "stopServer"

        val shuttingDown =
            action == "shutdown"


        AlertDialog(

            onDismissRequest = {

                confirmAction = null
            },


            // -------------------------------------------------
            // ICON
            // -------------------------------------------------

            icon = {

                Icon(

                    imageVector =
                        if (
                            stopping ||
                            shuttingDown
                        ) {

                            Icons.Default.PowerSettingsNew

                        } else {

                            Icons.Default.Warning
                        },


                    contentDescription =
                        null,


                    tint =
                        if (
                            stopping ||
                            shuttingDown
                        ) {

                            Rose

                        } else {

                            Amber
                        }
                )
            },


            // -------------------------------------------------
            // TITLE
            // -------------------------------------------------

            title = {

                Text(

                    text =
                        when {

                            stopping ->
                                "Stop Remote Server?"

                            shuttingDown ->
                                "Shutdown Remote Computer?"

                            else ->
                                "Restart Remote Computer?"
                        }
                )
            },


            // -------------------------------------------------
            // DESCRIPTION
            // -------------------------------------------------

            text = {

                Text(

                    text =
                        when {

                            stopping ->

                                "This will terminate the Node.js server session. You will lose remote connectivity until it is started again."


                            shuttingDown ->

                                "This will shut down the Windows computer immediately. Any unsaved work may be lost."


                            else ->

                                "This will reboot the system session immediately. Any unsaved remote work may be lost."
                        }
                )
            },


            // -------------------------------------------------
            // CANCEL
            // -------------------------------------------------

            dismissButton = {

                TextButton(

                    onClick = {

                        confirmAction =
                            null
                    }
                ) {

                    Text("Cancel")
                }
            },


            // -------------------------------------------------
            // CONFIRM BUTTON
            // -------------------------------------------------

            confirmButton = {

                Button(

                    onClick = {

                        val selectedAction =
                            confirmAction

                        confirmAction =
                            null


                        if (
                            selectedAction != null
                        ) {

                            executeAction(

                                activity,

                                selectedAction
                            )
                        }
                    },


                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                if (
                                    stopping ||
                                    shuttingDown
                                ) {

                                    Rose

                                } else {

                                    Charcoal
                                }
                        )
                ) {

                    Text(

                        text =
                            when {

                                stopping ->
                                    "Stop Server"

                                shuttingDown ->
                                    "Shutdown PC"

                                else ->
                                    "Confirm Reboot"
                            }
                    )
                }
            }
        )
    }


    // ---------------------------------------------------------
    // SCREENSHOT FULLSCREEN VIEWER
    // ---------------------------------------------------------

    if (
        showScreenshot &&
        screenshot != null
    ) {

        Dialog(

            onDismissRequest = {

                showScreenshot = false
            },


            properties =
                DialogProperties(
                    usePlatformDefaultWidth =
                        false
                )
        ) {

            Box(

                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xE6000000)
                    )
                    .clickable {

                        showScreenshot =
                            false
                    }
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    )
            ) {

                Image(

                    bitmap =
                        screenshot!!.asImageBitmap(),

                    contentDescription =
                        "Desktop screenshot",

                    contentScale =
                        ContentScale.Fit,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                )


                TextButton(

                    onClick = {

                        showScreenshot =
                            false
                    },

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopEnd
                            )
                            .padding(12.dp)
                ) {

                    Text(
                        text =
                            "CLOSE",

                        color =
                            Color.White,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }


    // ---------------------------------------------------------
    // DASHBOARD CONTENT
    // ---------------------------------------------------------

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .background(
                CreamBackground
            )
            .windowInsetsPadding(
                WindowInsets.safeDrawing
            )
            .padding(
                horizontal = 18.dp
            ),


        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {


        // HEADER
        item {

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            HeaderSection(

                online =
                    online,

                battery =
                    battery,

                charging =
                    charging
            )
        }


        // ACTIVE SESSION
        item {

            ActiveSession()
        }


        // TITLE
        item {

            Text(

                text =
                    "Control Workstation",

                fontSize =
                    30.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    TextMain
            )


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(

                text =
                    "Manage power states, capture your desktop, and monitor device health remotely.",

                fontSize =
                    14.sp,

                color =
                    TextMuted,

                lineHeight =
                    21.sp
            )
        }


        // POWER ACTIONS
        item {

            PowerActionsCard(

                onLock = {

                    executeAction(
                        activity,
                        "lock"
                    )
                },


                onRestart = {

                    confirmAction =
                        "restart"
                },


                onCancelShutdown = {

                    executeAction(
                        activity,
                        "cancelShutdown"
                    )
                }
            )
        }


        // SCREENSHOT
        item {

            ScreenshotCard(

                capturing =
                    capturing,

                screenshot =
                    screenshot,


                onCapture = {

                    if (capturing) {
                        return@ScreenshotCard
                    }


                    capturing = true


                    Thread {

                        try {

                            val bytes =
                                RemoteApi.getScreenshot()


                            val bitmap =
                                BitmapFactory.decodeByteArray(
                                    bytes,
                                    0,
                                    bytes.size
                                )


                            activity.runOnUiThread {

                                screenshot =
                                    bitmap

                                capturing =
                                    false


                                Toast.makeText(

                                    activity,

                                    "Screenshot captured successfully",

                                    Toast.LENGTH_SHORT

                                ).show()
                            }

                        } catch (e: Exception) {

                            activity.runOnUiThread {

                                capturing =
                                    false


                                Toast.makeText(

                                    activity,

                                    e.message
                                        ?: "Screenshot failed",

                                    Toast.LENGTH_LONG

                                ).show()
                            }
                        }

                    }.start()
                },


                onOpen = {

                    showScreenshot =
                        true
                }
            )
        }


        // -----------------------------------------------------
        // STOP SERVER + SHUTDOWN PC
        // -----------------------------------------------------

        item {

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            ServerPowerControls(

                onStopServer = {

                    confirmAction =
                        "stopServer"
                },


                onShutdownPC = {

                    confirmAction =
                        "shutdown"
                }
            )
        }


        // FOOTER
        item {

            Text(

                text =
                    "Remote PC Control Center • Secure tunnel endpoint",

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 4.dp,
                            bottom = 20.dp
                        ),

                textAlign =
                    TextAlign.Center,

                fontSize =
                    11.sp,

                color =
                    TextMuted
            )
        }
    }
}


// ---------------------------------------------------------
// HEADER
// ---------------------------------------------------------

@Composable
fun HeaderSection(
    online: Boolean,
    battery: Int?,
    charging: Boolean
) {

    Column {

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(

                modifier = Modifier
                    .size(48.dp)
                    .clip(
                        RoundedCornerShape(15.dp)
                    )
                    .background(
                        Charcoal
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    imageVector =
                        Icons.Default.PowerSettingsNew,

                    contentDescription =
                        null,

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(24.dp)
                )
            }


            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text =
                        "Remote Control",

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        TextMain
                )


                Text(

                    text =
                        "Personal workstation node",

                    fontSize =
                        11.sp,

                    color =
                        TextMuted
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        Row(

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            StatusPill(

                text =
                    if (online)
                        "Online"
                    else
                        "Offline",

                dotColor =
                    if (online)
                        Emerald
                    else
                        Rose
            )


            StatusPill(

                text =
                    if (battery == null) {

                        "🔋 N/A"

                    } else if (charging) {

                        "🔋 $battery% ⚡"

                    } else {

                        "🔋 $battery%"
                    },

                dotColor =
                    Color.Transparent
            )
        }
    }
}


// ---------------------------------------------------------
// STATUS PILL
// ---------------------------------------------------------

@Composable
fun StatusPill(
    text: String,
    dotColor: Color
) {

    Surface(

        shape =
            CircleShape,

        color =
            Color.White,

        border =
            BorderStroke(
                1.dp,
                BorderWarm
            )
    ) {

        Row(

            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 7.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            if (
                dotColor !=
                Color.Transparent
            ) {

                Box(

                    modifier = Modifier
                        .size(7.dp)
                        .clip(
                            CircleShape
                        )
                        .background(
                            dotColor
                        )
                )


                Spacer(
                    modifier =
                        Modifier.width(7.dp)
                )
            }


            Text(

                text =
                    text,

                fontSize =
                    11.sp,

                color =
                    TextMuted,

                fontWeight =
                    FontWeight.Medium
            )
        }
    }
}


// ---------------------------------------------------------
// ACTIVE SESSION
// ---------------------------------------------------------

@Composable
fun ActiveSession() {

    Surface(

        shape =
            CircleShape,

        color =
            AmberLight,

        border =
            BorderStroke(
                1.dp,
                Color(0xFFFDE68A)
            )
    ) {

        Row(

            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 7.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(

                modifier = Modifier
                    .size(7.dp)
                    .clip(
                        CircleShape
                    )
                    .background(
                        Amber
                    )
            )


            Spacer(
                modifier =
                    Modifier.width(7.dp)
            )


            Text(

                text =
                    "ACTIVE ENDPOINT SESSION",

                fontSize =
                    10.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Amber,

                letterSpacing =
                    0.5.sp
            )
        }
    }
}


// ---------------------------------------------------------
// POWER ACTIONS CARD
// ---------------------------------------------------------

@Composable
fun PowerActionsCard(
    onLock: () -> Unit,
    onRestart: () -> Unit,
    onCancelShutdown: () -> Unit
) {

    Surface(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        color =
            CardWhite,

        border =
            BorderStroke(
                1.dp,
                BorderWarm
            ),

        shadowElevation =
            3.dp
    ) {

        Column(

            modifier =
                Modifier.padding(20.dp)
        ) {

            CardHeader(

                icon =
                    Icons.Default.Bolt,

                iconBackground =
                    AmberLight,

                iconColor =
                    Amber,

                title =
                    "Power Actions",

                subtitle =
                    "Manage computer session",

                label =
                    "CONTROL"
            )


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            Row(

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                ActionButton(

                    modifier =
                        Modifier.weight(1f),

                    text =
                        "Lock Session",

                    icon =
                        Icons.Default.Lock,

                    onClick =
                        onLock
                )


                ActionButton(

                    modifier =
                        Modifier.weight(1f),

                    text =
                        "Restart PC",

                    icon =
                        Icons.Default.Refresh,

                    dark =
                        true,

                    onClick =
                        onRestart
                )
            }


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            ActionButton(

                modifier =
                    Modifier.fillMaxWidth(),

                text =
                    "Cancel Shutdown",

                icon =
                    Icons.Default.Refresh,

                danger =
                    true,

                onClick =
                    onCancelShutdown
            )


            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )


            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(

                    text =
                        "SECURITY: ENCRYPTED",

                    fontSize =
                        9.sp,

                    color =
                        TextMuted
                )


                Text(

                    text =
                        "TYPE: REST API",

                    fontSize =
                        9.sp,

                    color =
                        TextMuted
                )
            }
        }
    }
}


// ---------------------------------------------------------
// CARD HEADER
// ---------------------------------------------------------

@Composable
fun CardHeader(
    icon: ImageVector,
    iconBackground: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    label: String
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(

            modifier = Modifier
                .size(44.dp)
                .clip(
                    RoundedCornerShape(13.dp)
                )
                .background(
                    iconBackground
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(

                imageVector =
                    icon,

                contentDescription =
                    null,

                tint =
                    iconColor,

                modifier =
                    Modifier.size(21.dp)
            )
        }


        Spacer(
            modifier =
                Modifier.width(11.dp)
        )


        Column(

            modifier =
                Modifier.weight(1f)
        ) {

            Text(

                text =
                    title,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    TextMain
            )


            Text(

                text =
                    subtitle,

                fontSize =
                    11.sp,

                color =
                    TextMuted
            )
        }


        Surface(

            shape =
                RoundedCornerShape(7.dp),

            color =
                Color(0xFFF5F5F4)
        ) {

            Text(

                text =
                    label,

                modifier =
                    Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 5.dp
                    ),

                fontSize =
                    8.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    TextMuted
            )
        }
    }
}


// ---------------------------------------------------------
// ACTION BUTTON
// ---------------------------------------------------------

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    dark: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit
) {

    val background =
        when {

            dark ->
                Charcoal

            danger ->
                RoseLight

            else ->
                Color.White
        }


    val contentColor =
        when {

            dark ->
                Color.White

            danger ->
                Rose

            else ->
                TextMain
        }


    Surface(

        modifier =
            modifier
                .height(52.dp)
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(13.dp),

        color =
            background,

        border =
            BorderStroke(

                1.dp,

                when {

                    danger ->
                        Color(0xFFFECDD3)

                    dark ->
                        Charcoal

                    else ->
                        BorderWarm
                }
            )
    ) {

        Row(

            modifier =
                Modifier.fillMaxSize(),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(

                imageVector =
                    icon,

                contentDescription =
                    null,

                tint =
                    contentColor,

                modifier =
                    Modifier.size(17.dp)
            )


            Spacer(
                modifier =
                    Modifier.width(7.dp)
            )


            Text(

                text =
                    text,

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    contentColor
            )
        }
    }
}


// ---------------------------------------------------------
// SCREENSHOT CARD
// ---------------------------------------------------------

@Composable
fun ScreenshotCard(
    capturing: Boolean,
    screenshot: Bitmap?,
    onCapture: () -> Unit,
    onOpen: () -> Unit
) {

    Surface(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        color =
            CardWhite,

        border =
            BorderStroke(
                1.dp,
                BorderWarm
            ),

        shadowElevation =
            3.dp
    ) {

        Column(

            modifier =
                Modifier.padding(20.dp)
        ) {

            CardHeader(

                icon =
                    Icons.Default.CameraAlt,

                iconBackground =
                    IndigoLight,

                iconColor =
                    Indigo,

                title =
                    "Desktop Preview",

                subtitle =
                    "Capture current active display",

                label =
                    "LIVE VIEW"
            )


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            Button(

                onClick =
                    onCapture,

                enabled =
                    !capturing,

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(13.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Charcoal
                    )
            ) {

                if (capturing) {

                    CircularProgressIndicator(

                        modifier =
                            Modifier.size(18.dp),

                        color =
                            Color.White,

                        strokeWidth =
                            2.dp
                    )


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Text(
                        "Capturing..."
                    )

                } else {

                    Icon(

                        imageVector =
                            Icons.Default.CameraAlt,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(18.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Text(
                        "Capture Screenshot"
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        Color(0xFFFAF9F5)
                    )
                    .border(
                        1.dp,
                        BorderWarm,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(
                        enabled =
                            screenshot != null
                    ) {

                        onOpen()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                if (screenshot == null) {

                    Column(

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.CameraAlt,

                            contentDescription =
                                null,

                            tint =
                                Color(0xFFA8A29E),

                            modifier =
                                Modifier.size(38.dp)
                        )


                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )


                        Text(

                            text =
                                "No screenshot captured yet",

                            fontSize =
                                12.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                TextMuted
                        )


                        Text(

                            text =
                                "Click capture to stream desktop frame",

                            fontSize =
                                10.sp,

                            color =
                                Color(0xFFA8A29E)
                        )
                    }

                } else {

                    Image(

                        bitmap =
                            screenshot.asImageBitmap(),

                        contentDescription =
                            "Desktop screenshot",

                        contentScale =
                            ContentScale.Fit,

                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            Text(

                text =
                    if (screenshot == null)
                        "STATUS: IDLE"
                    else
                        "STATUS: CAPTURED",

                fontSize =
                    9.sp,

                color =
                    TextMuted
            )
        }
    }
}


// ---------------------------------------------------------
// STOP SERVER + SHUTDOWN PC
// ---------------------------------------------------------

@Composable
fun ServerPowerControls(
    onStopServer: () -> Unit,
    onShutdownPC: () -> Unit
) {

    Column(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment.CenterVertically
        ) {


            // -------------------------------------------------
            // STOP SERVER
            // -------------------------------------------------

            Surface(

                modifier =
                    Modifier
                        .size(58.dp)
                        .clickable {

                            onStopServer()
                        },

                shape =
                    CircleShape,

                color =
                    Color.White,

                border =
                    BorderStroke(
                        1.5.dp,
                        Color(0xFFFECDD3)
                    ),

                shadowElevation =
                    3.dp
            ) {

                Box(

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Stop,

                        contentDescription =
                            "Stop remote server",

                        tint =
                            Rose,

                        modifier =
                            Modifier.size(25.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.width(18.dp)
            )


            // -------------------------------------------------
            // SHUTDOWN PC
            // -------------------------------------------------

            Surface(

                modifier =
                    Modifier
                        .size(66.dp)
                        .clickable {

                            onShutdownPC()
                        },

                shape =
                    CircleShape,

                color =
                    Color.White,

                border =
                    BorderStroke(
                        1.5.dp,
                        Color(0xFFFECDD3)
                    ),

                shadowElevation =
                    4.dp
            ) {

                Box(

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.PowerSettingsNew,

                        contentDescription =
                            "Shutdown PC",

                        tint =
                            Rose,

                        modifier =
                            Modifier.size(31.dp)
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        Text(

            text =
                "Stop server   •   Shutdown PC",

            fontSize =
                11.sp,

            fontWeight =
                FontWeight.Medium,

            color =
                TextMuted
        )
    }
}


// ---------------------------------------------------------
// EXECUTE BACKEND ACTION
// ---------------------------------------------------------

private fun executeAction(
    activity: Activity,
    action: String
) {

    Thread {

        try {

            val result =
                RemoteApi.performAction(action)


            activity.runOnUiThread {

                Toast.makeText(

                    activity,

                    result.message,

                    if (result.success)
                        Toast.LENGTH_SHORT
                    else
                        Toast.LENGTH_LONG

                ).show()
            }

        } catch (e: Exception) {

            activity.runOnUiThread {

                Toast.makeText(

                    activity,

                    e.message
                        ?: "Request failed",

                    Toast.LENGTH_LONG

                ).show()
            }
        }

    }.start()
}