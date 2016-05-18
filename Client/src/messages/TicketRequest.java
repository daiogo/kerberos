/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import java.io.Serializable;
import java.util.Random;
import javax.crypto.SealedObject;
 /*
 * @author Diogo
 */
public class TicketRequest implements Serializable {
    private SealedObject clientName;
    private SealedObject timestamp;
    private SealedObject tgt;
    private String serviceName;
    private int tgsRandomNumber;

    public TicketRequest(SealedObject clientName, SealedObject timestamp, SealedObject tgt, String serviceName) throws Exception {
        this.clientName = clientName;
        this.timestamp = timestamp;
        this.tgt = tgt;
        this.serviceName = serviceName;
        this.tgsRandomNumber = new Random().nextInt();
    }

    public SealedObject getClientName() {
        return clientName;
    }

    public SealedObject getTimestamp() {
        return timestamp;
    }

    public SealedObject getTgt() {
        return tgt;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getTgsRandomNumber() {
        return tgsRandomNumber;
    }
}
