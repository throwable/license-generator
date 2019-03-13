package es.minsait.tm.license.test;

import es.minsait.tm.license.gen.KeyGen;
import es.minsait.tm.license.test.SomeProtectedClass;
import es.minsait.tm.license.gen.Enhancer;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class TestEnhancer {
    @Test
    public void testGenerator() throws Exception {
        final Enhancer enhancer = new Enhancer("Test Product v1.0", 1000, 2000, ".licence-test",
                Collections.singletonList("es.minsait.tm.license.test.SomeProtectedClass"));
        enhancer.generate("es.minsait.tm.license.test.SomeProtectedClass");
        //final Class<?> enhancedClass = enhancer.enhanceClass("es.minsait.tm.license.test.SomeProtectedClass");
        //enhancedClass.newInstance();
        final Class<?> aClass = Class.forName("es.minsait.tm.license.test.SomeProtectedClass");
    }
}
