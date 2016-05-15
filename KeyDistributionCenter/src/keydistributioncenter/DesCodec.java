/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 *
 * @author http://www.java2s.com/Code/Java/Security/EncryptingaStringwithDES.htm
 */
public class DesCodec {
    
    Cipher encoder;
    Cipher decoder;

    DesCodec(SecretKey key) throws Exception {
        encoder = Cipher.getInstance("DES");
        decoder = Cipher.getInstance("DES");
        encoder.init(Cipher.ENCRYPT_MODE, key);
        decoder.init(Cipher.DECRYPT_MODE, key);
    }

    public String encode(String str) throws Exception {
        // Encode the string into bytes using utf-8
        byte[] utf8 = str.getBytes("UTF8");

        // Encrypt
        byte[] enc = encoder.doFinal(utf8);

        // Encode bytes to base64 to get a string
        return new sun.misc.BASE64Encoder().encode(enc);
    }

    public String decode(String str) throws Exception {
        // Decode base64 to get bytes
        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);

        byte[] utf8 = decoder.doFinal(dec);

        // Decode using utf-8
        return new String(utf8, "UTF8");
    }
}
