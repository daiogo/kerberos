/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.SecretKey;

/**
 *
 * @author Diogo
 */
public class TicketGrantingTicket implements Serializable {
    private String clientName;
    private Calendar calendar;          // Gets current date and time
    private Date expirationDate;        // Holds the expiration date value
    private SecretKey tgsSessionKey;
    
    public TicketGrantingTicket(String clientName, SecretKey tgsSessionKey) throws NoSuchAlgorithmException {
        this.clientName = clientName;
        this.calendar = Calendar.getInstance();
        this.calendar.add(Calendar.SECOND, 30);      // Makes expiration date 30 seconds from now
        this.expirationDate = calendar.getTime();
        this.tgsSessionKey = tgsSessionKey;
    }
}
