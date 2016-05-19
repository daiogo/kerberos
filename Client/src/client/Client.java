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
import static client.DesCodec.*;

/**
 *
 * @author Diogo
 */
public class Client extends Thread {

    private static final int PACKET_SIZE = 65535;
    private static final int NEW_CLIENT_SERVER_PORT_NUMBER = 8000;
    private static final int AS_PORT_NUMBER = 8002;
    private static final int TGS_PORT_NUMBER = 8003;
    private static final int SERVICE_PORT_NUMBER = 8004;
    private DatagramSocket socket;
    private String clientName;
    private String kdcIpAddress;
    private InetAddress host;
    private SecretKey clientKey;
    private boolean uniqueClientName;
    private Calendar calendar;
    private ArrayList<AuthenticationRequest> requests;
    private SecretKey tgsSessionKey;
    private SecretKey serviceSessionKey;
    private int asRandomNumber;
    private int tgsRandomNumber;
    
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
                this.tgsSessionKey = (SecretKey) deserializeObject(decode(authenticationResponse.getTgsSessionKey(), clientKey));
                this.asRandomNumber = (int) deserializeObject(decode(authenticationResponse.getRandomNumber(), clientKey));
                SealedObject encryptedClientName = encode(serializeObject(clientName), tgsSessionKey);
                this.calendar = Calendar.getInstance();
                SealedObject encryptedTimestamp = encode(serializeObject(this.calendar.getTime()), tgsSessionKey);
                SealedObject encryptedTgt = authenticationResponse.getTgt();
                String serviceName = findServiceName(this.asRandomNumber);
                
                TicketRequest ticketRequest = new TicketRequest(encryptedClientName, encryptedTimestamp, encryptedTgt, serviceName);

                // Calls requestTicket() method to initiate connection with TGS
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
            if (objectName.equals("messages.TicketResponse")) { //Check for expiration date!
                TicketResponse ticketResponse = (TicketResponse) object;
                this.serviceSessionKey = (SecretKey) deserializeObject(decode(ticketResponse.getServiceSessionKey(), this.tgsSessionKey));
                this.tgsRandomNumber = (int) deserializeObject(decode(ticketResponse.getTgsRandomNumber(), this.tgsSessionKey));
                SealedObject serviceTicket = ticketResponse.getServiceTicket();

                this.calendar = Calendar.getInstance();
                ServiceRequest serviceRequest = new ServiceRequest(
                        encode(serializeObject(clientName), serviceSessionKey),
                        encode(serializeObject(calendar.getTime()), serviceSessionKey),
                        serviceTicket,
                        "GET");
                
                // Calls requestService() method to initiate connection with service
                this.requestService(serviceRequest);
            } else {
                String errorMessage = (String) object;
                System.out.println(errorMessage);
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
    
    public void requestService(ServiceRequest serviceRequest) {
        // UDP client code
        try {
            // Transforms serviceRequest into byte array
            byte [] outputBuffer = serializeObject(serviceRequest);
            
            // Creates socket and sends message to the service
            DatagramPacket request = new DatagramPacket(outputBuffer, outputBuffer.length, host, SERVICE_PORT_NUMBER);
            socket.send(request);

            // Waits response from the service (Blocking code!)
            byte[] inputBuffer = new byte[PACKET_SIZE];
            DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(response);
            
            // Deserialize and gather information about received packet
            Object object = deserializeObject(response.getData());
            String objectName = object.getClass().getName();
            
            // Handles service response
            if (objectName.equals("messages.ServiceResponse")) { //Check for expiration date!
                ServiceResponse serviceResponse = (ServiceResponse) object;
                String reply = (String) deserializeObject(decode(serviceResponse.getReply(), this.serviceSessionKey));
                System.out.println("-----------------------------");
                System.out.println("Server responded: " + reply);
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
            uniqueClientName = (boolean) deserializeObject(inputBuffer);
            if (uniqueClientName) {
                // Show confirmation dialog
                JOptionPane.showMessageDialog(null, "Your username and clientKey were sucessfully stored at the KDC.");
                
                // Starts authentication and the Kerberos protocol itself
                this.authenticate();
                //ClientFrame clientFrame = new ClientFrame();
                
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
        return uniqueClientName;
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
