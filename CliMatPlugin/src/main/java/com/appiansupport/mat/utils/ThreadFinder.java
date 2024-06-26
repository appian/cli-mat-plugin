package com.appiansupport.mat.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.appiansupport.mat.utils.internal.ObjectFetcher;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;

/**
 * Loads and stores Threads and Thread dumps from the .threads file. Also identifies the OOM Thread if present.
 * Stores int objectId to optimize Memory usage.
 */
public class ThreadFinder {
  private final ISnapshot snapshot;
  private final Map<Integer, String> threadIdToStackTrace;
  private final Map<IObject, Set<IObject>> objectToThreads;
  private final ObjectFetcher objectFetcher;
  private boolean sorted = false;
  private Integer oomThreadId;
  private List<Integer> allThreadIds;
  private List<IObject> allThreadObjects;


  public ThreadFinder(ISnapshot snapshot, ObjectFetcher objectFetcher) {
    this.snapshot = snapshot;
    this.objectFetcher = objectFetcher;
    objectToThreads = new HashMap<>();
    threadIdToStackTrace = new HashMap<>();
  }

  private String getThreadsFileName() {
    final String THREADS_FILE_EXTENSION = "threads";
    return snapshot.getSnapshotInfo().getPrefix() + THREADS_FILE_EXTENSION;
  }

  private List<Integer> parseThreadIdsAndStackFromFile() {
    if (allThreadIds != null) {
      return allThreadIds;
    }
    allThreadIds = new ArrayList<>();
    String fileName = getThreadsFileName();
    final File f = new File(fileName);
    if (!f.exists()) {
      System.err.println("Unable to find .threads file; cannot parse Threads and stack traces");
      return allThreadIds;
    }
    final String charset = PrintUtils.DEFAULT_CHARSET;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset))) {
      final Pattern threadOrOomRegex = Pattern.compile("^Thread (?<id>.*)|java\\.lang\\.OutOfMemoryError");
      String line;
      Integer lastThreadId = null;
      TextStringBuilder currentStackTrace = new TextStringBuilder();
      boolean isReadingStackTrace = false;
      while (((line = br.readLine()) != null)) {
        Matcher matcher = threadOrOomRegex.matcher(line);
        if (matcher.find()) {
          String threadAddress = matcher.group("id");
          if (threadAddress != null) {
            try {
              int threadId = objectFetcher.getIdFromHexAddress(threadAddress);
              allThreadIds.add(lastThreadId = threadId);
              isReadingStackTrace = true;
              continue;
            } catch (NumberFormatException | SnapshotException readAddressException) {
              System.err.println("Unable to parse Thread " + threadAddress + " from .threads file");
              readAddressException.printStackTrace();
            }
          } else {
            //Thread address is null, so we matched OutofMemoryError
            oomThreadId = lastThreadId;
          }
        }
        if (isReadingStackTrace) {
          if (line.isEmpty()) {
            isReadingStackTrace = false;
            threadIdToStackTrace.put(lastThreadId, currentStackTrace.toString());
            currentStackTrace.clear();
          } else {
            currentStackTrace.appendln(line);
          }
        }
      }
    } catch (FileNotFoundException fileNotFoundException) {
      System.err.printf("Threads file %s not found%n", fileName);
    } catch (UnsupportedEncodingException unsupportedEncodingException) {
      System.err.printf("Error opening threads file %s with encoding %s%n", fileName, charset);
    } catch (IOException ioException) {
      System.err.println("Error opening threads file " + fileName);
      ioException.printStackTrace();
    }
    return allThreadIds;
  }

  private List<IObject> getThreadObjectsFromIds() {
    if (allThreadObjects != null) {
      return allThreadObjects;
    }
    if (allThreadIds == null) {
      parseThreadIdsAndStackFromFile();
    }
    List<IObject> allThreads = new ArrayList<>(allThreadIds.size());

    for (int threadId : allThreadIds) {
      try {
        IObject threadObject = snapshot.getObject(threadId);
        allThreads.add(threadObject);
      } catch (SnapshotException snapshotException) {
        System.err.println("Error resolving Thread id " + threadId + " from .threads file");
        snapshotException.printStackTrace();
      }
    }
    return this.allThreadObjects = allThreads;
  }

  /**
   * @return List of thread IObjects sorted descending by retained Heap
   */
  public List<IObject> sortThreadsByHeapUsage() {
    if (allThreadObjects == null) {
      getThreadObjectsFromIds();
    }
    if (!sorted) {
      allThreadObjects.sort(Comparator.comparing(IObject::getRetainedHeapSize).reversed());
      sorted = true;
    }
    return new ArrayList<>(allThreadObjects);
  }

  /**
   * @param object The IObject to retrieve mapped threads of
   * @return The Set of Thread IObjects identified as related to this Object.
   */
  public Set<IObject> getThreadsMappedToObject(IObject object) {
    return objectToThreads.getOrDefault(object, new HashSet<>());
  }

  /**
   * If finding referenced Threads is an expensive operation, this mapping can be used as storage to avoid recalculation
   * @param object The IObject to add a mapped Thread to
   * @param thread The Thread IObject to map to the target IObject.
   */
  public void mapObjectToThread(IObject object, IObject thread) {
    Set<IObject> threads = new HashSet<>();
    threads.add(thread);
    mapObjectToThreads(object, threads);
  }

  /**
   * If finding referenced Threads is an expensive operation, this mapping can be used as storage to avoid recalculation
   * @param object The IObject to add a mapped Thread to
   * @param threads The Thread IObjects to map to the target IObject.
   */
  public void mapObjectToThreads(IObject object, Set<IObject> threads) {
    objectToThreads.put(object,threads);
  }

  /**
   * @param objectId Object id of the target
   * @return whether or not the object is a thread (present in the .threads file)
   */
  public boolean isThread(int objectId) {
    if (allThreadIds == null) {
      parseThreadIdsAndStackFromFile();
    }
    return allThreadIds.contains(objectId);
  }

  /**
   * @return A list of object IDs of all threads (present in the .threads file)
   */
  public List<Integer> getAllThreadIds() {
    return parseThreadIdsAndStackFromFile();
  }

  /**
   * @return A list of IObject representations of all threads (present in the .threads file)
   */
  public List<IObject> getAllThreads() {
    return getThreadObjectsFromIds();
  }

  /**
   * @return The thread object which threw OOM, or null if there isn't one
   */
  public Optional<IObject> getOomThread() {
    if (allThreadIds == null) {
      parseThreadIdsAndStackFromFile();
    }
    if(oomThreadId != null) {
      try {
        return Optional.ofNullable(snapshot.getObject(oomThreadId));
      } catch (SnapshotException snapshotException) {
        System.err.println("Error processing OutOfMemoryError Thread address: ");
        snapshotException.printStackTrace();
      }
    }
    return Optional.empty();
  }

  /**
   * @param threadId the Object id of the target thread
   * @return The stack trace of the thread as found in the .threads file, or null if the thread was not present in the .threads file. s
   */
  public String getStackTrace(int threadId) {
    if (allThreadIds == null) {
      parseThreadIdsAndStackFromFile();
    }
    return threadIdToStackTrace.get(threadId);
  }
}