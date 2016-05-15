/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keydistributioncenter;

import messages.UserKeyPair;
import gui.KdcGui;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 *
 * @author Diogo
 */
public class KeyDistributionCenter {

    private AuthenticationServer as;
    private TicketGrantingService tgs;
    private ArrayList<UserKeyPair> userKeyPairs;
    private DesCodec desCodec;
    private NewClientServer newClientServer;
    
    public KeyDistributionCenter() {
        this.userKeyPairs = new ArrayList();
        this.as = new AuthenticationServer();
        this.tgs = new TicketGrantingService();
        this.newClientServer = new NewClientServer(this);
        as.start();
        newClientServer.start();
    }

    public AuthenticationServer getAs() {
        return as;
    }

    public TicketGrantingService getTgs() {
        return tgs;
    }

    public ArrayList<UserKeyPair> getUserKeyPairs() {
        return userKeyPairs;
    }

    public DesCodec getDesCodec() {
        return desCodec;
    }

    public static void main(String[] args) throws Exception {
        
        KeyDistributionCenter kdc = new KeyDistributionCenter();
        
        
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter a new username: ");
//        String username = scanner.nextLine();
//        System.out.println("Enter a password: ");
//        String password = scanner.nextLine();
//        
//        SecretKey key = KeyGenerator.getInstance("DES").generateKey();
//        System.out.println("KEY " + key.toString());
//        DesCodec encrypter = new DesCodec(key);
//        String encrypted = encrypter.encode(password);
//        password = null;
//        String decrypted = encrypter.decode(encrypted);
//        System.out.println(encrypted);
//        System.out.println(decrypted);
    }
    
}
