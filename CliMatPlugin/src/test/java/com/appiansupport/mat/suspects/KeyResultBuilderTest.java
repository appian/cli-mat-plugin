package com.appiansupport.mat.suspects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KeyResultBuilderTest {

  @Test void addKeyResults_null_npe() {
    KeyResultBuilder krb = new KeyResultBuilder();
    assertThrows(NullPointerException.class, () -> krb.addKeyResults(null));
  }

  @Test void addKeyResults_nullSet_npe() {
    KeyResultBuilder krb = new KeyResultBuilder();
    Map<String, Set<String>> testInput = new HashMap<>();
    testInput.put("",null);
    assertThrows(NullPointerException.class, () -> krb.addKeyResults(testInput));
  }

  @Test void addKeyResults_emptySet_noChange() {
    KeyResultBuilder krb = new KeyResultBuilder();
    Map<String, Set<String>> testInput = new HashMap<>();
    testInput.put("",new HashSet<>());
    assertTrue(krb.addKeyResults(testInput).isEmpty());
  }


  @Test void addKeyResults_singleResult_value1() {
    KeyResultBuilder krb = new KeyResultBuilder();
    Map<String, Set<String>> testInput = new HashMap<>();
    HashSet<String> testValue = new HashSet<>();
    testValue.add("value");
    testInput.put("key",testValue);
    assertEquals(1,krb.addKeyResults(testInput).get("key").get("value"));
  }

  @Test void addKeyResults_sameResultTwice_value2() {
    KeyResultBuilder krb = new KeyResultBuilder();
    Map<String, Set<String>> testInput = new HashMap<>();
    HashSet<String> testValue = new HashSet<>();
    testValue.add("value");
    testInput.put("key",testValue);
    krb.addKeyResults(testInput);
    assertEquals(2,krb.addKeyResults(testInput).get("key").get("value"));
  }

  @Test void addKeyResults_2valuesSameKey_2values() {
    KeyResultBuilder krb = new KeyResultBuilder();
    Map<String, Set<String>> testInput = new HashMap<>();
    HashSet<String> testValue = new HashSet<>();
    testValue.add("value1");
    testValue.add("value2");
    testInput.put("key",testValue);
    assertEquals(2,krb.addKeyResults(testInput).get("key").keySet().size());
  }

  @Test void printKeyResults_belowThreshold_emptyPrint() {
    KeyResultBuilder krb = new KeyResultBuilder();
    Map<String, Set<String>> testInput = new HashMap<>();
    HashSet<String> testValue = new HashSet<>();
    testValue.add("value");
    testInput.put("key",testValue);
    krb.addKeyResults(testInput);
    assertEquals("",krb.printKeyResults(2));
  }

  @Test void printKeyResults_atThreshold_printLine() {
    KeyResultBuilder krb = new KeyResultBuilder();
    String testKey="key";
    Map<String, Set<String>> testInput = new HashMap<>();
    HashSet<String> testValue = new HashSet<>();
    testValue.add("value");
    testInput.put(testKey,testValue);
    krb.addKeyResults(testInput);
    assertTrue(krb.printKeyResults(1).contains(testKey));
  }

  @Test void printKeyResults_2Rows_printGreaterFirst() {
    KeyResultBuilder krb = new KeyResultBuilder();
    String testKey1="keyWithCount1";
    String testKey2="keyWithCount2";

    Map<String, Set<String>> testInput1 = new HashMap<>();
    Map<String, Set<String>> testInput2 = new HashMap<>();
    HashSet<String> testValue = new HashSet<>();
    testValue.add("value");
    testInput1.put(testKey1,testValue);
    testInput2.put(testKey2,testValue);
    krb.addKeyResults(testInput1);
    krb.addKeyResults(testInput2);
    krb.addKeyResults(testInput2);
    String keyResultsPrinted = krb.printKeyResults(1);
    Pattern k1 = Pattern.compile(testKey1);
    Pattern k2 = Pattern.compile(testKey2);
    Matcher m1 = k1.matcher(keyResultsPrinted);
    Matcher m2 = k2.matcher(keyResultsPrinted);
    m1.find();
    m2.find();
    assertTrue(m1.start()>m2.start());
  }


}