package es.minsait.tm.license.test;

import es.minsait.tm.license.gen.CodeSnippets;
import es.minsait.tm.license.gen.Enhancer;
import es.minsait.tm.license.gen.LicenseSigner;
import org.junit.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static es.minsait.tm.license.test.util.asLocalPath;
import static org.junit.Assert.*;

public class TestLicenseSigner {
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


    private boolean verifyLicense(Runnable verifier) throws Exception
    {
        final SynchronousQueue<Boolean> verifiedStatus = new SynchronousQueue<>();
        final Thread thread = new Thread(() -> {
            try {
                try {
                    // Try to verify license
                    verifier.run();
                    // Verification passed
                    verifiedStatus.put(Boolean.TRUE);
                } catch (RuntimeException e) {
                    // Verification failed and tried to call System.exit(100)
                    verifiedStatus.put(Boolean.FALSE);
                }
            } catch (Exception e) {
                /* ignore */
            }
        });
        thread.start();
        final Boolean status = verifiedStatus.poll(1, TimeUnit.SECONDS);
        thread.join();

        if (status == null)
            throw new RuntimeException("Method hangs up");

        return status;
    }


    @Before @After
    public void removeLicenseFailFile() throws Exception {
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir"), "license.key"));
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir"), "licence.fail"));

        // clean loaded license & time check interval
        final Field _csInst_ = CodeSnippets.class.getDeclaredField("_INST_");
        _csInst_.setAccessible(true);
        final Object[] state = (Object[]) _csInst_.get(null);
        state[0] = null;
        state[2] = null;
    }

    private static String getLicenseFail() {
        try {
            return String.join("\n", Files.readAllLines(Paths.get(System.getProperty("user.dir"), "licence.fail")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void genLicenseKeyFile(String licenseResource) {
        new LicenseSigner(
                asLocalPath("public.key").toString(),
                asLocalPath("private.key").toString(),
                asLocalPath(licenseResource).toString()
        ).run();
    }

    @BeforeClass
    public static void setup() throws Exception
    {
        final Enhancer enhancer = new Enhancer("Test product V1.0",
                asLocalPath("public.key").toString(), 1000, ".licence-test");
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
    public static void removeLicenseFile() throws Exception {
        System.setSecurityManager(null);
    }
}
