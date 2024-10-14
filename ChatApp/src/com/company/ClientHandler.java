package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Client Handler class is a Runnable (for multithreading) that allows a server application to manage
 * communication with a client application. This will handle setting up input and output streams
 * as well as setting up a name, ip, and port for the connected client.
 */
public class ClientHandler implements Runnable {
    public Scanner scn = new Scanner(System.in);
    private String name;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private Socket s;
    private boolean isloggedin;
    private String ipAddress;
    private int port;

    /**
     * ClientHandler Constructor. Creates state for initial communication between a specific client and server.
     * @param s Socket Connection for the Client and Server
     * @param user Name of the User running at this client
     * @param ipAddress IP address of connected client
     * @param port Port the Client software is running on for the client
     * @param dis Input Stream
     * @param dos Output Stream
     */
    public ClientHandler(Socket s,String user, String ipAddress, int port,
                         DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = user;
        this.ipAddress = ipAddress;
        this.port = port;
        this.s = s;
        this.isloggedin = true;

    }

    /**
     * Prints Name, Address, and Port of a Client to the Terminal.
     */
    public void writeOutput() {
        System.out.println(this.name+" is on address: "+this.ipAddress+" at port "+this.port);
    }

    /**
     * Run method for use in multi-threaded application. This will Receive a message from a connected client.
     * It will then tokenize the message to determine the message, recipient address, and recipient port. It will
     * then loop through all available clients in the Server AR vector to find the specified client and send the
     * message to them. If the message "logout" is detected connection to the input and output streams for them
     * will be closed. Also sets status for that client to isloggedin to false.
     */
    @Override
    public void run() {

        String received;
        messageLoop:
        while (true) {
            try {
                // receive the string
                received = dis.readUTF();

                System.out.println(received);

                // break the string into message and recipient part
                StringTokenizer st = new StringTokenizer(received, "#");
                String MsgToSend = st.nextToken();
                String addr = st.nextToken();
                int port = Integer.parseInt(st.nextToken());

                // search for the recipient in the connected devices list.
                // ar is the vector storing client of active users
                for (ClientHandler mc : Server.ar) {
                    // if the recipient is found, write on its
                    // output stream
                    if (mc.ipAddress.equals(addr) && mc.port==port && mc.isloggedin == true) {
                        if (MsgToSend.equals("logout")) {
                            mc.dos.writeUTF(this.name+" has logged out");
                            this.isloggedin = false;
                            this.s.close();
                            break messageLoop;
                        }
                        else
                            mc.dos.writeUTF(this.name+":" + MsgToSend);
                        break;
                    }

                }
            } catch (IOException e) {

                e.printStackTrace();
            }

        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
