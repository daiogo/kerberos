/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import java.io.Serializable;
import javax.crypto.SealedObject;

/**
 *
 * @author Diogo
 */
public class ServiceRequest implements Serializable {
    private SealedObject clientName;
    private SealedObject timestamp;
    private SealedObject serviceTicket;
    private String resourceRequest;

    public ServiceRequest(SealedObject clientName, SealedObject timestamp, SealedObject serviceTicket, String resourceRequest) {
        this.clientName = clientName;
        this.timestamp = timestamp;
        this.serviceTicket = serviceTicket;
        this.resourceRequest = resourceRequest;
    }

    public SealedObject getClientName() {
        return clientName;
    }

    public SealedObject getTimestamp() {
        return timestamp;
    }

    public SealedObject getServiceTicket() {
        return serviceTicket;
    }

    public String getResourceRequest() {
        return resourceRequest;
    }
    
    
}
