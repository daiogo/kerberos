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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import messages.*;
import static messages.Serializer.*;
import static keydistributioncenter.DesCodec.*;
/**
 *
 * @author Diogo
 */
public class AuthenticationServer extends Thread {
    
    private static final int PACKET_SIZE = 65535;
    private static final int AS_PORT_NUMBER = 8002;
    private KeyDistributionCenter myKdc;
    private TicketGrantingService myTgs;
    private DatagramSocket socket;
    private DateFormat dateFormat;
    private Calendar calendar;
    
    public AuthenticationServer(TicketGrantingService tgs, KeyDistributionCenter kdc) {
        this.socket = null;
        this.dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        this.calendar = Calendar.getInstance();
        System.out.println("Created AS at " + dateFormat.format(calendar.getTime()));
        this.myTgs = tgs;
        this.myKdc = kdc;
    }
    
    @Override
    public void run() {
        // UDP server code
        try {
            socket = new DatagramSocket(AS_PORT_NUMBER);
            
            byte[] inputBuffer = new byte[PACKET_SIZE];
            byte[] outputBuffer = new byte[PACKET_SIZE];
            
            while(true) {
                DatagramPacket request = new DatagramPacket(inputBuffer, inputBuffer.length);
                socket.receive(request);
                
                // Deserialize and gather information about received packet
                Object object = deserializeObject(request.getData());
                String objectName = object.getClass().getName();

                // Identify AuthenticationRequest
                if (objectName.equals("messages.AuthenticationRequest")) {                
                    AuthenticationRequest authenticationRequest = (AuthenticationRequest) object;
                    String clientName = authenticationRequest.getClientName();
                    SecretKey clientKey = myKdc.findClientKey(clientName);
                    
                    // Create AuthenticationResponse
                    SecretKey tgsSessionKey = KeyGenerator.getInstance("DES").generateKey();
                    TicketGrantingTicket tgt = new TicketGrantingTicket(clientName, tgsSessionKey);
                    
                    AuthenticationResponse authenticationResponse = new AuthenticationResponse(
                            encode(serializeObject(tgsSessionKey), clientKey),
                            encode(serializeObject(new Random().nextInt()), clientKey),
                            encode(serializeObject(tgt), myTgs.getTgsKey()));

                    System.out.println("Session key: " + (SecretKey) deserializeObject(decode(authenticationResponse.getTgsSessionKey(), clientKey)));
                    System.out.println("Random: " + (int) deserializeObject(decode(authenticationResponse.getRandomNumber(), clientKey)));
                    
                    outputBuffer = serializeObject(authenticationResponse);
                    
                    DatagramPacket response = new DatagramPacket(outputBuffer, outputBuffer.length, request.getAddress(), request.getPort());
                    socket.send(response);
                    
                    // Stores data into requests database for future reference by the TGS
                    myKdc.getRequests().add(new Request(authenticationRequest, tgt, tgsSessionKey));
                    
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
