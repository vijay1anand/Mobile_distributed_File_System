# Mobile-Distributed-File-System-DFS2
Group 2
Project Code DFS2

The project aimed at making a prototype Distributed File System for Android Platform.
The GridViewUpdate app is the master application, whereas Client app is the slave application.
The clients should be on the same network as the master. Also, there are a couple of things that have been hardcoded in the apps for testing purposes. For example the large files to be distributed, etc.
Any large file uploaded in the master node in the Cloud folder is broken into 50MB chunks. These chunks are then shared among connected clients in the cluster. 

Further goals are:
  --Any file is made available to the client on demand
  --Keep the chunks compressed(Lempel Ziv maybe)
  --Make file transfer faster by transfering over multiple threads
  --Introducing redundancy for data loss
