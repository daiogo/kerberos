/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author Diogo
 */
public class AuthenticationRequest implements Serializable {
    private String clientName;          // Identifies client who created the request
    private String serviceName;         // Identifies service the client wants to connect to
    private DateFormat dateFormat;      // Allows dates to be printable as strings
    private Calendar calendar;          // Gets current date and time
    private Date expirationDate;        // Holds the expiration date value
    private int asRandomNumber;           // Random number used as a challenge–response authentication

    public AuthenticationRequest(String clientName, String serviceName) {
        this.clientName = clientName;
        this.serviceName = serviceName;
        this.dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        this.calendar = Calendar.getInstance();
        this.calendar.add(Calendar.MINUTE, 2);          // Makes expiration date 2 minutes from now
        this.expirationDate = calendar.getTime();
        this.asRandomNumber = new Random().nextInt();
    }

    public String getClientName() {
        return clientName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public int getAsRandomNumber() {
        return asRandomNumber;
    }
    
    
}
