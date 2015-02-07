# TCPChat

TCPChat is a server-based open source chat, which is equipped with the following features:

  - Multi client chat server
  - Group and private chat
  - Userlist
  - Communication with packets

### Version

0.2.0

### Technology

TCPChat uses a self-developed packet-framework, which allows an easy transfer of data from cilent to server and vice versa.

### Installation
Currently there is no installer. You can just pull the repository.
```sh
$ git clone [git-repo-url] tcpchat
$ cd tcpchat/
$ chmod 777 TcpChat/ #for log-files
```
You can execute the .jar-Files in the way as you can do it with .exe files. You can find stable builds under "*stable_builds*"

### Starting stable Server
Open your favorite Terminal and run this command:

```sh
$ cd stable_builds/
$ java -jar TCPChat_Server.jar
```

### Starting stable Client
Open your favorite Terminal and run this command:

```sh
$ cd stable_builds/
$ java -jar TCPChat_Client.jar
```

### Support

If you have any questions or feature requests, don't hesitate to contact me at [mash-it.org]

### Current Todo's

 - [x] Show private messages in tabs
 - [ ] AES-256-Encryption
 - [ ] Add configuration file for server
 - [ ] Add feature to send files
 - [ ] Implement Database
 - [ ] Implement user accounts
 - [ ] Better prevention of spam

License
----

This software is under MIT-License

[mash-it.org]:http://mash-it.org/
