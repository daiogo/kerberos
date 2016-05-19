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
public class ServiceResponse implements Serializable {
    private SealedObject reply;

    public ServiceResponse(SealedObject reply) {
        this.reply = reply;
    }

    public SealedObject getReply() {
        return reply;
    }
}
