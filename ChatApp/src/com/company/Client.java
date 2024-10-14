package com.company;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Client application that will try to connect via socket to a specified server ip and port. Creates and input
 * and output stream to that specified server. Grabs relevant user info and Will announce to server connection
 * information via writeoutput method. User will then pick an IP address and port of a client they would like to
 * communicate with.After this 2 threads will run in parallel. One will hand messaging to the other client over the
 * chat server, the other will await messages from the other client that it receives with the chat server.
 */
public class Client
{
    final static int ServerPort = 1234;

    public static void main(String args[]) throws UnknownHostException, IOException, InterruptedException {
        boolean connected = true;
        Scanner scn = new Scanner(System.in);

        // Insert Address of Machine running server application
        //   InetAddress ip = InetAddress.getByName("172.16.1.137");
        System.out.println("What is the Ip of the Machine running the server application?");
        String ip = scn.nextLine();
        //  InetAddress ip = InetAddress.getByAddress();



        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        System.out.println("What is your User name?");
        String userName = scn.nextLine();
        dos.writeUTF(userName);
        Thread.sleep(2000);
        for(ClientHandler mc : Server.ar)
        {
            mc.writeOutput();
        }

        System.out.println("Which IP would you like to connect to?");
        String addr = scn.next();
        System.out.println("Which port are we communicating on?");
        int port = scn.nextInt();
        scn.nextLine();

        System.out.println("Start Chatting"); //make an if/ese to check if it really has an active connection
        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()

        {
            @Override
            public void run() {
                while (true) {

                    // read the message to deliver.
                    String msgpart = scn.nextLine();
                    String msg = msgpart+"#/"+addr+"#"+port;
                    if(msgpart.equals("logout"))
                    {
                        System.out.println("Are you sure you would like to quit? (Y/N)");
                        String confirm = scn.nextLine();
                        if(confirm.equalsIgnoreCase("Y"))
                        {
                            System.out.println("Logging out.");
                            try {
                                // write on the output stream
                                dos.writeUTF(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            finally
                            {
                                System.exit(0);
                            }
                        }
                    }
                    else {
                        try {
                            // write on the output stream
                            dos.writeUTF(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                    finally{
//                        if(msgpart.equals("logout")) {
//                            System.out.println("Are you sure you would like exit? ");
//
//                            if (msgpart.equals("Yes")) {
//                                System.exit(0);
//                                msgpart.equals("The other user has left");
//                            }
//                         else
//                             scn.nextLine();
//                        }
//                    }
                    }
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                while (true) {
                    try {
                        // read the message sent to this client
                        String msg = dis.readUTF();
                        System.out.println(msg);
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            }
        });

        sendMessage.start();
        readMessage.start();

    }
}
/**
 * Run method for use in multi-threaded application. This will Receive a message from a connected client.
 * It will then tokenize the message to determine the message, recipient address, and recipient port. It will
 * then loop through all available clients in the Server AR vector to find the specified client and send the
 * message to them. If the message "logout" is detected, the client will be removed from the vector and connection
 * to the input and output streams for them will be closed.
 */