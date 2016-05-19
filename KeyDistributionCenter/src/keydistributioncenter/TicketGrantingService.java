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
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import static keydistributioncenter.DesCodec.*;
import messages.*;
import static messages.Serializer.*;

/**
 *
 * @author Diogo
 */
public class TicketGrantingService extends Thread {
    private static final int PACKET_SIZE = 65535;
    private static final int TGS_PORT_NUMBER = 8003;
    private KeyDistributionCenter myKdc;
    private AuthenticationServer myAs;
    private DatagramSocket socket;
    private Calendar calendar;
    private SecretKey tgsKey;
    
    public TicketGrantingService() throws NoSuchAlgorithmException {
        this.tgsKey = KeyGenerator.getInstance("DES").generateKey();
        this.calendar = Calendar.getInstance();
    }
    
    public SecretKey getTgsKey() {
        return tgsKey;
    }
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(TGS_PORT_NUMBER);
            
            byte[] inputBuffer = new byte[PACKET_SIZE];
            byte[] outputBuffer = new byte[PACKET_SIZE];
            
            while(true) {
                DatagramPacket request = new DatagramPacket(inputBuffer, inputBuffer.length);
                socket.receive(request);
                
                // Deserialize and gather information about received packet
                Object object = deserializeObject(request.getData());
                String objectName = object.getClass().getName();

                // Identify TicketRequest
                if (objectName.equals("messages.TicketRequest")) { //Verify expiration too!                
                    TicketRequest ticketRequest = (TicketRequest) object;
                    
                    // Gathers data to build ticket response
                    TicketGrantingTicket tgt = (TicketGrantingTicket) deserializeObject(decode(ticketRequest.getTgt(), tgsKey));
                    SecretKey tgsSessionKey = (SecretKey) tgt.getTgsSessionKey();
                    SecretKey serviceSessionKey = KeyGenerator.getInstance("DES").generateKey();
                    int tgsRandomNumber = ticketRequest.getTgsRandomNumber();
                    SecretKey serviceKey = myKdc.findClientKey(ticketRequest.getServiceName());
                    Date expirationDate = tgt.getExpirationDate();
                    String clientName = myKdc.findClientName(tgt, tgsSessionKey);
                    
                    ServiceTicket serviceTicket = new ServiceTicket(clientName, expirationDate, serviceSessionKey);
                    TicketResponse ticketResponse = new TicketResponse(
                            encode(serializeObject(serviceSessionKey), tgsSessionKey), 
                            encode(serializeObject(tgsRandomNumber), tgsSessionKey),
                            encode(serializeObject(serviceTicket), serviceKey));
                    
                    outputBuffer = serializeObject(ticketResponse);
                    
                    DatagramPacket response = new DatagramPacket(outputBuffer, outputBuffer.length, request.getAddress(), request.getPort());
                    socket.send(response);
                    
                    // Stores data into requests database for future reference by the TGS
                    //myKdc.getRequests().add(new Request(authenticationRequest, tgt, tgsSessionKey));
                    
                }
                
            }
        } catch (SocketException e) {
            System.out.println("ERROR | Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR | IO: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("ERROR | Encryption: " + e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(AuthenticationServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(socket != null)
                socket.close();
        }
    }

}
