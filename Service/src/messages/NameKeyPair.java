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
public class NameKeyPair implements Serializable {
    private String name;
    private SecretKey key;

    public NameKeyPair(String name, SecretKey key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public SecretKey getKey() {
        return key;
    }
}
