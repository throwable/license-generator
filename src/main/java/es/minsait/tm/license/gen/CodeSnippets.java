package es.minsait.tm.license.gen;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeSnippets {
    // [0]: byte[]: content of license file xored with 0x55 or Exception
    // [1]: byte[]: public key
    // [2]: Long: next license check nanotimestamo in nanoseconds (or null)
    // [3]: Integer: license check interval in ms
    // [4]: Last check instant timestamp in ms (or null)
    // [5]: byte[]: productID - xored 0x55
    // [6]: Integer: milliseconds to wait before exit in case if license verification fail
    // [7]: RuntimeException: throwable to throw when the license is not valid
    private static Object[] _INST_;


    public CodeSnippets() {
        _init_();
    }

    private synchronized void _init_()
    {
        final Object[] s = _INST_;
        final Object s0 = s[0];

        // "user.dir"
        final byte[] $b = {117, 115, 101, 114, 46, 100, 105, 114};

        if (s0 == null) {
            /*
            Read license.key file
            */
            // "license.key"
            final byte[] $a = {108, 105, 99, 101, 110, 115, 101, 46, 107, 101, 121};

            InputStream r = null;
            try {
                if (System.getProperty(new String($a)) != null) {
                    // Defined system property license.key
                    r = Files.newInputStream(Paths.get(System.getProperty(new String($a))));
                } else {
                    // Find license.key in the current directory
                    Path f = Paths.get(System.getProperty(new String($b)), new String($a));
                    if (Files.isReadable(f))
                        r = Files.newInputStream(f);
                    else {
                        // Find license.key in the .jar file location
                        // jar:file:/path/to/file.jar!/package/name/MyClass.class
                        final String cr = this.getClass().getResource('/' + this.getClass().getName()
                                .replace('.', '/') +
                                // ".class"
                                new String(new byte[] {46, 99, 108, 97, 115, 115})).toString();
                        // "jar:"
                        final String $j = new String(new byte[] {106, 97, 114, 58});
                        if (cr.startsWith($j)) {
                            // detect basedir where the .jar file is located
                            final Matcher m = Pattern.compile($j + "([^!]+).*").matcher(cr);
                            if (m.matches()) {
                                // file:/path/to/file.jar
                                f = Paths.get(new URL(m.group(1)).toURI()).resolveSibling(new String($a));
                                if (Files.isReadable(f)) {
                                    r = Files.newInputStream(f);
                                }
                            }
                        }
                        if (r == null) {
                            // At last try to load license.key from classpath root
                            r = this.getClass().getResourceAsStream("/" + new String($a));
                            if (r == null)
                                // this provokes FileNotFoundException
                                Files.readAllBytes(f);
                        }
                    }
                }

                final ByteArrayOutputStream a = new ByteArrayOutputStream();
                int nr;
                final byte[] b = new byte[16384];

                while ((nr = r.read(b, 0, b.length)) != -1) {
                    a.write(b, 0, nr);
                }
                final byte[] c = a.toByteArray();
                for (int i = 0; i < c.length; i++) {
                    c[i] = (byte)((c[i] ^ 0x55) & 0xff);
                }
                s[0] = c;
            } catch (Exception e) {
                s[0] = e;
            } finally {
                if (r != null) try { r.close(); } catch (Exception e) {/* ignore */}
            }
        }

        /*
        Exit if check interval was not completed
         */
        if (s[2] != null && (Long) s[2] - System.nanoTime() > 0)
            return;



        if (!(s[0] instanceof Exception)) {

            // Checking license...

            try {
                final Properties l = new Properties();
                {
                    final byte[] bb = Arrays.copyOf((byte[]) s[0], ((byte[]) s[0]).length);
                    for (int i = 0; i < bb.length; i++) {
                        bb[i] = (byte) ((bb[i] ^ 0x55) & 0xff);
                    }
                    l.load(new ByteArrayInputStream(bb));
                }

                // verifying signature
                boolean v1;
                // "signature";
                final byte[] $c = {115, 105, 103, 110, 97, 116, 117, 114, 101};
                {
                    final byte[] sg = Base64.getDecoder().decode(l.getProperty(new String($c)));
                    final byte[] d;
                    {
                        /*license.entrySet().stream()
                            .filter(e -> !$s.equals(e.getKey()))
                            .sorted(Comparator.comparing(e -> e.getKey().toString()))
                            .map(e -> e.getKey() + "=" + e.getValue())
                            .collect(Collectors.joining("\n"))
                            .getBytes(StandardCharsets.UTF_8);*/
                        final ArrayList<String> p = new ArrayList<>();
                        for (Map.Entry<Object, Object> e : new TreeMap<>(l).entrySet()) {
                            if (new String($c).equals(e.getKey())) continue;
                            p.add(e.getKey() + "=" + e.getValue());
                        }
                        d = String.join("\n", p).getBytes(StandardCharsets.UTF_8);
                    }

                    try {
                        // "java.security."
                        final String $j = new String(new byte[]{106, 97, 118, 97, 46, 115, 101, 99, 117, 114, 105, 116, 121, 46});
                        final String $i = new String(new byte[]{103, 101, 116, 73, 110, 115, 116, 97, 110, 99, 101});
                        // Signature.getInstance("SHA1WithRSA")
                        final Object /*Signature*/ sv = Class.forName($j +
                                // "Signature"
                                new String (new byte[] {83, 105, 103, 110, 97, 116, 117, 114, 101}))
                                // "getInstance"
                                .getMethod($i, String.class)
                                .invoke(null,
                                        // "SHA1WithRSA"
                                        new String(new byte[] {83, 72, 65, 49, 87, 105, 116, 104, 82, 83, 65}));
                        // new X509EncodedKeySpec((byte[]) s[1])
                        Object ks = Class.forName($j +
                                // spec.X509EncodedKeySpec
                                new String(new byte[] {115, 112, 101, 99, 46, 88, 53, 48, 57, 69, 110, 99, 111, 100, 101, 100, 75, 101, 121, 83, 112, 101, 99}))
                                .getConstructor(byte[].class)
                                .newInstance(s[1]);
                        // KeyFactory.getInstance("RSA")
                        final Object kf = Class.forName($j +
                                // "KeyFactory"
                                new String(new byte[]{75, 101, 121, 70, 97, 99, 116, 111, 114, 121}))
                                // "getInstance"
                                .getMethod($i, String.class)
                                .invoke(null,
                                        // "RSA"
                                        new String(new byte[]{82, 83, 65}));
                        // kf.generatePublic(ks)
                        final Object k = kf.getClass().getMethod(
                                // "generatePublic"
                                new String(new byte[]{103, 101, 110, 101, 114, 97, 116, 101, 80, 117, 98, 108, 105, 99}),
                                Class.forName($j +
                                        // "spec.KeySpec
                                        new String(new byte[] {115, 112, 101, 99, 46, 75, 101, 121, 83, 112, 101, 99})
                                        ))
                                .invoke(kf, ks);
                        // sv.initVerify(k)
                        sv.getClass().getMethod(
                                // "initVerify"
                                new String(new byte[]{105, 110, 105, 116, 86, 101, 114, 105, 102, 121}),
                                Class.forName($j +
                                        // "PublicKey"
                                        new String(new byte[] {80, 117, 98, 108, 105, 99, 75, 101, 121})))
                                .invoke(sv, k);
                        // sv.update(data)
                        sv.getClass().getMethod(
                                // "update"
                                new String(new byte[]{117, 112, 100, 97, 116, 101}), byte[].class)
                                .invoke(sv, (Object) d);
                        // sv.verify(sg)
                        v1 = (boolean) sv.getClass().getMethod(
                                // "verify"
                                new String(new byte[]{118, 101, 114, 105, 102, 121}), byte[].class)
                                .invoke(sv, (Object) sg);
                    } catch (Exception e) {
                        throw new IllegalStateException();
                    }
                }

                // verifying product
                boolean v2;
                // "productId"
                final String $p = new String(new byte[] {112, 114, 111, 100, 117, 99, 116, 73, 100} );
                {
                    final byte[] a = (byte[]) s[5];
                    final byte[] b = Arrays.copyOf(a, a.length);
                    for (int i = 0; i < b.length; i++) {
                        b[i] = (byte) ((b[i] ^ 0x55) & 0xff);
                    }
                    v2 = new String(b, StandardCharsets.UTF_8).equals(l.getProperty($p));
                }

                // verifying timestamp
                boolean v3;
                Instant t;
                final Path tf;
                final Instant n = Instant.now();
                {
                    tf = Paths.get(System.getProperty(
                            // "java.io.tmpdir"
                            new String(new byte[] {106, 97, 118, 97, 46, 105, 111, 46, 116, 109, 112, 100, 105, 114})),
                            // filename: .lock-<#productId><#signature>
                            "." +
                                    // "lock"
                                    new String( new byte[] {108, 111, 99, 107} ) +
                                    "-" +
                            Integer.toHexString(l.getProperty($p).hashCode()) +
                                    Integer.toHexString(l.getProperty(new String($c)).hashCode())
                            );
                    try {
                        t = Files.getLastModifiedTime(tf).toInstant();
                        if (n.isAfter(t))
                            t = n;
                    } catch (Exception ex) {
                        t = n;
                    }
                    if (s[4] != null) {
                        final Instant lastTs = Instant.ofEpochMilli((Long) s[4]);
                        if (lastTs.isAfter(t))
                            t = lastTs;
                    }

                    final LocalDate ld = t.atZone(ZoneId.systemDefault()).toLocalDate();

                    try {
                        v3 = !ld.isBefore(LocalDate.parse(l.getProperty(
                                // "validFrom"
                                new String(new byte[] {118, 97, 108, 105, 100, 70, 114, 111, 109} )
                        )))
                                && !ld.isAfter(LocalDate.parse(l.getProperty(
                                        // "validUntil"
                                        new String(new byte[] {118, 97, 108, 105, 100, 85, 110, 116, 105, 108})
                        )));
                    } catch (Exception e) {
                        throw new IllegalStateException();
                    }
                }

                final List<Boolean> v = Arrays.asList(v1, v2, v3);
                for (int i1 = 0; i1 < v.size(); i1++) {
                    if (!v.get(i1)) {
                        s[0] = new RuntimeException("" + i1);
                        break;
                    }
                }

                // update timestamp
                if (!n.isBefore(t)) {
                    try (BufferedWriter o = Files.newBufferedWriter(tf)) {
                        o.append(UUID.randomUUID().toString());
                    } catch (IOException e) {
                         /* ignore */
                    }
                    s[4] = n.toEpochMilli();
                }
                s[2] = System.nanoTime() + (Integer) s[3] * 1000000;
            } catch (Exception e) {
                s[0] = e;
            }
        }

        if ((s[0] instanceof Exception) && !(s0 instanceof Exception)) {
            // exit in new thread with delay
            //System.out.println(state[0]);
            try {
                Files.write(Paths.get(System.getProperty(new String($b)),
                        // "license.fail"
                        new String(new byte[] {108, 105, 99, 101, 110, 115, 101, 46, 102, 97, 105, 108})),
                        s[0].toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) { /* ignore */ }
            try {
                //System.exit(100);
                Class.forName(
                        // "java.lang.System"
                        new String(new byte[] {106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 121, 115, 116, 101, 109})
                ).getMethod(
                        // "exit"
                        new String(new byte[] {101, 120, 105, 116}
                        ), Integer.TYPE)
                .invoke(null, 100);
            } catch (Exception ex) {/*ignore*/}
            // finally throw exception with the wrong stack trace
            throw (RuntimeException) s[7];
        }
    }
}
