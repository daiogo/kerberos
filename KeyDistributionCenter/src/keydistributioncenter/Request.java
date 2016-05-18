/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import javax.crypto.SecretKey;
import messages.AuthenticationRequest;
import messages.TicketGrantingTicket;

/**
 *
 * @author Diogo
 */
public class Request {
    private AuthenticationRequest authenticationRequest;
    private TicketGrantingTicket tgt;
    private SecretKey tgsSessionKey;

    public Request(AuthenticationRequest authenticationRequest, TicketGrantingTicket tgt, SecretKey tgsSessionKey) {
        this.authenticationRequest = authenticationRequest;
        this.tgt = tgt;
        this.tgsSessionKey = tgsSessionKey;
    }

    public AuthenticationRequest getAuthenticationRequest() {
        return authenticationRequest;
    }

    public TicketGrantingTicket getTgt() {
        return tgt;
    }

    public SecretKey getTgsSessionKey() {
        return tgsSessionKey;
    }
    
    
    
}
