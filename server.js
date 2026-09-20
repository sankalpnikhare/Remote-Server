require("dotenv").config();

const express = require("express");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");
const screenshot = require("screenshot-desktop");
const si = require("systeminformation");
const { execFile } = require("child_process");
const path = require("path");

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

// Serve the frontend
app.use(express.static("public"));

// Execute Windows commands
function runWindows(command, args = []) {
  return new Promise((resolve, reject) => {
    execFile(command, args, (error, stdout, stderr) => {
      if (error) {
        reject(new Error(stderr || error.message));
        return;
      }

      resolve(stdout);
    });
  });
}

// Health check
app.get("/api/health", (req, res) => {
  res.json({
    online: true,
    hostname: require("os").hostname(),
  });
});

// Wi-Fi and battery status
app.get("/api/status", async (req, res) => {
  try {
    const battery = await si.battery();
    const interfaces = await si.networkInterfaces();

    const activeInterface = interfaces.find(
      (network) =>
        network.operstate === "up" &&
        !network.internal &&
        network.ip4
    );

    res.json({
      internet: Boolean(activeInterface),
      battery: battery.hasBattery ? battery.percent : null,
      charging: battery.hasBattery
        ? Boolean(battery.isCharging)
        : false,
    });
  } catch (error) {
    console.error("Status error:", error.message);

    res.status(500).json({
      error: "Unable to retrieve system status",
    });
  }
});

// Power controls
app.post("/api/action", async (req, res) => {
  const { action } = req.body;

  try {
    switch (action) {
      case "lock":
        await runWindows("rundll32.exe", [
          "user32.dll,LockWorkStation",
        ]);
        break;

      case "restart":
        await runWindows("shutdown.exe", ["/r", "/t", "0"]);
        break;

      case "cancelShutdown":
        await runWindows("shutdown.exe", ["/a"]);
        break;

      case "stopServer":
        await runWindows("cscript.exe", [
          "//nologo",
          path.join(
            "C:",
            "Users",
            "sanka",
            "VS Code",
            "Remote_system_express",
            "stop-remote-server.vbs"
          ),
        ]);
        break;

      default:
        return res.status(400).json({
          error: "Unsupported action",
        });
    }

    res.json({
      success: true,
      action,
    });
  } catch (error) {
    console.error("Action error:", error.message);

    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// Take a screenshot
app.get("/api/screenshot", async (req, res) => {
  try {
    const image = await screenshot({ format: "png" });

    res.setHeader("Content-Type", "image/png");
    res.send(image);
  } catch (error) {
    console.error("Screenshot error:", error.message);

    res.status(500).json({
      error: "Unable to take screenshot",
    });
  }
});

// Start server
app.listen(PORT, HOST, () => {
  console.log(`Server running at http://${HOST}:${PORT}`);
});