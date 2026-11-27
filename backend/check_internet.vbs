Option Explicit

Dim shell, http, vbsPath

Set shell = CreateObject("WScript.Shell")

vbsPath = "C:\Users\sanka\VS Code\Remote_system_express\remote-server.vbs"

Do
    ' Check internet connection
    Set http = CreateObject("WinHttp.WinHttpRequest.5.1")

    On Error Resume Next

    http.Open "HEAD", "https://www.google.com", False
    http.SetTimeouts 5000, 5000, 5000, 5000
    http.Send

    If Err.Number = 0 And http.Status >= 200 And http.Status < 500 Then
        On Error GoTo 0

        ' Internet connected
        shell.Run "wscript.exe """ & vbsPath & """", 0, False

        Exit Do
    End If

    On Error GoTo 0

    ' Internet unavailable - wait 3 minutes
    WScript.Sleep 180000

Loop

Set http = Nothing
Set shell = Nothing