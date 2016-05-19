/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import java.security.NoSuchAlgorithmException;
import messages.NameKeyPair;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import messages.TicketGrantingTicket;

/**
 *
 * @author Diogo
 */
public class KeyDistributionCenter {

    private AuthenticationServer as;
    private TicketGrantingService tgs;
    private ArrayList<NameKeyPair> clientKeyPairs;
    private ArrayList<NameKeyPair> serviceKeyPairs;
    private ArrayList<Request> requests;
    private NewClientServer newClientServer;
    private NewServiceServer newServiceServer;
    
    public KeyDistributionCenter() throws NoSuchAlgorithmException {
        this.clientKeyPairs = new ArrayList();
        this.serviceKeyPairs = new ArrayList();
        this.requests = new ArrayList();
        this.tgs = new TicketGrantingService(this);
        this.as = new AuthenticationServer(tgs, this);
        this.newClientServer = new NewClientServer(this);
        this.newServiceServer = new NewServiceServer(this);
        as.start();
        tgs.start();
        newClientServer.start();
        newServiceServer.start();
    }

    public AuthenticationServer getAs() {
        return as;
    }

    public TicketGrantingService getTgs() {
        return tgs;
    }

    public ArrayList<NameKeyPair> getClientKeyPairs() {
        return clientKeyPairs;
    }

    public ArrayList<NameKeyPair> getServiceKeyPairs() {
        return serviceKeyPairs;
    }

    public ArrayList<Request> getRequests() {
        return requests;
    }
    
    public SecretKey findClientKey(String clientName) {
        for (NameKeyPair pair : clientKeyPairs) {
            if (pair.getName().equals(clientName))
                return pair.getKey();
        }
        return null;
    }
    
    public SecretKey findServiceKey(String serviceName) {
        for (NameKeyPair pair : serviceKeyPairs) {
            if (pair.getName().equals(serviceName))
                return pair.getKey();
        }
        return null;
    }

    public String findClientName(TicketGrantingTicket tgt, SecretKey tgsSessionKey) {
        for (Request request : requests) {
            if (request.getTgt().equals(tgt) && request.getTgsSessionKey().equals(tgsSessionKey)) {
                return request.getAuthenticationRequest().getClientName();
            }
        }
        return null;
    }
    
    public static void main(String[] args) throws Exception {
        
        KeyDistributionCenter kdc = new KeyDistributionCenter();
        
    }
    
}
