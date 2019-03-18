package es.minsait.tm.license.gen;

import javassist.*;
import picocli.CommandLine;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static picocli.CommandLine.*;

@Command(name = "enhance", description = "Enhance compiled classes with the license check code")
public class Enhancer implements Runnable
{
    @Option(names = {"--product", "-p"}, required = true, description = "Product identifier")
    private String productId;

    @Option(names = {"--key", "-k"}, required = true, description = "Public key file")
    private Path pubKeyFile;

    @Option(names = {"--check", "-c"}, description = "License periodic verification interval (default: ${DEFAULT-VALUE}).")
    private Duration checkInterval = Duration.ofMinutes(10);

    @Option(names = {"-classpath", "-cp"}, description = "Target classpath")
    private String classpathList;

    @Parameters(arity = "1..*", paramLabel = "CLASS", description = "Class(es) to process.")
    private List<String> classNames;


    private Enhancer() {}

    public Enhancer(String productId, Path pubKeyFile, Duration checkInterval) {
        this.productId = productId;
        this.checkInterval = checkInterval;
        this.pubKeyFile = pubKeyFile;
    }

    public Enhancer(String productId, Path pubKeyFile, Duration checkInterval, List<String> classNames) {
        this.productId = productId;
        this.checkInterval = checkInterval;
        this.classNames = classNames;
        this.pubKeyFile = pubKeyFile;
    }


    private CtClass enhance(String className) throws Exception
    {
        final ClassPool pool = ClassPool.getDefault();
        if (classpathList != null)
            pool.appendPathList(classpathList);
        CtClass targetClass = pool.get(className);
        try {
            targetClass.getDeclaredField("_INST_");
            return null;   // already enhanced
        } catch (NotFoundException e) { /* continue */ }

        final CtClass ctCodeSnippets = pool.get(CodeSnippets.class.getName());
        final byte[] pub = Base64.getDecoder().decode(String.join("\n", Files.readAllLines(pubKeyFile)));
        targetClass.addField(new CtField(ctCodeSnippets.getField("_INST_"), targetClass),
                "new Object[] {" +
                        "null," +
                        toByteArrayCode(pub) + "," +
                        "null," +
                        "Integer.valueOf(" + checkInterval.toMillis() +")," +
                        "null," +
                        toByteArrayCode(encodeString(productId)) + "," +
                        "null," +
                        "new RuntimeException()" +
                    "};"
                );
        CtMethod _init_ = ctCodeSnippets.getDeclaredMethod("_init_");
        CtMethod _initCopy_ = CtNewMethod.copy(_init_, targetClass, null);
        targetClass.addMethod(_initCopy_);

        /*final CtMethod test = ctCodeSnippets.getDeclaredMethod("test");
        final CtMethod testCopy = CtNewMethod.copy(test, targetClass, null);
        targetClass.addMethod(testCopy);*/

        for (CtConstructor constructor : targetClass.getConstructors()) {
            constructor.insertBeforeBody("_init_();");
        }

        /*final CtClass ctObject = pool.get("java.lang.Object");
        final CtField f = new CtField(ctObject, "_INST_", targetClass);
        targetClass.addField(f);*/

        targetClass.rebuildClassFile();
        return targetClass;
    }

    public void generate(String className) throws Exception {
        final CtClass targetClass = enhance(className);
        if (targetClass != null) {
            final URL url = targetClass.getURL();
            //targetClass.writeFile(Paths.get(url.toURI()).getParent().toString());
            Files.write(Paths.get(url.toURI()), targetClass.toBytecode());
        }
    }

    public Class<?> enhanceClass(String className) throws Exception {
        final CtClass targetClass = enhance(className);
        if (targetClass != null) {
            return targetClass.toClass(Thread.currentThread().getContextClassLoader(), null);
        } else
            return Thread.currentThread().getContextClassLoader().loadClass(className);
    }


    private static byte[] encodeString(String s) {
        final byte[] buf = s.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte)((buf[i] ^ 0x55) & 0xff);
        }
        return buf;
    }

    private static String toByteArrayCode(byte[] array) {
        final StringBuilder sb = new StringBuilder("new byte[]{");
        for (int i = 0; i < array.length; i++) {
            if (i != 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("}");
        return sb.toString();
    }


    public void run() {
        try {
            for (String className : classNames) {
                generate(className);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        CommandLine.run(new Enhancer(), args);
    }
}
