# Java Chat Server with File Transfer and Simple Drawing Capabilities

Brief description and steps to run:

1. Compile the code: javac *.java
2. Start Chat Server from the command line: java Server 678 (Where 678 specifies the port number where server runs)
	As soon as you start your server a directory named Server will be created on server side which stores all the files that has been transferred from one clientto another
4. Add the IP addres of Server on line 90 in Client.java file and compile it: javac Client.java
5. Start the client from the command line on different machine: java Client (integer or String) 678 (Where the first parameter will create a directory with the number or string you provide as name for it at the client side, and it stores the files received by client)
6. Enter the username in the top left corner textfield of the client chat window and press connect button
7. Start 2nd/3rd or more Clients, using the above two steps.
8. Exchange messages by typing into the bottom textfield and pressing enter button of keyboard.
9. To share files click on the SendFile button on top right
10. To draw click and drag the mouse on the botton colored area.