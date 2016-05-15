/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

/**
 *
 * @author Diogo
 */
public class ClientFrame {
    
    private File htmlFile;
    private String url;
    
    public ClientFrame() throws IOException {
        this.htmlFile = new File("index.html");
        this.url = htmlFile.toURI().toURL().toString();
        
        JEditorPane editorPane = new JEditorPane(url);
        editorPane.setEditable(false);
        
        JFrame frame = new JFrame();
        frame.getContentPane().add(editorPane, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    
    
}
