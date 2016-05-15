/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import messages.UserKeyPair;
import gui.ClientFrame;
import gui.CreateClientFrame;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private static final int PORT_NUMBER = 6789;
    private DatagramSocket socket;
    private String username;
    private SecretKey key;
    
    public Client() {
        this.socket = null;
    }
    
    @Override
    public void run() {
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
            socket = new DatagramSocket();
            
            username = user;
            SecretKey key = KeyGenerator.getInstance("DES").generateKey();
            
            UserKeyPair newClient = new UserKeyPair(user, key);
            
            System.out.println(kdcIpAddress);
            byte[] outputBuffer = serializeObject(newClient);
            InetAddress host = InetAddress.getByName(kdcIpAddress);
            DatagramPacket request = new DatagramPacket(outputBuffer, outputBuffer.length, host, PORT_NUMBER);
            socket.send(request);
            
            byte[] inputBuffer = new byte[PACKET_SIZE];
            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(response);
            
            boolean uniqueUsername = (boolean) deserializeObject(inputBuffer);

            if (uniqueUsername) {
                JOptionPane.showMessageDialog(null, "Your username and key were sucessfully stored at the KDC.");
            } else {
                JOptionPane.showMessageDialog(null, "This username is taken.");
                CreateClientFrame createClientFrame = new CreateClientFrame(this);
                createClientFrame.setVisible(true);
            }
            
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
    
    public static byte[] serializeObject(Object object) {
        byte[] serializedObject = null;

        try {
            ObjectOutputStream objectOut = null;
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(object);
            serializedObject = byteOut.toByteArray();
            return serializedObject;
        } catch (IOException ex) {
            System.out.println("ERROR | Not able to serialize object of class " + object.getClass().getName());
        }
        return serializedObject;
    }
    
    public static Object deserializeObject(byte[] outputBuffer) {
        ObjectInputStream objectIn = null;
        Object object = null;
        
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(outputBuffer);
            objectIn = new ObjectInputStream(byteIn);
            object = objectIn.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("ERROR | Not able to deserialize object of class " + object.getClass().getName());
        } finally {
            try {
                objectIn.close();
            } catch (IOException ex) {
                System.out.println("ERROR | Not able to deserialize object of class " + object.getClass().getName());
            }
        }
        return object;
    }
    
    public static void main(String[] args) throws IOException {
        
        Client client = new Client();
        CreateClientFrame createClientFrame = new CreateClientFrame(client);
        createClientFrame.setVisible(true);
        //ClientFrame clientFrame = new ClientFrame();
        //client.start();
        
        
        
    }
}
