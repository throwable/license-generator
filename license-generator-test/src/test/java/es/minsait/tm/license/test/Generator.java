package es.minsait.tm.license.test;

import es.minsait.tm.license.gen.KeyGen;
import es.minsait.tm.license.gen.LicenseSigner;
import org.junit.Test;

import java.util.Properties;

import static es.minsait.tm.license.test.util.*;

public class Generator {
    @Test
    public void generateKeys() throws Exception {
        new KeyGen("public.key", "private.key").run();
    }


    @Test
    public void generateSampleLicense() throws Exception {
        new LicenseSigner(
                asLocalPath("public.key").toString(),
                asLocalPath("private.key").toString(),
                asLocalPath("license.properties").toString()
        ).run();
    }
}
