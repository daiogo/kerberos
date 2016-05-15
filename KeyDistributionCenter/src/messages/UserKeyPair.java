/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import java.io.Serializable;
import javax.crypto.SecretKey;

/**
 *
 * @author Diogo
 */
public class UserKeyPair implements Serializable {
    private String user;
    private SecretKey key;

    public UserKeyPair(String user, SecretKey key) {
        this.user = user;
        this.key = key;
    }

    public String getUser() {
        return user;
    }

    public SecretKey getKey() {
        return key;
    }
}
