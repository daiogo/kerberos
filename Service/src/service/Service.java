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
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;
import static service.DesCodec.*;
import messages.NameKeyPair;
import messages.ServiceRequest;
import messages.ServiceResponse;
import messages.ServiceTicket;

/**
 *
 * @author Diogo
 */
public class Service extends Thread {

    private static final int PACKET_SIZE = 65535;
    private static final int NEW_SERVICE_SERVER_PORT_NUMBER = 8001;
    private static final int SERVICE_PORT_NUMBER = 8004;
    private DatagramSocket socket;
    private String serviceName;
    private SecretKey serviceKey;
    private SecretKey serviceSessionKey;
    private Calendar calendar;
    private Date currentDate;
    private String reply;
    private ServiceResponse serviceResponse;
    
    
    public Service() {
        this.socket = null;
        this.calendar = Calendar.getInstance();
    }
    
    @Override
    public void run() {
        // UDP server code
        try {
            socket = new DatagramSocket(SERVICE_PORT_NUMBER);
            
            byte[] inputBuffer = new byte[PACKET_SIZE];
            byte[] outputBuffer = new byte[PACKET_SIZE];
            
            while (true) {
                DatagramPacket request = new DatagramPacket(inputBuffer, inputBuffer.length);
                socket.receive(request);
                
                Object object = deserializeObject(inputBuffer);
                String objectName = object.getClass().getName();
                
                if (objectName.equals("messages.ServiceRequest")) {
                    // Unpacks data from message
                    ServiceRequest serviceRequest = (ServiceRequest) object;
                    ServiceTicket serviceTicket = (ServiceTicket) deserializeObject(decode(serviceRequest.getServiceTicket(), serviceKey));
                    this.serviceSessionKey = serviceTicket.getServiceSessionKey();
                    String resourceRequest = serviceRequest.getResourceRequest();
                    String requester = (String) deserializeObject(decode(serviceRequest.getClientName(), serviceSessionKey));
                    String clientName = serviceTicket.getClientName();
                    
                    this.currentDate = calendar.getTime();
                    Date ticketExpirationDate = serviceTicket.getExpirationDate();
                    
                    if (requester.equals(clientName) && currentDate.before(ticketExpirationDate)) {
                        switch (resourceRequest.toLowerCase()) {
                            case "get":
                                this.reply = "You asked for GET!";
                                break;
                            case "post":
                                this.reply = "You asked for POST!";
                                break;
                            default:
                                this.reply = "ERROR | Unknown request!";
                        }

                        this.serviceResponse = new ServiceResponse(
                                encode(serializeObject(reply), serviceSessionKey));

                        outputBuffer = serializeObject(serviceResponse);
                    } else {
                        this.reply = "ERROR | Wrong client or ticket expired.";
                        this.serviceResponse = new ServiceResponse(
                                encode(serializeObject(reply), serviceSessionKey));

                        outputBuffer = serializeObject(serviceResponse);
                    }
                    
                    DatagramPacket response = new DatagramPacket(outputBuffer, outputBuffer.length, request.getAddress(), request.getPort());
                    socket.send(response);
                } else {
                    System.out.println("ERROR | Unknown message received");
                }
            }
        } catch (SocketException e) {
            System.out.println("ERROR | Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR | IO: " + e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(socket != null)
                socket.close();
        }
    }
    
    public void createService(String serviceName, String kdcIpAddress) throws NoSuchAlgorithmException {
        try {
            // Creating service and serviceKey pair
            this.serviceName = serviceName;
            this.serviceKey = KeyGenerator.getInstance("DES").generateKey();
            NameKeyPair newService = new NameKeyPair(this.serviceName, this.serviceKey);
            
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
                // Show confirmation message
                JOptionPane.showMessageDialog(null, "Your service name and key were sucessfully stored at the KDC.");
                
                // Starts server
                this.start();
            } else {
                JOptionPane.showMessageDialog(null, "This service name is taken.");
                CreateServiceFrame createServiceFrame = new CreateServiceFrame(this);
                createServiceFrame.setVisible(true);
                createServiceFrame.setLocationRelativeTo(null);
            }
            
        } catch (SocketException e) {
            System.out.println("ERROR | Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR | IO: " + e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(socket != null)
                socket.close();
        }
    }
    
    public static void main(String[] args) throws IOException {
        
        Service service = new Service();
        CreateServiceFrame createServiceFrame = new CreateServiceFrame(service);
        createServiceFrame.setVisible(true);
        createServiceFrame.setLocationRelativeTo(null);
    }

}

