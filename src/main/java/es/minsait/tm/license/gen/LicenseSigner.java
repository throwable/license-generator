package es.minsait.tm.license.gen;

import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Collectors;

import static picocli.CommandLine.*;

@Command(name = "sign", description = "Sign license and generate license.key file")
public class LicenseSigner implements Runnable
{
    @Option(names = {"-pub"}, required = true, description = "Public key file")
    private Path publicKeyFile;

    @Option(names = {"-pri"}, required = true, description = "Private key file")
    private Path privateKeyFile;

    @Option(names = {"-l"}, required = true, description = "license.properties file to sign")
    private Path licenseFile;

    @Option(names = {"-o"}, description = "Output directory for generated license.key (default: ${DEFAULT-VALUE})")
    private Path outputDir = Paths.get(System.getProperty("user.dir"));


    private LicenseSigner() {}

    public LicenseSigner(Path publicKeyFile, Path privateKeyFile, Path licenseFile) {
        this.publicKeyFile = publicKeyFile;
        this.privateKeyFile = privateKeyFile;
        this.licenseFile = licenseFile;
    }


    public Properties sign() throws Exception {
        KeyPair keyPair = loadKeys();
        final Properties license = new Properties();
        try (InputStream is = Files.newInputStream(licenseFile)) {
            license.load(is);
        }

        // check required fields
        checkRequired(license, "productId");
        checkRequired(license, "validFrom");
        checkRequired(license, "validUntil");
        // check format
        LocalDate.parse(license.getProperty("validFrom"));
        LocalDate.parse(license.getProperty("validUntil"));

        final byte[] data = license.entrySet().stream()
                .filter(e -> !"signature".equals(e.getKey()))
                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"))
                .getBytes(StandardCharsets.UTF_8);
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(keyPair.getPrivate());
        sig.update(data);
        byte[] signature = sig.sign();

        final Properties signedLicense = new Properties();
        signedLicense.putAll(license);
        signedLicense.setProperty("signature", Base64.getEncoder().encodeToString(signature));
        return signedLicense;
    }


    @Override
    public void run() {
        try {
            final Properties signedLicense = sign();
            final Path licenseKey = outputDir.resolve("license.key");
            try (OutputStream os = Files.newOutputStream(licenseKey)) {
                signedLicense.store(os, "License Key File. Please put it in the product directory folder " +
                        "or modify classpath to include this file as a root resource.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void checkRequired(Properties license, String field) {
        final String property = license.getProperty(field);
        if (property == null || property.isEmpty())
            throw new RuntimeException("Required field: " + field);
    }


    private KeyPair loadKeys() throws Exception {
        byte[] pub = Base64.getDecoder().decode(String.join("\n", Files.readAllLines(publicKeyFile)));
        byte[] pri = Base64.getDecoder().decode(String.join("\n", Files.readAllLines(privateKeyFile)));
        // Generate KeyPair.
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                pub);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(pri);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        return new KeyPair(publicKey, privateKey);
    }

    public static void main(String[] args) {
        CommandLine.run(new LicenseSigner(), args);
    }
}
