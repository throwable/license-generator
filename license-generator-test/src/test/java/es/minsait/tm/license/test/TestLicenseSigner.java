package es.minsait.tm.license.test;

import es.minsait.tm.license.gen.CodeSnippets;
import es.minsait.tm.license.gen.Enhancer;
import es.minsait.tm.license.gen.LicenseSigner;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.Permission;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static es.minsait.tm.license.test.util.asLocalPath;
import static org.junit.Assert.*;

public class TestLicenseSigner {
    private final static String PRODUCT_ID = "Test product V1.0";

    private static Runnable codeSnippetVerifier, enhancedClassVerifier;


    @Test
    public void testValidLicenseVerification() throws Exception {
        genLicenseKeyFile("license.properties");
        assertTrue(verifyLicense(codeSnippetVerifier));
    }

    @Test
    public void testInvalidLicenseVerification() throws Exception {
        genLicenseKeyFile("license-invalid.properties");
        assertFalse(verifyLicense(codeSnippetVerifier));
        assertEquals("java.lang.RuntimeException: 1", getLicenseFail());
    }

    @Test
    public void testExpiredLicenseVerification() throws Exception {
        genLicenseKeyFile("license-expired.properties");
        assertFalse(verifyLicense(codeSnippetVerifier));
        assertEquals("java.lang.RuntimeException: 2", getLicenseFail());
    }

    @Test
    public void testLicenseVerificationInterval() throws Exception {
        genLicenseKeyFile("license.properties");
        final Path tsFile = timestampFile();

        assertTrue(verifyLicense(codeSnippetVerifier));
        final FileTime lastModifiedTime = Files.getLastModifiedTime(tsFile);

        // repeat verification
        assertTrue(verifyLicense(codeSnippetVerifier));
        // the second verification must not be done as the verification time was not expired
        assertEquals(lastModifiedTime, Files.getLastModifiedTime(tsFile));

        Thread.sleep(1000);

        // verification time expired -- new verification must be done
        assertTrue(verifyLicense(codeSnippetVerifier));
        assertNotEquals(lastModifiedTime, Files.getLastModifiedTime(tsFile));
    }

    @Test
    public void testLicenseExpiration() throws Exception {
        genLicenseKeyFile("license.properties");
        final Path tsFile = timestampFile();

        assertTrue(verifyLicense(codeSnippetVerifier));

        Thread.sleep(1000); // verification time expiration

        Files.setLastModifiedTime(tsFile, FileTime.from(LocalDate.of(3001, 1, 2)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        assertFalse(verifyLicense(codeSnippetVerifier));
        assertEquals("java.lang.RuntimeException: 2", getLicenseFail());
    }

    @Test
    public void testValidLicenseVerificationEnhanced() throws Exception {
        genLicenseKeyFile("license.properties");
        assertTrue(verifyLicense(enhancedClassVerifier));
    }

    @Test
    public void testInvalidLicenseVerificationEnhanced() throws Exception {
        genLicenseKeyFile("license-invalid.properties");
        assertFalse(verifyLicense(enhancedClassVerifier));
        assertEquals("java.lang.RuntimeException: 1", getLicenseFail());
    }

    @Test
    public void testExpiredLicenseVerificationEnhanced() throws Exception {
        genLicenseKeyFile("license-expired.properties");
        assertFalse(verifyLicense(enhancedClassVerifier));
        assertEquals("java.lang.RuntimeException: 2", getLicenseFail());
    }

    @Test
    public void testLicenseVerificationIntervalEnhanced() throws Exception {
        genLicenseKeyFile("license.properties");
        final Path tsFile = timestampFile();

        assertTrue(verifyLicense(enhancedClassVerifier));
        final FileTime lastModifiedTime = Files.getLastModifiedTime(tsFile);

        // repeat verification
        assertTrue(verifyLicense(enhancedClassVerifier));
        // the second verification must not be done as the verification time was not expired
        assertEquals(lastModifiedTime, Files.getLastModifiedTime(tsFile));

        Thread.sleep(1000);

        // verification time expired -- new verification must be done
        assertTrue(verifyLicense(enhancedClassVerifier));
        assertNotEquals(lastModifiedTime, Files.getLastModifiedTime(tsFile));
    }

    @Test
    public void testLicenseExpirationEnhanced() throws Exception {
        genLicenseKeyFile("license.properties");
        final Path tsFile = timestampFile();

        assertTrue(verifyLicense(enhancedClassVerifier));

        Thread.sleep(1000); // verification time expiration

        Files.setLastModifiedTime(tsFile, FileTime.from(LocalDate.of(3001, 1, 2)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        assertFalse(verifyLicense(enhancedClassVerifier));
        assertEquals("java.lang.RuntimeException: 2", getLicenseFail());
    }




    private boolean verifyLicense(Runnable verifier) throws Exception
    {
        final AtomicReference<Boolean> verifiedStatus = new AtomicReference<>();
        final Thread thread = new Thread(() -> {
            try {
                try {
                    // Try to verify license
                    verifier.run();
                    // Verification passed
                    verifiedStatus.set(Boolean.TRUE);
                } catch (RuntimeException e) {
                    // Verification failed and tried to call System.exit(100)
                    verifiedStatus.set(Boolean.FALSE);
                }
            } catch (Exception e) {
                /* ignore */
            }
        });
        thread.start();
        thread.join(1000);
        final Boolean status = verifiedStatus.get();

        if (status == null)
            throw new RuntimeException("Method hangs up");

        return status;
    }


    @Before @After
    public void removeLicenseFailFile() throws Exception {
        try { Files.deleteIfExists(timestampFile()); } catch (Exception e) {/*ignore*/}
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir"), "license.key"));
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir"), "license.fail"));

        // clean loaded license & time check interval
        final Field _csInst_ = CodeSnippets.class.getDeclaredField("_INST_");
        _csInst_.setAccessible(true);
        final Object[] state = (Object[]) _csInst_.get(null);
        state[0] = null;
        state[2] = null;
    }

    private static String getLicenseFail() {
        try {
            return String.join("\n", Files.readAllLines(Paths.get(System.getProperty("user.dir"), "license.fail")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path timestampFile() {
        final Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(Paths.get("license.key"))) {
            properties.load(is);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        final String productId = properties.getProperty("productId");
        final String signature = properties.getProperty("signature");
        return Paths.get(System.getProperty("java.io.tmpdir"),
                ".lock-" + Integer.toHexString(productId.hashCode()) + Integer.toHexString(signature.hashCode()));
    }

    private static void genLicenseKeyFile(String licenseResource) {
        new LicenseSigner(
                asLocalPath("public.key"),
                asLocalPath("private.key"),
                asLocalPath(licenseResource)
        ).run();
    }

    @BeforeClass
    public static void setup() throws Exception
    {
        final Enhancer enhancer = new Enhancer(PRODUCT_ID,
                asLocalPath("public.key"), Duration.ofSeconds(1));
        final Class<?> enhancedClass = enhancer.enhanceClass("es.minsait.tm.license.test.SomeProtectedClass");
        enhancedClassVerifier = () -> {
            try {
                enhancedClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        {   // Set up codeSnippets params
            final Field _inst_ = enhancedClass.getDeclaredField("_INST_");
            _inst_.setAccessible(true);
            final Object params = _inst_.get(null);
            final Field _csInst_ = CodeSnippets.class.getDeclaredField("_INST_");
            _csInst_.setAccessible(true);
            _csInst_.set(null, params);
            codeSnippetVerifier = CodeSnippets::new;
        }

        class PreventSystemExitSecurityManager extends SecurityManager {
            @Override public void checkExit(int status) {
                throw new SecurityException();
            }

            @Override public void checkPermission(Permission perm) {
                // Allow other activities by default
            }
        }
        System.setSecurityManager(new PreventSystemExitSecurityManager());
    }

    @AfterClass
    public static void removeLicenseFile() {
        System.setSecurityManager(null);
    }
}
