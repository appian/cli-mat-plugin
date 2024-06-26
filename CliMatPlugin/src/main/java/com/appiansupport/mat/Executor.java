package com.appiansupport.mat;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.ConsoleController;
import com.appiansupport.mat.knownobjects.KnownObjectProvider;
import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.utils.internal.ThreadPrinter;
import com.appiansupport.mat.resolvers.KnownObjectExtensionResolver;
import com.appiansupport.mat.resolvers.KnownObjectResolver;
import com.appiansupport.mat.suspects.LeakSuspectPrinter;
import com.appiansupport.mat.suspects.LeakSuspectsFinder;
import com.appiansupport.mat.utils.internal.HeapSizer;
import com.appiansupport.mat.utils.internal.HeapTablePrinter;
import com.appiansupport.mat.utils.internal.ObjectFetcher;
import com.appiansupport.mat.utils.PrintUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.eclipse.mat.util.IProgressListener;

public class Executor implements IApplication {
  private ISnapshot snapshot;
  private ResultsBuilder resultsBuilder;
  private static final String CLI_FLAG = "c";
  private static final String HELP_FLAG = "h";
  private static final String OBJECT_ID_FLAG = "i";


  private static ISnapshot openSnapshot(File heapDump) throws SnapshotException {
    System.out.println("Opening " + heapDump.toString());
    return SnapshotFactory.openSnapshot(heapDump, new ConsoleProgressListener(System.out));
  }

  @Override public Object start(IApplicationContext context) {
    String[] args = (String[]) context.getArguments().get("application.args");
    execute(args);
    return IApplication.EXIT_OK;
  }

  private void execute(String[] args) {

    Options options = new Options();
    options.addOption(HELP_FLAG, "help", false, "Print this menu");
    options.addOption(CLI_FLAG, "cli", false, "Execute CLIMAT CLI");
    options.addOption(OBJECT_ID_FLAG, "id", true, "Comma-separated hexadecimal object IDs to analyze");
    HelpFormatter helpFormatter = new HelpFormatter();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException exp) {
      System.err.println("ERROR: Argument parsing failed. Reason: " + exp.getMessage());
      return;
    }

    if (args.length < 1 || cmd.hasOption(HELP_FLAG)) {
      helpFormatter.printHelp("CliMatPlugin.execute <HPROF>", options);
      return;
    }

    File heapFile = new File(args[0]);

    if (!heapFile.isFile()) {
      System.err.println("ERROR: Heap dump file " + args[0] + " not found");
      return;
    }
    String objectAddressInput = cmd.getOptionValue(OBJECT_ID_FLAG);
    String datetime = getNowAsString();
    String heapFileLocation = FilenameUtils.getFullPath(args[0]);
    String outFileName = String.format("%sclimat_results_%s.txt", heapFileLocation, datetime);

    try {
      //This line will trigger parsing & creation of .index files if none exist
      snapshot = openSnapshot(heapFile);
    } catch (SnapshotException snapshotException) {
      System.err.println("ERROR: SnapshotException while parsing Heap file (generating .index files):");
      snapshotException.printStackTrace();
      if (snapshot != null) {
        snapshot.dispose();
      }
      return;
    }

    IProgressListener listener = new ConsoleProgressListener(System.out);
    ObjectFetcher objectFetcher = new ObjectFetcher(snapshot);
    HeapSizer heapSizer = new HeapSizer(snapshot,objectFetcher);
    HeapTablePrinter tablePrinter = new HeapTablePrinter(heapSizer);
    LeakSuspectsFinder leakSuspectsFinder = new LeakSuspectsFinder(snapshot, listener, heapSizer);
    ThreadFinder threadFinder = new ThreadFinder(snapshot, objectFetcher);
    KnownObjectProvider knownObjectProvider = new KnownObjectProvider(snapshot, listener, threadFinder, new KnownObjectExtensionResolver().getExtensions());
    ThreadPrinter threadPrinter = new ThreadPrinter(threadFinder, heapSizer);
    LeakSuspectPrinter leakSuspectPrinter = new LeakSuspectPrinter(leakSuspectsFinder, knownObjectProvider, tablePrinter, heapSizer);
    resultsBuilder = new ResultsBuilder(heapSizer, tablePrinter, knownObjectProvider, leakSuspectsFinder,
            threadFinder, threadPrinter, leakSuspectPrinter, getKnownObjectExtensionsToReport());
    if (cmd.hasOption(CLI_FLAG)) {
      try (Scanner scanner = new Scanner(System.in, PrintUtils.DEFAULT_CHARSET)) {
        CommandExecutor commandExecutor = new CommandExecutor(snapshot, listener, scanner, knownObjectProvider, objectFetcher, resultsBuilder, threadFinder, threadPrinter, heapSizer);
        ConsoleController matConsoleController = new ConsoleController(commandExecutor, scanner);
        matConsoleController.execute();
        System.out.println("Thank you for using CLIMAT :D");
        return;
      }
    }
    
    try (
        FileOutputStream fos = new FileOutputStream(outFileName, false);
        OutputStreamWriter resultsFileWriter = new OutputStreamWriter(fos, PrintUtils.DEFAULT_CHARSET);
        BufferedWriter resultsBufferedWriter = new BufferedWriter(resultsFileWriter);
        PrintWriter resultsPrintWriter = new PrintWriter(resultsBufferedWriter)) {

      resultsPrintWriter.println(snapshot.getSnapshotInfo().getPath());
      if (objectAddressInput != null) {
        resultsPrintWriter.println(handleObjectInput(objectFetcher, objectAddressInput));
      } else {
        resultsPrintWriter.println(resultsBuilder.printFullReport(true));
      }
    } catch (IOException ioException) {
      System.err.println("ERROR: Exception writing to results file:");
      ioException.printStackTrace();
      return;
    } finally {
      if (snapshot != null) {
        snapshot.dispose();
      }
    }
    System.out.println();
    System.out.println("Analysis completed and saved to " + outFileName);
  }

  @Override public void stop() {
  }

  private List<KnownObjectResolver> getKnownObjectExtensionsToReport() {
    return (new KnownObjectExtensionResolver()).getExtensions().stream().filter(KnownObjectResolver::doBreakdownInDefaultReport).collect(Collectors.toList());
  }

  private String handleObjectInput(ObjectFetcher objectFetcher, String objectAddressInput) {
    TextStringBuilder output = new TextStringBuilder();
    output.appendln(PrintUtils.printNote("Object ID flag passed"));
    List<IObject> objects = new ArrayList<>();
    for (String address : objectAddressInput.split(",")) {
      try {
        objects.add(objectFetcher.getObjectFromHexAddress(address));
      } catch (NumberFormatException numberFormatException) {
        //listener.sendUserMessage is not working in execute
        System.err.println("ERROR: Input " + address + " is not a valid hexadecimal number.");
        numberFormatException.printStackTrace();
      } catch (SnapshotException snapshotException) {
        System.err.println("Error processing address " + address + ":");
        snapshotException.printStackTrace();
      }
    }
    output.append(resultsBuilder.printManyObjectsInfo(objects));
    return output.toString();
  }

  private String getNowAsString() {
    LocalDateTime nowDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss");
    return nowDateTime.format(formatter);
  }
}