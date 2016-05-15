/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import gui.ClientFrame;
import static messages.Serializer.*;
import messages.NameKeyPair;
import gui.CreateClientFrame;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private static final int AUTHENTICATION_SERVER_PORT_NUMBER = 8002;
    private DatagramSocket socket;
    private String username;
    private String kdcIpAddress;
    private SecretKey clientKey;
    private boolean uniqueUsername;
    private DateFormat dateFormat;
    private Calendar calendar;
    
    public Client() {
        this.socket = null;
        this.dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        this.calendar = Calendar.getInstance();
        System.out.println("Created client at " + dateFormat.format(calendar.getTime()));
    }
    
    @Override
    public void run() {
        // UDP client code
        try {
            String outputBuffer = "MEH";
            byte [] m = outputBuffer.getBytes();
            
            socket = new DatagramSocket();
            InetAddress host = InetAddress.getByName(kdcIpAddress);
            DatagramPacket request = new DatagramPacket(m, outputBuffer.length(), host, AUTHENTICATION_SERVER_PORT_NUMBER);
            socket.send(request);
            
            byte[] inputBuffer = new byte[PACKET_SIZE];
            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(response);
            
            System.out.println("Reply: " + new String(response.getData()));
        } catch (SocketException e) {
            System.out.println("ERROR | Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR | IO: " + e.getMessage());
        } finally {
            if(socket != null)
                socket.close();
        }
    }
    
    public void createClient(String username, String kdcIpAddress) throws NoSuchAlgorithmException {
        try {
            // Creating user and clientKey pair
            this.username = username;
            this.kdcIpAddress = kdcIpAddress;
            this.clientKey = KeyGenerator.getInstance("DES").generateKey();
            NameKeyPair newClient = new NameKeyPair(username, clientKey);
            
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
            uniqueUsername = (boolean) deserializeObject(inputBuffer);
            if (uniqueUsername) {
                // Show confirmation dialog
                JOptionPane.showMessageDialog(null, "Your username and clientKey were sucessfully stored at the KDC.");
                
                // Starts AS server and the Kerberos protocol itself
                this.start();
                ClientFrame clientFrame = new ClientFrame();
                
            } else {
                // Show error dialog
                JOptionPane.showMessageDialog(null, "This username is taken.");
                
                // Gives new chance for user to choose an username
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

    public boolean isUniqueUsername() {
        return uniqueUsername;
    }
    
    public static void main(String[] args) throws IOException {
        
        Client client = new Client();
        CreateClientFrame createClientFrame = new CreateClientFrame(client);
        createClientFrame.setVisible(true);
    }
}
