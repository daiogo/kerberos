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
public class TicketResponse implements Serializable {
    private SealedObject serviceSessionKey;
    private SealedObject tgsRandomNumber;
    private SealedObject serviceTicket;

    public TicketResponse(SealedObject serviceSessionKey, SealedObject tgsRandomNumber, SealedObject serviceTicket) {
        this.serviceSessionKey = serviceSessionKey;
        this.tgsRandomNumber = tgsRandomNumber;
        this.serviceTicket = serviceTicket;
    }

    public SealedObject getServiceSessionKey() {
        return serviceSessionKey;
    }

    public SealedObject getTgsRandomNumber() {
        return tgsRandomNumber;
    }

    public SealedObject getServiceTicket() {
        return serviceTicket;
    }
    
    
}
