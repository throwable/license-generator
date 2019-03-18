package es.minsait.tm.license.gen;

import picocli.CommandLine;

import java.util.List;

import static picocli.CommandLine.*;

@Command(subcommands = {
        picocli.CommandLine.HelpCommand.class,
        Enhancer.class, KeyGen.class, LicenseSigner.class
})
public class Main implements Runnable {

    public static void main(String... args) {
        try {
            CommandLine.run(new Main(), args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        CommandLine.usage(new Main(), System.err);
        System.exit(2);
    }
}
