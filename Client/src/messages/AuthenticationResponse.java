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
public class AuthenticationResponse implements Serializable {
    private SealedObject tgsSessionKey;
    private SealedObject randomNumber;
    private SealedObject tgt;

    public AuthenticationResponse(SealedObject tgsSessionKey, SealedObject randomNumber, SealedObject tgt) {
        this.tgsSessionKey = tgsSessionKey;
        this.randomNumber = randomNumber;
        this.tgt = tgt;
    }

    public SealedObject getTgsSessionKey() {
        return tgsSessionKey;
    }

    public SealedObject getRandomNumber() {
        return randomNumber;
    }

    public SealedObject getTgt() {
        return tgt;
    }
    

}
