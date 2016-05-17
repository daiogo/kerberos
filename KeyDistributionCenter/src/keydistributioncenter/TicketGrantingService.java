/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 *
 * @author Diogo
 */
public class TicketGrantingService {
    private SecretKey tgsKey;
    private SecretKey tgsSessionKey;
    
    public TicketGrantingService() throws NoSuchAlgorithmException {
        this.tgsSessionKey = KeyGenerator.getInstance("DES").generateKey();
    }
    public SecretKey getTgsKey() {
        return tgsKey;
    }

    public SecretKey getTgsSessionKey() {
        return tgsSessionKey;
    }
    
}
