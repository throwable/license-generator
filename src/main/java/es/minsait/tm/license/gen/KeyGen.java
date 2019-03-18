package es.minsait.tm.license.gen;

import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Option;

@Command(name = "keygen", description = "Generate public/private key pair")
public class KeyGen implements Runnable {
    @Option(names = {"-pub"}, description = "Public key file name (default: ${DEFAULT-VALUE})")
    private String publicKeyFileName = "public.key";

    @Option(names = {"-pri"}, description = "Private key file name (default: ${DEFAULT-VALUE})")
    private String privateKeyFileName = "private.key";

    @Option(names = {"-o"}, description = "Output directory for generated license.key (default: ${DEFAULT-VALUE})")
    private Path outputDir = Paths.get(System.getProperty("user.dir"));

    public KeyGen() {}

    public KeyGen(String publicKeyFileName, String privateKeyFileName, Path outputDir) {
        this.publicKeyFileName = publicKeyFileName;
        this.privateKeyFileName = privateKeyFileName;
        this.outputDir = outputDir;
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
            Files.write(outputDir.resolve(publicKeyFileName), Collections.singletonList(Base64.getEncoder().encodeToString(pub)));
            Files.write(outputDir.resolve(privateKeyFileName), Collections.singletonList(Base64.getEncoder().encodeToString(priv)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Keys generated");
    }


    public static void main(String[] args) {
        CommandLine.run(new KeyGen(), args);
    }
}
