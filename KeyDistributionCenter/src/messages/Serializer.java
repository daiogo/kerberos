/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Diogo
 */
public class Serializer {
    
    public static byte[] serializeObject(Object object) {
        byte[] serializedObject = null;
        
        try {
            ObjectOutputStream objectOut = null;
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(object);
            serializedObject = byteOut.toByteArray();
            return serializedObject;
        } catch (IOException ex) {
            System.out.println("ERROR | Not able to serialize object of class " + object.getClass().getName());
        }
        return serializedObject;
    }
    
    public static Object deserializeObject(byte[] message) {
        ObjectInputStream objectIn = null;
        Object object = null;
        
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(message);
            objectIn = new ObjectInputStream(byteIn);
            object = objectIn.readObject();
            return object;
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("ERROR | Not able to deserialize object of class " + object.getClass().getName());
        } finally {
            try {
                objectIn.close();
            } catch (IOException ex) {
                System.out.println("ERROR | Not able to deserialize object of class " + object.getClass().getName());
            }
        }
        
        return object;
    }
}
