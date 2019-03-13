package es.minsait.tm.license.gen;

import javassist.*;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@CommandLine.Command(name = "enhance")
public class Enhancer implements Runnable {

    /*@CommandLine.Option(names = {"--keyfile", "-kf"}, required = true)
    private String pubKeyFile;*/

    @CommandLine.Option(names = {"--checkinv", "-ci"})
    private int checkInterval = 3600_000;

    @CommandLine.Option(names = {"--tsfile", "-tf"})
    private String timestampFile = ".chkfile";

    @CommandLine.Parameters(arity = "1..*", paramLabel = "CLASS", description = "Class(es) to process.")
    private List<String> classNames;


    public Enhancer() {}

    public Enhancer(List<String> classNames) {
        this.classNames = classNames;
    }


    private void generate(String className) throws Exception
    {
        ClassPool pool = ClassPool.getDefault();
        CtClass targetClass = pool.get(className);
        try {
            targetClass.getDeclaredField("__cf");
            return;   // already exists
        } catch (NotFoundException e) { /* continue */ }

        final CtClass ctCodeSnippets = pool.get(CodeSnippets.class.getName());
        targetClass.addField(new CtField(ctCodeSnippets.getField("__cf"), targetClass));

        CtMethod __conf = ctCodeSnippets.getDeclaredMethod("__conf");
        __conf = CtNewMethod.copy(__conf, targetClass, null);
        // __cf[1] = public key
        __conf.insertAfter("if (__cf[1] == null) {" +
                "__cf[1] = new byte[]{1, 2, 3, 4, 5};" +
                "__cf[3] = " + checkInterval + ";" +
                "__cf[4] = \"" + timestampFile + "\";}");
        targetClass.addMethod(__conf);

        for (CtConstructor constructor : targetClass.getConstructors()) {
            constructor.insertBeforeBody("__conf();");
        }

        final URL url = targetClass.getURL();
        //targetClass.writeFile(Paths.get(url.toURI()).getParent().toString());
        Files.write(Paths.get(url.toURI()), targetClass.toBytecode());
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
