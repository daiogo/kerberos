/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author Diogo
 */
public class AuthenticationServer extends Thread {
    
    private static final int PACKET_SIZE = 65535;
    private static final int PORT_NUMBER = 6790;
    private DatagramSocket socket;
    
    public AuthenticationServer() {
        this.socket = null;
    }
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(PORT_NUMBER);
            
            byte[] buffer = new byte[PACKET_SIZE];
            
            while(true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
                socket.send(reply);
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
