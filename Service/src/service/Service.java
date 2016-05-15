/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

/**
 *
 * @author Diogo
 */
import gui.CreateServiceFrame;
import static messages.Serializer.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;
import messages.NameKeyPair;

/**
 *
 * @author Diogo
 */
public class Service extends Thread {

    private static final int PACKET_SIZE = 65535;
    private static final int NEW_SERVICE_SERVER_PORT_NUMBER = 8001;
    private DatagramSocket socket;
    private String serviceName;
    private SecretKey serviceKey;
    
    public Service() {
        this.socket = null;
    }
    
    @Override
    public void run() {
        // UDP server code
    }
    
    public void createService(String serviceName, String kdcIpAddress) throws NoSuchAlgorithmException {
        try {
            // Creating service and serviceKey pair
            this.serviceName = serviceName;
            this.serviceKey = KeyGenerator.getInstance("DES").generateKey();
            NameKeyPair newService = new NameKeyPair(serviceName, serviceKey);
            
            // Starting socket and sending request to create service
            socket = new DatagramSocket();
            byte[] outputBuffer = serializeObject(newService);
            InetAddress host = InetAddress.getByName(kdcIpAddress);
            DatagramPacket request = new DatagramPacket(outputBuffer, outputBuffer.length, host, NEW_SERVICE_SERVER_PORT_NUMBER);
            socket.send(request);
            
            // Prepares and waits for the KDC response
            // Not scalable! I chose not to create handlers in separate threads
            // given that this is being developed only for educational purposes
            byte[] inputBuffer = new byte[PACKET_SIZE];
            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(response);
            
            // Determines whether the serviceName was already taken on the KDC 
            boolean uniqueServiceName = (boolean) deserializeObject(inputBuffer);
            if (uniqueServiceName) {
                JOptionPane.showMessageDialog(null, "Your service name and key were sucessfully stored at the KDC.");
            } else {
                JOptionPane.showMessageDialog(null, "This service name is taken.");
                CreateServiceFrame createServiceFrame = new CreateServiceFrame(this);
                createServiceFrame.setVisible(true);
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
        
        Service service = new Service();
        CreateServiceFrame createServiceFrame = new CreateServiceFrame(service);
        createServiceFrame.setVisible(true);
    }

}

