# 🖥️ Remote-Server

**A lightweight remote control and monitoring server for Windows machines, built with Node.js and Express.js.**

Remote-Server allows you to partially control and monitor your Windows PC using your smartphone or another device through a web-based interface.

Run the Express server on your Windows machine, connect to it through your local network, or access it remotely using a reverse proxy such as **Ngrok**.

## ✨ Features

- **Remote PC Control** — Perform supported operations on your Windows machine remotely.
- **System Monitoring** — Monitor what's happening on your machine.
- **Local Network Access** — Control your PC using another device connected to the same network.
- **Remote Access with Ngrok** — Access your machine from anywhere using a public Ngrok URL.
- **Automated Server Startup** — Start both Express and Ngrok using a single VBS script.
- **Automated Internet Monitoring** — Automatically detect internet connectivity and manage server startup.
- **Automatic Reconnection** — Restart the remote server workflow when internet connectivity is restored.

## 🏗️ How It Works

The project uses an Express.js server running locally on your Windows PC.

For remote access, Ngrok creates a secure public tunnel that forwards incoming requests to your local Express server.

<pre>
        📱 Smartphone / Client
                 |
                 | HTTP Request
                 |
                 ▼
          🌐 Ngrok Tunnel
                 |
                 | Port Forwarding
                 |
                 ▼
          🖥️ Express Server
            (Windows PC)
                 |
                 ▼
          ⚙️ Windows System
</pre>

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- [Node.js](https://nodejs.org/)
- [Ngrok](https://ngrok.com/)
- Windows Operating System

### 1. Clone the Repository

```bash
git clone https://github.com/sankalpnikhare/Remote-Server.git
cd Remote-Server
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Configure Environment Variables

Create a `.env` file in the project root and configure the required environment variables.

```env
PORT=5000
REMOTE_TOKEN=your_secret_token
```

Use the variable names expected by your application.

### 4. Start the Express Server

```bash
node server.js
```

Your server should now be running locally.

## 🌍 Accessing Your PC Remotely

By default, you can access your server from devices connected to the same local network.

To access your PC from outside your local network, use Ngrok.

### Using Ngrok Manually

Start your Express server and execute:

```bash
ngrok http 5000
```

Replace `5000` with the port on which your Express server is running.

Ngrok will generate a public forwarding URL.

Use that URL to access your remote server from your smartphone or another device.

## ⚡ Automated Startup Using VBS Scripts

The project includes VBS scripts to simplify the entire startup and shutdown process.

| Script | Function |
|---|---|
| `remote-server.vbs` | Starts the Express server and Ngrok agent |
| `stop-remote-server.vbs` | Stops both the Express server and Ngrok |
| `check_internet.vbs` | Monitors internet connectivity and manages server startup |

### Automated Workflow

The `check_internet.vbs` script automates the process:

1. Checks whether the PC is connected to the internet.
2. If connected, executes `remote-server.vbs`.
3. If disconnected, waits for 3 minutes and checks again.
4. If the internet connection drops, it retries the startup workflow when connectivity is restored.

This eliminates the need to manually start the server every time.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Node.js | JavaScript runtime |
| Express.js | Backend server |
| Ngrok | Reverse proxy and secure tunneling |
| VBS (VBScript) | Windows automation and process management |
| Windows | Host operating system |

## 🔐 Security

- Uses a secret token for remote access authentication.
- Avoid exposing your remote server without authentication.
- Keep your `REMOTE_TOKEN` private.
- Never commit your `.env` file or other sensitive credentials to GitHub.
- Expose only the endpoints you actually need.

**Important:** Anyone who obtains access to your public Ngrok URL may be able to reach your server. Always use authentication and validate incoming requests before allowing system-level operations.

## ⚠️ Limitations

- The host machine must be powered on.
- Remote access requires an active internet connection.
- Without Ngrok or another tunneling solution, access is limited to the local network.
- The project currently supports Windows machines only.
- Remote control is limited to the operations implemented in the application.

## 🔮 Future Improvements

- More system monitoring capabilities.
- Improved remote command execution.
- Better authentication and authorization.
- Real-time system status updates.
- Improved connection handling and reliability.

## 👨‍💻 Author

**Sankalp Nikhare**

- GitHub: [@sankalpnikhare](https://github.com/sankalpnikhare)
- LinkedIn: [Sankalp Nikhare](https://www.linkedin.com/in/sankalp-nikhare-404a08259/)

---

⭐ If you find this project interesting, consider giving it a star!