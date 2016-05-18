/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import gui.ClientFrame;
import static messages.Serializer.*;
import messages.*;
import gui.CreateClientFrame;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;
import static keydistributioncenter.DesCodec.*;

/**
 *
 * @author Diogo
 */
public class Client extends Thread {

    private static final int PACKET_SIZE = 65535;
    private static final int NEW_CLIENT_SERVER_PORT_NUMBER = 8000;
    private static final int AS_PORT_NUMBER = 8002;
    private static final int TGS_PORT_NUMBER = 8003;
    private DatagramSocket socket;
    private String clientName;
    private String kdcIpAddress;
    private InetAddress host;
    private SecretKey clientKey;
    private boolean uniqueUsername;
    private Calendar calendar;
    private ArrayList<AuthenticationRequest> requests;
    
    public Client() {
        this.socket = null;
        this.calendar = Calendar.getInstance();
        this.requests = new ArrayList();
    }
    
    public void authenticate() {
        // Gets the service the client wants to connect with
        String desiredService = (String) JOptionPane.showInputDialog("Enter the name of the service you want to connect with");
        
        // UDP client code
        try {
            // Creates AuthenticationRequest message
            AuthenticationRequest authenticationRequest = new AuthenticationRequest(clientName, desiredService);
            requests.add(authenticationRequest);
            byte [] outputBuffer = serializeObject(authenticationRequest);
            
            // Creates socket and sends message to the KDC
            DatagramPacket request = new DatagramPacket(outputBuffer, outputBuffer.length, host, AS_PORT_NUMBER);
            socket.send(request);
            
            // Waits response from the AS (Blocking code!)
            byte[] inputBuffer = new byte[PACKET_SIZE];
            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(response);
            
            // Deserialize and gather information about received packet
            Object object = deserializeObject(response.getData());
            String objectName = object.getClass().getName();
            
            // Handles AS response
            if (objectName.equals("messages.AuthenticationResponse")) {
                AuthenticationResponse authenticationResponse = (AuthenticationResponse) object;
                SecretKey tgsSessionKey = (SecretKey) deserializeObject(decode(authenticationResponse.getTgsSessionKey(), clientKey));
                int clientRandomNumber = (int) deserializeObject(decode(authenticationResponse.getRandomNumber(), clientKey));
                SealedObject encryptedClientName = encode(serializeObject(clientName), tgsSessionKey);
                this.calendar = Calendar.getInstance();
                SealedObject encryptedTimestamp = encode(serializeObject(this.calendar.getTime()), tgsSessionKey);
                SealedObject encryptedTgt = authenticationResponse.getTgt();
                String serviceName = findServiceName(clientRandomNumber);
                
                TicketRequest ticketRequest = new TicketRequest(encryptedClientName, encryptedTimestamp, encryptedTgt, serviceName);
                this.requestTicket(ticketRequest);
            }

        } catch (SocketException e) {
            System.out.println("ERROR | Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR | IO: " + e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(socket != null)
                socket.close();
        }
    }
    
    public void requestTicket(TicketRequest ticketRequest) {
        // UDP client code
        try {
            // Transforms ticketRequest into byte array
            byte [] outputBuffer = serializeObject(ticketRequest);
            
            // Creates socket and sends message to the KDC
            DatagramPacket request = new DatagramPacket(outputBuffer, outputBuffer.length, host, TGS_PORT_NUMBER);
            socket.send(request);

            // Waits response from the TGS (Blocking code!)
            byte[] inputBuffer = new byte[PACKET_SIZE];
            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(response);
            
            // Deserialize and gather information about received packet
            Object object = deserializeObject(response.getData());
            String objectName = object.getClass().getName();
            
            // Handles TGS response
            if (objectName.equals("messages.AuthenticationResponse")) {
                
            }

        } catch (SocketException e) {
            System.out.println("ERROR | Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR | IO: " + e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(socket != null)
                socket.close();
        }
    }
    
    public void createClient(String clientName, String kdcIpAddress) throws NoSuchAlgorithmException {
        try {
            // Creating user and clientKey pair
            this.clientName = clientName;
            this.kdcIpAddress = kdcIpAddress;
            this.host = InetAddress.getByName(this.kdcIpAddress);
            this.clientKey = KeyGenerator.getInstance("DES").generateKey();
            NameKeyPair newClient = new NameKeyPair(this.clientName, clientKey);
            
            // Starting socket and sending request to create user
            socket = new DatagramSocket();
            byte[] outputBuffer = serializeObject(newClient);
            DatagramPacket request = new DatagramPacket(outputBuffer, outputBuffer.length, host, NEW_CLIENT_SERVER_PORT_NUMBER);
            socket.send(request);
            
            // Prepares and waits for the KDC response
            // Not scalable! I chose not to create handlers in separate threads
            // given that this is being developed only for educational purposes
            byte[] inputBuffer = new byte[PACKET_SIZE];
            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(response);
            
            // Determines whether the clientName was already taken on the KDC 
            uniqueUsername = (boolean) deserializeObject(inputBuffer);
            if (uniqueUsername) {
                // Show confirmation dialog
                JOptionPane.showMessageDialog(null, "Your username and clientKey were sucessfully stored at the KDC.");
                
                // Starts AS server and the Kerberos protocol itself
                this.authenticate();
                ClientFrame clientFrame = new ClientFrame();
                
            } else {
                // Show error dialog
                JOptionPane.showMessageDialog(null, "This username is taken.");
                
                // Gives new chance for user to choose an clientName
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
    
    public String findServiceName(int randomNumber) {
        for (AuthenticationRequest request: requests) {
            if (request.getAsRandomNumber() == randomNumber)
                return request.getServiceName();
        }
        return null;
    }
    
    public static void main(String[] args) throws IOException {
        // Creates client and calls for the form to create a new client
        Client client = new Client();
        CreateClientFrame createClientFrame = new CreateClientFrame(client);
        createClientFrame.setVisible(true);
    }
}
