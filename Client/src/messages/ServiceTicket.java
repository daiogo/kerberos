/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import java.io.Serializable;
import java.util.Date;
import javax.crypto.SecretKey;

/**
 *
 * @author Diogo
 */
public class ServiceTicket implements Serializable {
    private String clientName;
    private Date expirationDate;
    private SecretKey serviceSessionKey;

    public ServiceTicket(String clientName, Date expirationDate, SecretKey serviceSessionKey) {
        this.clientName = clientName;
        this.expirationDate = expirationDate;
        this.serviceSessionKey = serviceSessionKey;
    }

    public String getClientName() {
        return clientName;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public SecretKey getServiceSessionKey() {
        return serviceSessionKey;
    }
    
}
