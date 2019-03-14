package es.minsait.tm.license.gen;

import com.sun.istack.internal.Nullable;
import javassist.*;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ConstPool;
import picocli.CommandLine;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@CommandLine.Command(name = "enhance")
public class Enhancer implements Runnable {
    @CommandLine.Option(names = {"--product", "-p"}, required = true, description = "Product identifier")
    private String productId;

    @CommandLine.Option(names = {"--key", "-k"}, required = true, description = "Public key file")
    private String pubKeyFile;

    @CommandLine.Option(names = {"--check", "-c"}, description = "Check interval in ms.")
    private int checkInterval = 3600_000;

    //@CommandLine.Option(names = {"--wait", "-w"}, description = "Time interval before the program terminates when verification fails (in ms.)")
    private int exitWaitTime = 3600_000;

    @CommandLine.Option(names = {"--tsfile", "-tf"})
    private String timestampFile = ".chkfile";

    @CommandLine.Parameters(arity = "1..*", paramLabel = "CLASS", description = "Class(es) to process.")
    private List<String> classNames;


    private Enhancer() {}

    public Enhancer(String productId, String pubKeyFile, int checkInterval, String timestampFile) {
        this.productId = productId;
        this.checkInterval = checkInterval;
        this.timestampFile = timestampFile;
        this.pubKeyFile = pubKeyFile;
    }

    public Enhancer(String productId, String pubKeyFile, int checkInterval, String timestampFile, List<String> classNames) {
        this.productId = productId;
        this.checkInterval = checkInterval;
        this.timestampFile = timestampFile;
        this.classNames = classNames;
        this.pubKeyFile = pubKeyFile;
    }


    @Nullable
    private CtClass enhance(String className) throws Exception
    {
        ClassPool pool = ClassPool.getDefault();
        CtClass targetClass = pool.get(className);
        try {
            targetClass.getDeclaredField("_INST_");
            return null;   // already exists
        } catch (NotFoundException e) { /* continue */ }

        final CtClass ctCodeSnippets = pool.get(CodeSnippets.class.getName());
        final byte[] pub = Base64.getDecoder().decode(String.join("\n", Files.readAllLines(Paths.get(pubKeyFile))));
        targetClass.addField(new CtField(ctCodeSnippets.getField("_INST_"), targetClass),
                "new Object[] {" +
                        "null," +
                        toByteArrayCode(pub) + "," +
                        "null," +
                        "Integer.valueOf(" + checkInterval +")," +
                        toByteArrayCode(encodeString(timestampFile)) + "," +
                        toByteArrayCode(encodeString(productId)) + "," +
                        "Integer.valueOf(" + exitWaitTime + ")," +
                        "new RuntimeException()" +
                    "};"
                );
        CtMethod _init_ = ctCodeSnippets.getDeclaredMethod("_init_");
        CtMethod _initCopy_ = CtNewMethod.copy(_init_, "test1", targetClass, null);
        /*for (AttributeInfo attribute : _init_.getMethodInfo2().getAttributes()) {
            _initCopy_.getMethodInfo().addAttribute(attribute);
        }*/
        targetClass.addMethod(_initCopy_);

        /*final CtMethod test = ctCodeSnippets.getDeclaredMethod("test");
        final CtMethod testCopy = CtNewMethod.copy(test, targetClass, null);
        targetClass.addMethod(testCopy);*/

        /*for (CtConstructor constructor : targetClass.getConstructors()) {
            constructor.insertBeforeBody("_init_();");
        }*/

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
