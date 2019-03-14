package es.minsait.tm.license.test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class util {
    public static Path asLocalPath(String resource) {
        final URL url = util.class.getClassLoader().getResource(resource);
        Objects.requireNonNull(url);
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
