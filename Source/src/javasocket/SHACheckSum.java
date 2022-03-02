/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javasocket;

/**
 *
 * @author namhh
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
               
public class SHACheckSum {
    
    public String SHACheckSum(String file) throws IOException, NoSuchAlgorithmException{
        FileInputStream fis = null;
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            fis = new FileInputStream(file);
            byte[] dataBytes = new byte[1024];
            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }   ;
            byte[] mdbytes = md.digest();
            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }   
            
            //System.out.println("Hex format : " + sb.toString());
            
            hash = sb.toString();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SHACheckSum.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(SHACheckSum.class.getName()).log(Level.SEVERE, null, ex);
            }
        }       
        return hash;
    }
        
    
}
