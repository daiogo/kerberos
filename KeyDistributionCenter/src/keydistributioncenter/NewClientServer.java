/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.UserKeyPair;

/**
 *
 * @author Diogo
 */
public class NewClientServer extends Thread {
    
    private static final int PACKET_SIZE = 65535;
    private static final int PORT_NUMBER = 6789;
    private DatagramSocket socket;
    private KeyDistributionCenter myKdc;
    
    public NewClientServer(KeyDistributionCenter kdc) {
        this.socket = null;
        this.myKdc = kdc;
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
    
    public static Object deserializeObject(byte[] message) {
        ObjectInputStream objectIn = null;
        Object object = null;
        
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(message);
            objectIn = new ObjectInputStream(byteIn);
            object = objectIn.readObject();
            return object;
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
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(PORT_NUMBER);
            
            byte[] inputBuffer = new byte[PACKET_SIZE];
            byte[] outputBuffer = new byte[PACKET_SIZE];
            
            while (true) {
                DatagramPacket request = new DatagramPacket(inputBuffer, inputBuffer.length);
                socket.receive(request);
                
                Object object = deserializeObject(inputBuffer);
                String objectName = object.getClass().getName();
                boolean uniqueUsername = true;
                
                if (objectName.equals("messages.UserKeyPair")) {
                    UserKeyPair newClient = (UserKeyPair) object;

                    // Checks if username exists                    
                    for (UserKeyPair pair : myKdc.getUserKeyPairs()) {
                        System.out.println("User: " + pair.getUser());
                        System.out.println("Key: " + pair.getKey().toString());
                        if (pair.getUser().equals(newClient.getUser())) {
                            uniqueUsername = false;
                            break;
                        }
                    }
                    
                    // Updates database
                    if (uniqueUsername) {
                        System.out.println("Database updated");
                        myKdc.getUserKeyPairs().add(newClient);
                    }
                    
                } else {
                    uniqueUsername = false;
                }
                
                outputBuffer = serializeObject(uniqueUsername);

                DatagramPacket response = new DatagramPacket(outputBuffer, outputBuffer.length, request.getAddress(), request.getPort());
                socket.send(response);
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
    
    
}
