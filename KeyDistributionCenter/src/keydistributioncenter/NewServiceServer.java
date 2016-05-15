/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import static messages.Serializer.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import messages.NameKeyPair;

/**
 *
 * @author Diogo
 */
public class NewServiceServer extends Thread {
    
    private static final int PACKET_SIZE = 65535;
    private static final int NEW_SERVICE_SERVER_PORT_NUMBER = 8001;
    private DatagramSocket socket;
    private KeyDistributionCenter myKdc;

    public NewServiceServer(KeyDistributionCenter kdc) {
        this.socket = null;
        this.myKdc = kdc;
    }
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(NEW_SERVICE_SERVER_PORT_NUMBER);
            
            byte[] inputBuffer = new byte[PACKET_SIZE];
            byte[] outputBuffer = new byte[PACKET_SIZE];
            
            while (true) {
                DatagramPacket request = new DatagramPacket(inputBuffer, inputBuffer.length);
                socket.receive(request);
                
                Object object = deserializeObject(inputBuffer);
                String objectName = object.getClass().getName();
                boolean uniqueServiceName = true;
                
                if (objectName.equals("messages.NameKeyPair")) {
                    NameKeyPair newService = (NameKeyPair) object;

                    // Checks if service name exists                    
                    for (NameKeyPair pair : myKdc.getServiceKeyPairs()) {                        
                        if (pair.getName().equals(newService.getName())) {
                            uniqueServiceName = false;
                            break;
                        }
                    }
                    
                    // Updates database
                    if (uniqueServiceName) {
                        myKdc.getServiceKeyPairs().add(newService);
                    }
                    
                } else {
                    uniqueServiceName = false;
                }
                
                outputBuffer = serializeObject(uniqueServiceName);

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
