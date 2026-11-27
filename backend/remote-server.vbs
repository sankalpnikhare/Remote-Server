Set shell = CreateObject("WScript.Shell")

shell.CurrentDirectory = "C:\Users\sanka\VS Code\Remote_system_express"

shell.Run "node server.js", 0, False
WScript.Sleep 5000
shell.Run "ngrok http 5000", 0, False