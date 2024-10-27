## Multi-Client-Chat
Multi-client chat using threads, RSA encryption, and md5 hash in java 

To compile:
```bash
javac Chat/*.java 
```

To Run: 
```bash
java Chat.Server 
```
```bash
java Chat.ClientApp 
```

In the chat you can use 2 commands:
```bash
1. /whisper <username> <message>
```
```bash
2. /connectedUsers
```

1. /whisper will send a direct message to the user given and it will be visible only to him and you.
2. /connectedUsers will print a list of all the connected users except you and the output of this command will be visible only to you.
