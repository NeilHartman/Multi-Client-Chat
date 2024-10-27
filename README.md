# Multi-Client-Chat
Multi-client chat using threads, RSA encryption, and md5 hash in java 

To compile: javac Chat/*.java 
To Run: 1. java Chat.Server 2. java Chat.ClientApp

In the chat you can use 2 commands: 1. '''/whisper {username} {message}''' 2. '''/connectedUsers'''

1. /whisper will send a direct message to the user given and it will be visible only to him and you.
2. /connectedUsers will print a list of all the connected users except you and the output of this command will be visible only to you.
