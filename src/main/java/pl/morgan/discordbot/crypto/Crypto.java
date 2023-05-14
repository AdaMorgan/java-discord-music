package pl.morgan.discordbot.crypto;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Crypto {
    private final String inputFile = "config.toml";
    private final String outputFile = "config.enc";
    private final String keyFile = "config.key";

    public void ENCRYPT() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        FileOutputStream keyOut = new FileOutputStream(keyFile);
        keyOut.write(secretKey.getEncoded());
        keyOut.close();

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        FileInputStream in = new FileInputStream(inputFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        CipherOutputStream cipherOut = new CipherOutputStream(out, cipher);
        byte[] buffer = new byte[8192];
        int count;
        while ((count = in.read(buffer)) > 0) {
            cipherOut.write(buffer, 0, count);
        }
        in.close();
        cipherOut.close();
    }

    public void DECRYPT() throws Exception {
        FileInputStream keyIn = new FileInputStream(keyFile);
        byte[] keyBytes = new byte[keyIn.available()];
        keyIn.read(keyBytes);
        keyIn.close();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        FileInputStream in = new FileInputStream(outputFile);
        CipherInputStream cipherIn = new CipherInputStream(in, cipher);
        FileOutputStream out = new FileOutputStream(inputFile);
        byte[] buffer = new byte[8192];
        int count;
        while ((count = cipherIn.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        cipherIn.close();
        out.close();
    }

    public static void main(String[] args) throws Exception {
        Crypto crypto = new Crypto();
        crypto.ENCRYPT();
        crypto.DECRYPT();
    }
}
