package es.minsait.tm.license.test;

import es.minsait.tm.license.gen.KeyGen;
import org.junit.Test;

public class GenerateKeys {
    @Test
    public void generateKeys() throws Exception {
        final String dir = System.getProperty("user.dir");
        new KeyGen("public.key", "private.key").run();
    }
}
