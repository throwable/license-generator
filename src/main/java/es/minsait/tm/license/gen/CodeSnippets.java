package es.minsait.tm.license.gen;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Comparator;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CodeSnippets {
    // [0]: String: Base64-encoded content of license file or Exception
    // [1]: byte[]: public key
    // [2]: Long: next license check timestamp in ms (or null)
    // [3]: Integer: license check interval in ms
    // [4]: String: path of last timestamp file
    private static Object[] __cf;

    private synchronized void __conf() {
        if (__cf == null) {
            /*
            Read license.key file
             */
            __cf = new Object[5];

            // TODO: encode text
            final String $lp = "license.key";

            try {
                final BufferedReader br;
                InputStream r = this.getClass().getResourceAsStream($lp);
                if (r != null) {
                    br = new BufferedReader(new InputStreamReader(r, StandardCharsets.UTF_8));
                } else {
                    String f = System.getProperty($lp);
                    if (f == null) {
                        // TODO: encode text
                        String u = this.getClass().getResource(this.getClass().getSimpleName() + ".class").toString();

                        if (u.startsWith("jar:")) {
                            // detect basedir where the jar is located
                            /*
                            jar:file:/D:/workspaces/idea/toa-data-extractor/target/toa-data-extractor.jar!/es/indra/tcol/tde/Main.class
                             */
                            Pattern regex = Pattern.compile("jar:([^!]+).*");
                            Matcher m = regex.matcher(u);
                            if (!m.matches())
                                throw new IllegalStateException();
                            String jarUrl = m.group(1);
                            try {
                                f = Paths.get(new URL(jarUrl).toURI()).toFile().getParent();
                            } catch (Exception e) {
                                throw new IOException(e);
                            }
                        } else {
                            // local run - set as current dir
                            /*
                            file:/D:/workspaces/idea/toa-data-extractor/target/classes/es/indra/tcol/tde/ApplicationContext.class
                            */
                            // TODO: encode text
                            File userDir = new File(System.getProperty("user.dir"));
                            f = userDir.getPath();
                        }
                    }
                    br = Files.newBufferedReader(Paths.get(f));
                }
                __cf[0] = Base64.getEncoder().encodeToString(br.lines()
                        .collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8));
                br.close();
            } catch (Exception e) {
                __cf[0] = e;
                return;
            }
        } else {
            /*
            Check license
             */
            if (__cf[0] instanceof Exception) {
                // TODO: exit in new thread with delay
                System.out.println(__cf[0]);
                System.exit(100);
                return;
            }

            if (__cf[2] != null && (Long) __cf[2] > System.nanoTime())
                return;

            /*
             Checking license...
             */
            Properties pp = new Properties();
            try {
                pp.load(new ByteArrayInputStream(Base64.getDecoder().decode((String) __cf[0])));
            } catch (Exception e) {
                __cf[0] = new RuntimeException(e);
                return;
            }
            // TODO: encode text
            final String $s = "signature";
            final byte[] s = Base64.getDecoder().decode(pp.getProperty($s));
            final byte[] d = pp.entrySet().stream()
                    .filter(e -> !$s.equals(e.getKey()))
                    .sorted(Comparator.comparing(e -> e.getKey().toString()))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("\n"))
                    .getBytes(StandardCharsets.UTF_8);

            try {
                // TODO: encode text
                Signature ts = Signature.getInstance("SHA1WithRSA");
                ts.initVerify(KeyFactory.getInstance(new String(new char[] {'R', 'S', 'A'}))
                        .generatePublic(new X509EncodedKeySpec((byte[]) __cf[1]))
                );
                ts.update(d);
                if (!ts.verify(s)) {
                    __cf[0] = new RuntimeException();
                }
            } catch (Exception e) {
                __cf[0] = e;
                return;
            }

            /*
            Checking timestamp
             */
            // TODO: encode text
            try {
                final String $uh = "user.home";
                final Path p = Paths.get(System.getProperty($uh), __cf[4].toString());
                Instant i;
                try {
                    i = Files.getLastModifiedTime(p).toInstant();
                } catch (Exception ex) {
                    i = Instant.now();
                }
                final LocalDate ld = i.atZone(ZoneId.systemDefault()).toLocalDate();
                final String $vf = "validFrom";
                final String $vt = "validUntil";
                if (ld.isBefore(LocalDate.parse(pp.getProperty($vf)))
                        || ld.isAfter(LocalDate.parse(pp.getProperty($vt))))
                {
                    __cf[1] = new RuntimeException();
                }

                if (Instant.now().isAfter(i)) {
                    try (BufferedWriter o = Files.newBufferedWriter(p)) {
                        o.append(UUID.randomUUID().toString());
                    } catch (IOException e) {
                        /* ignore */
                    }
                }

                __cf[2] = System.nanoTime() + (Integer) __cf[3] * 1000000;
                return;
            } catch (Exception e) {
                __cf[0] = e;
            }
        }

        // Set up public key
        // __cf[1] = public key
        // __cf[3] = license check interval
        // __cf[4] = timestamp file
    }
}
