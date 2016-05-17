/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

/**
 *
 * @author http://www.java2s.com/Code/Java/Security/EncryptingaStringwithDES.htm
 */
public class DesCodec {
    
    public static SealedObject encode(byte[] content, SecretKey key) throws Exception {
        Cipher encoder = Cipher.getInstance("DES");
        encoder.init(Cipher.ENCRYPT_MODE, key);

        SealedObject sealedObject = new SealedObject(content, encoder);
        
        return sealedObject;
    }

    public static byte[] decode(SealedObject encryptedObject, SecretKey key) throws Exception {
        Cipher decoder = Cipher.getInstance("DES");
        decoder.init(Cipher.DECRYPT_MODE, key);
        
        return (byte[]) encryptedObject.getObject(decoder);
    }
}
