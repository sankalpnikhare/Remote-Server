# Remote-Server
A VibeCoded Remote server for Windows machine made with Express.

Run this code in your local Env and you can partially control your windows pc and monitor whats going on your machine.

The only limit is that you can only control it when you machine and Phone are connected to same device.

To control it remotely use Reverse Proxy for example Ngrok.

Forward the port to Ngrok agent with Command "ngrok http {port where the server is running}"

With the given link by Ngrok you can Control your pc remotely. 

Or instead of that you can directly run both the express server and ngrok server by running the remote-server.vbs file provided.(stop-remote-server.vbs to stop both the servers).


To automate the whole Process I have provided the check_internet.vbs file to automate all the process .
If PC is connected to internet -> Run remote-server.vbs 
If not -> Wait 3 minutes , Try again 

If connected , then disconnected -> Try Again
