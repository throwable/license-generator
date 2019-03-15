package es.minsait.tm.license.gen;

import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;

public class KeyGen implements Runnable {
    @CommandLine.Option(names = {"-pub"}, required = true, description = "Public key file name")
    private String publicKeyFile;

    @CommandLine.Option(names = {"-pri"}, required = true, description = "Private key file name")
    private String privateKeyFile;


    private KeyGen() {}

    public KeyGen(String publicKeyFile, String privateKeyFile) {
        this.publicKeyFile = publicKeyFile;
        this.privateKeyFile = privateKeyFile;
    }

    @Override
    public void run() {
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        kpg.initialize(1024);
        KeyPair keyPair = kpg.genKeyPair();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                keyPair.getPublic().getEncoded());
        byte[] pub = x509EncodedKeySpec.getEncoded();

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyPair.getPrivate().getEncoded());

        byte[] priv = pkcs8EncodedKeySpec.getEncoded();
        try {
            Files.write(Paths.get(publicKeyFile), Collections.singletonList(Base64.getEncoder().encodeToString(pub)));
            Files.write(Paths.get(privateKeyFile), Collections.singletonList(Base64.getEncoder().encodeToString(priv)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Keys generated");
    }


    public static void main(String[] args) {
        CommandLine.run(new KeyGen(), args);
    }
}
