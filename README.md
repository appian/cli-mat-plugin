1. [Project Overview](#project-overview)
2. [How To Run](#usage)
3. [Build Steps](#build-Steps)
4. [Extending CLIMAT](#extending-climat)
7. [General Considerations](#general-considerations)

# Project Overview
CLIMAT is a command line HPROF analyzer built on Eclipse MAT & supporting:
 - Leak Suspects Report
 - Reference Navigation (Incoming & Outgoing by Object or Class)
 - Dominator Tree
 - Class Histogram
 - OQL Querying
 - Batched navigation of Arrays, Collections, Strings
 - Much more

This project consists of an Eclipse MAT plugins built using Eclipse Tycho, which is a manifest-first way to build Eclipse plug-ins/OSGi bundles.  
CliMatPlugin is the core module of the project, containing all the logic to support CLI navigation of HPROFs.  
CliMatPlugin provides one [Eclipse Extension Point](https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.pde.doc.user%2Fconcepts%2Fextension.htm), [KnownObjectResolver](CliMatPlugin/plugin.xml), which can be extended to provide custom analyses of relevant objects.

## Usage
After building CLIMAT, execute from the command line using the following command:  
`mat/MemoryAnalyzer -application CliMatPlugin.execute <HPROF_FILE>`  
The results file is generated in the location of the HPROF.  
The default report provides Thread statistics, executes the Leak Suspects Report, and reports analyses of knownObjectResolver extensions.  
When advanced traversal is needed, the -c CLI flag should be used. 

#### Flags
*-c -cli*: Opens the CLI instead of the static results file. The CLI is meant as a complete replacement and expansion of Eclipse MAT.  
*-i -id*: Comma-separated list of hexadecimal Object IDs to analyze. This flag will bypass the default report, and only provide details on the objects requested.

## Build Steps
To compile the plugin locally, the 'org.eclipse.mat.api*.jar' JAR that comes in the plugins directory of Eclipse MAT must added as a dependency.  
This project uses a nested POM structure to support the requirements of OSGi. Each module serves a distinct purpose in the build process.  
The use of each plugin is explained with a comment.  

0. Download & unzip the latest [Eclipse MAT](https://www.eclipse.org/mat/downloads.php) for your OS directly into /mat.   
1. `mvn package`
    1. CliMatDependencies builds the local dependency JAR/OSGi bundle. 
    2. CliMatPlugin builds the core Eclipse Plugin to a JAR/OSGi bundle.
    4. CliMatFeature is an Eclipse Feature bundle containing CliMatPlugin (and any custom extension bundles you wish to add).
    5. CliMatRepository installs CliMatFeature into a local zipped P2 Repository.
    6. exec-maven-plugin in CliMatRepository/pom.xml uninstalls and reinstalls CliMatFeature to the OS-appropriate Eclipse MAT installation in local mat/ directory. 

#### Installation into Eclipse MAT
These actions are performed automatically by CliMatRepository/pom.xml, so you don't need to run them manually. These steps are here for reference only.
1. Install the plugin into MAT via the repository
    1. If the plugin is already installed in MAT, it must be uninstalled before installing a new version:  
        - (Windows) *MemoryAnalyzerc.exe -application org.eclipse.equinox.p2.director -uninstallIU CliMatPlugin*  
        - (Unix) *MemoryAnalyzer -application org.eclipse.equinox.p2.director -uninstallIU CliMatPlugin*  
    2. Install
        - (Windows) `MemoryAnalyzerc.exe -application org.eclipse.equinox.p2.director -repository "jar:file:<PATH_TO_PLUGIN>!/" -installIU CliMatPlugin`  
        - (Unix) `MemoryAnalyzer -application org.eclipse.equinox.p2.director -repository "jar:file:<PATH_TO_PLUGIN>%21/" -installIU CliMatPlugin`  
            - '%21' is used to encode '!' while avoiding the complications of the '!' Unix expansion.  
            - This may also be required on Windows depending on the CLI used.  

##### Cleanup
Eclipse MAT does not automatically clean up old plugin installations :(  
However, I have worked around this so that `mvn clean` is still effective and sufficient.
- The Maven Clean Plugin in the parent pom cleans up logs that can be easily specified.
- clean.sh in the project root directory, triggered by exec-maven-plugin, performs additional cleanup that is too complicated for the Clean Plugin. 

#### Adding Dependencies
1. Add the new dependency in the <dependencies> section of CliMatDependencies/pom.xml
2. Follow the complete Build Steps.  
- CliMatDependencies/pom.xml uses maven-bundle-plugin to package all dependencies into a JAR in CliMatDependencies/target  
- CliMatPlugin/pom.xml uses maven-dependency-plugin to copy the dependencies JAR into CliMatPlugin/target/dependency and add it to the classpath.

#### Building from a custom Eclipse MAT Zip
By default, this project downloads and builds from an official Eclipse MAT release as defined by the concatenation of [CI variables](.gitlab-ci.yml) `MAT_DOWNLOAD_URL` & `MAT_LINUX_ZIP`.  
In rare cases, you may want to build Eclipse MAT locally instead and upload the zip for CLIMAT to leverage. This should only be pursued if you cannot wait for the next Eclipse MAT release, e.g. for a CVE SLA in Eclipse Core.  
To build a custom Eclipse MAT zip:
1. First, check the [Eclipse MAT product page](https://projects.eclipse.org/projects/tools.mat) to ensure that the desired fix isn't already resolved in a newer version of MAT.
2. If patching a CVE, Check Google and the [MAT bug forum](https://bugs.eclipse.org/bugs/buglist.cgi?component=Core&list_id=21366461&product=MAT&resolution=---) to see if it has been reported and/or discussed yet. If not, report it ([example](https://bugs.eclipse.org/bugs/show_bug.cgi?id=582260)).
3. If applying a fixed-but-unreleased CVE within Eclipse, identify the latest Eclipse I-Build which includes the fix:
   1. Identify the in-progress (next upcoming e.g. Latest Release +1) [Eclipse Platform version here](https://download.eclipse.org/eclipse/downloads/).
   2. Identify the specific I-Build you want to use from the Update site for the above Eclipse Platform version (example link of all I-Builds for Eclipse 4.29: https://download.eclipse.org/eclipse/updates/4.29-I-builds/).
   3. To confirm that the I-Build has patched the vulnerable library, navigate to the plugins directory, which corresponds to the same directory in Eclipse MAT (e.g. https://download.eclipse.org/eclipse/updates/4.32-I-builds/I20240304-0140/plugins/)
4. [Build MAT](https://wiki.eclipse.org/MemoryAnalyzer/Building_MAT_With_Tycho#Building_MAT_Standalone_RCPs_from_an_Existing_MAT_Update_Site) on the target OS:
   1. If building a Linux ZIP, perform these steps on Linux only. Building the Linux ZIP on a Windows machine will give the target ZIP improper Unix write permission, which will break CI.
   2. `git clone git@github.com:eclipse-mat/mat.git`
   4. If building from the MAT-default Eclipse build (e.g. fixing a bug in MAT rather than Eclipse Core):
      1. `cd parent`
      2. `mvn clean install -P build-release-rcp`
   5. If building from a custom Eclipse build using the previous step:
      1. Create a new product definition off of the latest release: `cp org.eclipse.mat.product/mat-<date>.p2.inf org.eclipse.mat.product/mat-<date>i.p2.inf`
      2. Create a new target platform off of the latest release: `cp org.eclipse.mat.targetdef/mat-<date>.target org.eclipse.mat.targetdef/mat-<date>i.target`
      3. Edit `mat-<date>i.target` to update the target platform Eclipse repository to match the I-Build repo, e.g.: `<repository location="https://download.eclipse.org/eclipse/updates/4.32-I-builds/"/>`
      5. `cd parent`
      6. Build Mat using your new custom target: `mvn clean install -P build-release-rcp -Dmat-target=mat-<date>i`
   6. The built zip will write to `<mat_src>/org.eclipse.mat.product/target/products/org.eclipse.mat.ui.rcp.MemoryAnalyzer-linux.gtk.x86_64.zip`

## Extending CLIMAT
CLIMAT can be extended to provide custom analyses of objects unique to your organization.  
This is especially valuable if your heap issues are frequently caused by the same classes, with relevant root cause information in predictable heap objects.

### Creating an extension
`ExampleExtensionPlugin` is an example module which includes all the required code to make an extension.
1. Replace the module with your desired name and uncomment the references:
   1. [Parent pom](pom.xml) modules list
   2. [pom.xml](ExampleExtensionPlugin/pom.xml) 
   3. [META-INF/MANIFEST.MF](ExampleExtensionPlugin/META-INF/MANIFEST.MF)
   4. [CliMatFeature/pom.xml](CliMatFeature/pom.xml)
   5. [CliMatFeature/feature.xml](CliMatFeature/feature.xml)
2. Implement your extension by following the below steps to add new KnownObjects and/or resolve objects' names, using the [example KnownObject](ExampleExtensionPlugin/src/main/java/com/appiansupport/example/resolvers/ExampleKnownObjectResolver.java) and [Name Resolver](ExampleExtensionPlugin/src/main/java/com/appiansupport/example/resolvers/ExampleDocumentNameResolver.java) as guides.
3. Update [plugin.xml](ExampleExtensionPlugin/plugin.xml) to include your extensions
4. Build CLIMAT Your plugin should now build and deploy via CliMatFeature! The CLIMAT [KnownObjectExtensionResolver](CliMatPlugin/src/main/java/com/appiansupport/mat/resolvers/KnownObjectExtensionResolver.java) will find extensions at runtime and automatically use them for analyses.

### Introduction to [Eclipse MAT API](https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.mat.ui.help%2Fdoc%2Findex.html&cp%3D58)
[Repo URL](https://git.eclipse.org/r/mat/org.eclipse.mat)  
[Browser Link](https://git.eclipse.org/c/mat/org.eclipse.mat)  
There are 3 plugin packages you are likely to interact with:
1. org.eclipse.mat.api holds the primary API code.
2. org.eclipse.mat.report defines interfaces for many of the data types returned by the API. 
3. org.eclipse.mat.parser is private, but it holds the implementations of all the relevant objects.

Here are the must-know objects:  
1. [ISnapshot](https://help.eclipse.org/latest/topic/org.eclipse.mat.ui.help/doc/org/eclipse/mat/snapshot/ISnapshot.html) is the top-level representation the Heap dump.  
    - Many key functions are contained here, so this object is passed through the majority of CLIMAT.
2. [IObject](https://help.eclipse.org/latest/topic/org.eclipse.mat.ui.help/doc/org/eclipse/mat/snapshot/model/IObject.html) represents an object.
    - Most non-primitives are represented as IObjects.
    - resolveValue() is used to traverse object paths.
3. [ClassHistogramRecord](https://help.eclipse.org/latest/topic/org.eclipse.mat.ui.help/doc/org/eclipse/mat/snapshot/ClassHistogramRecord.html) represents the instances of a given class. 
4. [IProgressListener](https://help.eclipse.org/latest/topic/org.eclipse.mat.ui.help/doc/org/eclipse/mat/util/IProgressListener.html) is used to track the progress of MAT API operations, and is required throughout the API.

### Implementation Highlights
1. [Executor](CliMatPlugin/src/main/java/com/appiansupport/mat/Executor.java) is the entry point to CLIMAT.
2. [ResultsBuilder](CliMatPlugin/src/main/java/com/appiansupport/mat/ResultsBuilder.java) is the entry point to the static report, & defines how the sections are printed.
3. [ThreadFinder](CliMatPlugin/src/main/java/com/appiansupport/mat/ThreadFinder.java) loads Threads and Thread dumps from the .threads file.
4. [KnownObject](CliMatPlugin/src/main/java/com/appiansupport/mat/knownobjects/KnownObject.java) defines specific analysis and printing behavior for a specific common object.
5. [ConsoleController](CliMatPlugin/src/main/java/com/appiansupport/mat/console/ConsoleController.java) is the entry point to the CLI, controlling the CLI flow.
6. [ConsoleCommand](CliMatPlugin/src/main/java/com/appiansupport/mat/console/command/ConsoleCommand.java) represents a CLI option available to the user. Executing a ConsoleCommand returns a ConsoleState.
7. [ConsoleState](CliMatPlugin/src/main/java/com/appiansupport/mat/console/state/ConsoleState.java) represents the current CLI state and stores the list of available options (ConsoleCommands). 
    1. Extend [NoHistoryState](CliMatPlugin/src/main/java/com/appiansupport/mat/console/state/NoHistoryState.java) instead if you don't want your state stored on the history Stack.
    2. Extend [NullState](CliMatPlugin/src/main/java/com/appiansupport/mat/console/state/NullState.java) instead to add SuggestedCommands without modifying the state Stack. 
8. [CommandExecutor](CliMatPlugin/src/main/java/com/appiansupport/mat/console/CommandExecutor.java) is the entry point for CLI command execution. A single instance is maintained and passed through the CLI flow. 
9. [ListManager](CliMatPlugin/src/main/java/com/appiansupport/mat/console/listmanager/ListManager.java) defines the CLI behavior (choosing, printing, searching, sorting) for a list of a given type.

### Adding a new KnownObject 
1. Create a new class extending [KnownObject](CliMatPlugin/src/main/java/com/appiansupport/mat/knownobjects/KnownObject.java).
2. Create a new Resolver extending [KnownObjectResolver](CliMatPlugin/src/main/java/com/appiansupport/mat/resolvers/KnownObjectResolver.java).
   1. The init() method should create an instance of your KnownObject.
   2. The resolve() method determines whether an IObject is an instance of your KnownObject.
3. Add your resolver impl to plugin.xml. Instances of your KnownObject will now automatically be analyzed with your custom code.

### Resolving an object's name
Use [Eclipse MAT's Name Resolver Extension](https://help.eclipse.org/latest/topic/org.eclipse.mat.ui.help/doc/org_eclipse_mat_api_nameResolver.html) when you want to define how an object will be displayed.  
Specifically, this controls what is returned by IObject.getClassSpecificName().
1. Create an implementation of org.eclipse.mat.snapshot.extension.IClassSpecificNameResolver.
2. Use the @Subject annotation to define the target class, and @Override the resolve() method to define your naming logic.
3. Define your class as an extension point in plugin.xml, as shown in the API docs linked above.

#### Using Key Results
Every KnownObject holds a keyResults map, which represents the most relevant details that should be aggregated between objects.
- Use [KeyResultsBuilder](CliMatPlugin/src/main/java/com/appiansupport/mat/knownobjects/KeyResultBuilder.java) to aggregate keyResults maps between any objects you wish.
    - See [ResultsBuilder](CliMatPlugin/src/main/java/com/appiansupport/mat/ResultsBuilder.java) for examples of this.

### Adding a Suggestion
1. Initialize a new [SuggestedCommand](CliMatPlugin/src/main/java/com/appiansupport/mat/console/command/SuggestedCommand.java).
2. Add the SuggestedCommand to the desired state via ConsoleState.addSuggestedCommand() or ConsoleState.addSuggestedCommands().
3. For an example, see [CommandExecutor.getSuggestedOptionsFromLeakSuspects](CliMatPlugin/src/main/java/com/appiansupport/mat/console/CommandExecutor.java#L523).

#### Creating a new CLI Flow
1. Implement the command logic in [CommandExecutor](CliMatPlugin/src/main/java/com/appiansupport/mat/console/CommandExecutor.java).
2. Create a class extending [ConsoleCommand](CliMatPlugin/src/main/java/com/appiansupport/mat/console/command/ConsoleCommand.java).
   1. toString() defines the text of the option.
   2. execute() should execute the desired method in [CommandExecutor](CliMatPlugin/src/main/java/com/appiansupport/mat/console/CommandExecutor.java).
3. Instantiate your [ConsoleCommand](CliMatPlugin/src/main/java/com/appiansupport/mat/console/command/ConsoleCommand.java) and add it to the options list in the [ConsoleStates](CliMatPlugin/src/main/java/com/appiansupport/mat/console/state/ConsoleState.java) where you want your command available.

### Testing
### Remote Debugging
1. Configure remote debugging in mat/MemoryAnalyzer.ini.  
For example:
`-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005`  
2. Configure your IDE to attach to the remote JVM on the configured port.

### Writing Tests
Real ISnapshot objects cannot be instantiated unless the parent Class has the appropriate context, e.g. the Class is defined as an Eclipse extension point in plugin.xml.  
Therefore, mocking ISnapshot is recommended for testing.

## General Considerations
HPROF navigation features are consistent in naming with Eclipse MAT.  
However, this project deviates from Eclipse MAT and should not be expected to always align with it.  
Eclipse MAT API is made for extending the GUI. Therefore, this project works around much of the API to achieve its goal.  
For example, most extensions defined on the [API home page](https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.mat.ui.help%2Fdoc%2Findex.html&cp%3D58) are ignored.
