package es.minsait.tm.license.test;

import es.minsait.tm.license.gen.KeyGen;
import es.minsait.tm.license.gen.LicenseSigner;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Properties;

import static es.minsait.tm.license.test.util.*;

public class Generator {
    @Test @Ignore
    public void generateKeys() {
        new KeyGen().run();
    }


    @Test @Ignore
    public void generateSampleLicense() {
        new LicenseSigner(
                asLocalPath("public.key"),
                asLocalPath("private.key"),
                asLocalPath("license.properties")
        ).run();
    }
}
