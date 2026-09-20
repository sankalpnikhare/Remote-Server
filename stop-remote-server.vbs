Set shell = CreateObject("WScript.Shell")

shell.Run "powershell -NoProfile -Command ""Stop-Process -Name node -Force -ErrorAction SilentlyContinue; Stop-Process -Name ngrok -Force -ErrorAction SilentlyContinue""", 0, True

Set shell = Nothing