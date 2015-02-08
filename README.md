# TCPChat

TCPChat is a server-based open source chat, which is equipped with the following features:

  - Multi client chat server
  - Group and private chat in tabs
  - Userlist

### Version

0.3.1

### Technology

TCPChat is based on the network protocol <b>TCP</b>.
In addition to this, it uses a <b>self-developed packet-framework</b>, which allows an easy transfer of data from cilent to server and vice versa.

### Installation
Currently there is <b>no installer</b>. Just pull the repository:

```sh
$ git clone [git-repo-url] tcpchat
$ cd tcpchat/
$ chmod 777 TcpChat/ #for log-files
```
On Windows, you can execute .jar-Files by double-clicking on them. If it doesn't work, you can try to fix your system with [jarfix.exe].

You can find the latest stable builds in the folder <b>"*stable_builds*"</b>

### Starting stable Server
Open your favorite terminal and run this command:

```sh
$ cd stable_builds/
$ java -jar TCPChat_Server.jar <port> <logging yes/no>
```

### Starting stable Client
Open your favorite terminal and run this command:

```sh
$ cd stable_builds/
$ java -jar TCPChat_Client.jar
```

### Project Info
TCPChat is a netbeans project, which is developed with <b>JDK 1.7.0_71</b> with regards to an eventually <b>upcoming Android App</b>.

### Support
This project is developed by Manuel Schmid.
If you have any questions or feature requests, don't hesitate to contact me on [Twitter] or my [homepage].

### Current Todo's

 - [ ] AES-256-Encryption
 - [ ] Add configuration file for server
 - [ ] Add feature to send files
 - [ ] Implement Database
 - [ ] Save received messages in local file system or database
 - [ ] Implement user accounts / Create client id for claiming names
 - [ ] Better prevention of spam

License
----

This software is under MIT-License

[mash1t.de]:http://mash1t.de/
[homepage]:http://mash1t.de/
[Twitter]:https://twitter.com/mash1t
[jarfix.exe]:http://johann.loefflmann.net/en/software/jarfix/index.html
