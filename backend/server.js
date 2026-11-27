require("dotenv").config();

const express = require("express");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");
const screenshot = require("screenshot-desktop");
const si = require("systeminformation");
const { execFile } = require("child_process");
const path = require("path");
const crypto = require("crypto");

const app = express();

const PORT = process.env.PORT || 5000;

// Trusting the proxy
app.set("trust proxy", 1);

// Bind locally for use with ngrok
const HOST = "127.0.0.1";

app.use(
    helmet({
        contentSecurityPolicy: false,
    })
);

app.use(express.json({ limit: "1mb" }));

app.use(
    rateLimit({
        windowMs: 60 * 1000,
        max: 60,
        standardHeaders: true,
        legacyHeaders: false,
    })
);


// ============================================================
// REMOTE TOKEN AUTHENTICATION
// ============================================================

const REMOTE_TOKEN = process.env.REMOTE_TOKEN;

if (!REMOTE_TOKEN) {
    throw new Error("REMOTE_TOKEN is missing from the .env file");
}


// Get cookie value
function getCookie(req, name) {
    const cookies = req.headers.cookie || "";

    const item = cookies
        .split(";")
        .find((part) =>
            part.trim().startsWith(`${name}=`)
        );

    if (!item) {
        return null;
    }

    try {
        return decodeURIComponent(
            item.trim().slice(name.length + 1)
        );
    } catch {
        return null;
    }
}


// Secure token comparison
function tokenMatches(value) {
    if (!value || typeof value !== "string") {
        return false;
    }

    const provided = Buffer.from(value);
    const expected = Buffer.from(REMOTE_TOKEN);

    return (
        provided.length === expected.length &&
        crypto.timingSafeEqual(provided, expected)
    );
}


// Authentication middleware
function requireAuth(req, res, next) {
    const authCookie = getCookie(req, "remote_auth");

    if (!tokenMatches(authCookie)) {
        return res.status(401).json({
            error: "Unauthorized. Enter the remote key.",
        });
    }

    next();
}


// ============================================================
// LOGIN
// ============================================================

app.post("/api/auth/login", (req, res) => {

    const { key } = req.body || {};

    if (!tokenMatches(key)) {
        return res.status(401).json({
            error: "Invalid remote key",
        });
    }

    // HTTP-only authentication cookie
    res.setHeader(
        "Set-Cookie",
        "remote_auth=" +
        encodeURIComponent(REMOTE_TOKEN) +
        "; HttpOnly; SameSite=Strict; Path=/"
    );

    res.json({
        success: true,
        message: "Authentication successful",
    });
});


// ============================================================
// LOGOUT
// ============================================================

app.post("/api/auth/logout", (req, res) => {

    res.setHeader(
        "Set-Cookie",
        "remote_auth=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0"
    );

    res.json({
        success: true,
        message: "Logged out successfully",
    });
});


// ============================================================
// AUTH STATUS
// ============================================================

app.get("/api/auth/status", requireAuth, (req, res) => {

    res.json({
        authenticated: true,
    });
});


// ============================================================
// SERVE FRONTEND
// ============================================================

app.use(express.static("public"));


// ============================================================
// PROTECT API ROUTES
// ============================================================

app.use("/api", (req, res, next) => {

    if (req.path.startsWith("/auth/")) {
        return next();
    }

    return requireAuth(req, res, next);
});


// ============================================================
// WINDOWS COMMAND EXECUTOR
// ============================================================

function runWindows(command, args = []) {

    return new Promise((resolve, reject) => {

        execFile(
            command,
            args,
            (error, stdout, stderr) => {

                if (error) {

                    reject(
                        new Error(
                            stderr || error.message
                        )
                    );

                    return;
                }

                resolve(stdout);
            }
        );

    });
}


// ============================================================
// HEALTH CHECK
// ============================================================

app.get("/api/health", (req, res) => {

    res.json({
        online: true,
        hostname: require("os").hostname(),
    });

});


// ============================================================
// SYSTEM STATUS
// ============================================================

app.get("/api/status", async (req, res) => {

    try {

        const battery = await si.battery();

        const interfaces =
            await si.networkInterfaces();

        const activeInterface =
            interfaces.find(
                (network) =>
                    network.operstate === "up" &&
                    !network.internal &&
                    network.ip4
            );

        res.json({

            internet:
                Boolean(activeInterface),

            battery:
                battery.hasBattery
                    ? battery.percent
                    : null,

            charging:
                battery.hasBattery
                    ? Boolean(battery.isCharging)
                    : false,
        });

    } catch (error) {

        console.error(
            "Status error:",
            error.message
        );

        res.status(500).json({

            error:
                "Unable to retrieve system status",

        });

    }

});


// ============================================================
// POWER / REMOTE ACTIONS
// ============================================================

app.post("/api/action", async (req, res) => {

    const { action } = req.body || {};

    try {

        switch (action) {


            // ------------------------------------------------
            // LOCK WINDOWS
            // ------------------------------------------------

            case "lock":

                await runWindows(
                    "rundll32.exe",
                    [
                        "user32.dll,LockWorkStation",
                    ]
                );

                break;


            // ------------------------------------------------
            // RESTART WINDOWS
            // ------------------------------------------------

            case "restart":

                await runWindows(
                    "shutdown.exe",
                    [
                        "/r",
                        "/t",
                        "0",
                    ]
                );

                break;


            // ------------------------------------------------
            // SHUTDOWN WINDOWS
            // ------------------------------------------------

            case "shutdown":

                await runWindows("shutdown.exe", [
                    "/s",
                    "/f",
                    "/t",
                    "0"
                ]);

                break;


            // ------------------------------------------------
            // CANCEL PENDING SHUTDOWN
            // ------------------------------------------------

            case "cancelShutdown":

                await runWindows(
                    "shutdown.exe",
                    [
                        "/a",
                    ]
                );

                break;


            // ------------------------------------------------
            // STOP NODE.JS REMOTE SERVER
            // ------------------------------------------------

            case "stopServer":

                await runWindows(
                    "cscript.exe",
                    [
                        "//nologo",

                        // stop-remote-server.vbs should
                        // be in the same directory as server.js
                        path.join(
                            __dirname,
                            "stop-remote-server.vbs"
                        ),
                    ]
                );

                break;


            // ------------------------------------------------
            // INVALID ACTION
            // ------------------------------------------------

            default:

                return res.status(400).json({

                    error:
                        "Unsupported action",

                });

        }


        // ----------------------------------------------------
        // SUCCESS RESPONSE
        // ----------------------------------------------------

        res.json({

            success: true,

            action,

            message:
                action === "shutdown"
                    ? "Shutdown command sent"

                    : action === "stopServer"
                        ? "Server shutdown command sent"

                        : "Action completed",

        });


    } catch (error) {

        console.error(
            "Action error:",
            error.message
        );

        res.status(500).json({

            success: false,

            error:
                error.message,

        });

    }

});


// ============================================================
// SCREENSHOT
// ============================================================

app.get("/api/screenshot", async (req, res) => {

    try {

        const image =
            await screenshot({
                format: "png",
            });

        res.setHeader(
            "Content-Type",
            "image/png"
        );

        res.send(image);

    } catch (error) {

        console.error(
            "Screenshot error:",
            error.message
        );

        res.status(500).json({

            error:
                "Unable to take screenshot",

        });

    }

});


// ============================================================
// START SERVER
// ============================================================

app.listen(
    PORT,
    HOST,
    () => {

        console.log(
            `Server running at http://${HOST}:${PORT}`
        );

    }
);