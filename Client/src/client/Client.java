/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import static messages.Serializer.*;
import messages.NameKeyPair;
import gui.CreateClientFrame;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;

/**
 *
 * @author Diogo
 */
public class Client extends Thread {

    private static final int PACKET_SIZE = 65535;
    private static final int NEW_CLIENT_SERVER_PORT_NUMBER = 8000;
    private DatagramSocket socket;
    private String username;
    private SecretKey clientKey;
    
    public Client() {
        this.socket = null;
    }
    
    @Override
    public void run() {
        // UDP client code
//        try {
//            socket = new DatagramSocket();
//            
//            String outputBuffer = "MEH";
//            byte [] m = outputBuffer.getBytes();
//            InetAddress host = InetAddress.getByName("localhost");
//            DatagramPacket request = new DatagramPacket(m, outputBuffer.length(), host, PORT_NUMBER);
//            socket.send(request);
//            
//            byte[] inputBuffer = new byte[PACKET_SIZE];
//            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
//            socket.receive(response);
//            
//            System.out.println("Reply: " + new String(response.getData()));
//        } catch (SocketException e) {
//            System.out.println("ERROR | Socket: " + e.getMessage());
//        } catch (IOException e) {
//            System.out.println("ERROR | IO: " + e.getMessage());
//        } finally {
//            if(socket != null)
//                socket.close();
//        }
    }
    
    public void createClient(String user, String kdcIpAddress) throws NoSuchAlgorithmException {
        try {
            // Creating user and clientKey pair
            this.username = user;
            this.clientKey = KeyGenerator.getInstance("DES").generateKey();
            NameKeyPair newClient = new NameKeyPair(user, clientKey);
            
            // Starting socket and sending request to create user
            socket = new DatagramSocket();
            byte[] outputBuffer = serializeObject(newClient);
            InetAddress host = InetAddress.getByName(kdcIpAddress);
            DatagramPacket request = new DatagramPacket(outputBuffer, outputBuffer.length, host, NEW_CLIENT_SERVER_PORT_NUMBER);
            socket.send(request);
            
            // Prepares and waits for the KDC response
            // Not scalable! I chose not to create handlers in separate threads
            // given that this is being developed only for educational purposes
            byte[] inputBuffer = new byte[PACKET_SIZE];
            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(response);
            
            // Determines whether the username was already taken on the KDC 
            boolean uniqueUsername = (boolean) deserializeObject(inputBuffer);
            if (uniqueUsername) {
                JOptionPane.showMessageDialog(null, "Your username and clientKey were sucessfully stored at the KDC.");
            } else {
                JOptionPane.showMessageDialog(null, "This username is taken.");
                CreateClientFrame createClientFrame = new CreateClientFrame(this);
                createClientFrame.setVisible(true);
            }
            
        } catch (SocketException e) {
            System.out.println("ERROR | Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR | IO: " + e.getMessage());
        } finally {
            if(socket != null)
                socket.close();
        }
    }
    
    public static void main(String[] args) throws IOException {
        
        Client client = new Client();
        CreateClientFrame createClientFrame = new CreateClientFrame(client);
        createClientFrame.setVisible(true);
        //ClientFrame clientFrame = new ClientFrame();
        //client.start();
        
        
        
    }
}
