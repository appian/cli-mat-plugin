package com.appiansupport.mat.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrintUtilsTest {

  @Test void printNestedList_Null_Npe() {
    assertThrows(NullPointerException.class,()->PrintUtils.printNestedList(null));
  }

  @Test void printNestedList_Empty_EmptyString() {
    assertEquals("",PrintUtils.printNestedList(new ArrayList<>()));
  }

  @Test void printNestedList_OneRow_AddsNewline() {
    String testData = "Test";
    String result = PrintUtils.printNestedList(Collections.singletonList(testData));
    assertEquals("\n",result.substring(result.length()-1));
  }

  @Test void printTableFromLists_nullData_Npe() {
    assertThrows(NullPointerException.class,()->PrintUtils.printTableFromLists(null,new String[]{},null));
  }

  @Test void printTableFromLists_nullHeaders_Npe() {
    assertThrows(NullPointerException.class,()->PrintUtils.printTableFromLists(new ArrayList<>(),null,null));
  }

  @Test void printTableFromLists_mismatchedHeaders_IllegalArg() {
    assertThrows(IllegalArgumentException.class,()->PrintUtils.printTableFromLists(new ArrayList<>(),new String[]{"Test"},null));
  }

  @Test void printTableFromLists_mismatchedColumns_IllegalArg() {
    List<String> col1Data = Collections.singletonList("Row1");
    List<String> col2Data = Arrays.asList("Row1","Row2");
    List<List<String>> testData=Arrays.asList(col1Data,col2Data);
    String[] testHeaders = {"Test1","Test2"};
    assertThrows(IllegalArgumentException.class,()->PrintUtils.printTableFromLists(testData,testHeaders,null));
  }

  @Test void printTableFromLists_EmptyData_Empty() {
    assertEquals("",PrintUtils.printTableFromLists(new ArrayList<>(),new String[]{},null));
  }

  @Test void printTableFromLists_validDataNullHeader_Works() {
    List<String> col1Data = Collections.singletonList("Row1Col1");
    List<String> col2Data = Collections.singletonList("Row1Col2");
    List<List<String>> testData = Arrays.asList(col1Data,col2Data);
    String[] testHeaders = {"Test1","Test2"};
    assertFalse(PrintUtils.printTableFromLists(testData,testHeaders,null).isEmpty());
  }

  @Test void printTableFromLists_validDataValidHeader_printHeaderFirst() {
    List<String> col1Data = Collections.singletonList("Row1Col1");
    List<String> col2Data = Collections.singletonList("Row1Col2");
    List<List<String>> testData = Arrays.asList(col1Data,col2Data);
    String[] testHeaders = {"Test1","Test2"};
    String borderHeader = "Border Header";
    String tableResult = PrintUtils.printTableFromLists(testData,testHeaders,borderHeader);
    String toplineOfTable = tableResult.split("\n")[0];
    assertTrue(toplineOfTable.contains(borderHeader));
  }

  @Test void printTableFromLists_longBorderHeader_allLinesScale() {
    List<String> col1Data = Collections.singletonList("Row1Col1");
    List<String> col2Data = Collections.singletonList("Row1Col2");
    List<List<String>> testData = Arrays.asList(col1Data,col2Data);
    String[] testHeaders = {"Test1","Test2"};
    String borderHeader = "This is an unusually long Header, and the table length should scale accordingly on every line";
    String tableResult = PrintUtils.printTableFromLists(testData,testHeaders,borderHeader);
    String[] linesOfTable = tableResult.split("\n");
    assertTrue(Arrays.stream(linesOfTable).mapToInt(String::length).allMatch(r->r>=borderHeader.length()));
  }

  @Test void printTableFromLists_longBorderHeader_allLinesEqual() {
    List<String> col1Data = Collections.singletonList("Row1Col1");
    List<String> col2Data = Collections.singletonList("Row1Col2");
    List<List<String>> testData = Arrays.asList(col1Data,col2Data);
    String[] testHeaders = {"Test1","Test2"};
    String borderHeader = "This is an unusually long Header, and the table length should scale accordingly on every line";
    String tableResult = PrintUtils.printTableFromLists(testData,testHeaders,borderHeader);
    String[] linesOfTable = tableResult.split("\n");
    int firstRowLength = linesOfTable[0].length();
    assertTrue(Arrays.stream(linesOfTable).mapToInt(String::length).allMatch(r->r==firstRowLength));
  }

  @Test void printTableFromLists_longColHeader_allLineScale() {
    List<String> col1Data = Collections.singletonList("Row1Col1");
    List<String> col2Data = Collections.singletonList("Row1Col2");
    List<List<String>> testData = Arrays.asList(col1Data,col2Data);
    String longColHeader = "This header is much longer than everything else, and all rows should adjust accordingly";
    String[] testHeaders = {"Test1",longColHeader};
    String borderHeader = "Border Header";
    String tableResult = PrintUtils.printTableFromLists(testData,testHeaders,borderHeader);
    String[] linesOfTable = tableResult.split("\n");
    assertTrue(Arrays.stream(linesOfTable).mapToInt(String::length).allMatch(r->r>=longColHeader.length()));
  }

  @Test void printTableFromLists_longColHeader_allLinesEqual() {
    List<String> col1Data = Collections.singletonList("Row1Col1");
    List<String> col2Data = Collections.singletonList("Row1Col2");
    List<List<String>> testData = Arrays.asList(col1Data,col2Data);
    String longColHeader = "This header is much longer than everything else, and all rows should adjust accordingly";
    String[] testHeaders = {"Test1",longColHeader};
    String borderHeader = "Border Header";
    String tableResult = PrintUtils.printTableFromLists(testData,testHeaders,borderHeader);
    String[] linesOfTable = tableResult.split("\n");
    int firstRowLength = linesOfTable[0].length();
    assertTrue(Arrays.stream(linesOfTable).mapToInt(String::length).allMatch(r->r==firstRowLength));
  }

  @Test void printTableFromLists_longData_allLinesScale() {
    String longDataPoint = "This cell of data is much longer than the others, and all rows should adjust accordingly";
    List<String> col1Data = Collections.singletonList("Row1Col1");
    List<String> col2Data = Collections.singletonList(longDataPoint);
    List<List<String>> testData = Arrays.asList(col1Data,col2Data);
    String[] testHeaders = {"Test1","Test2"};
    String borderHeader = "Border Header";
    String tableResult = PrintUtils.printTableFromLists(testData,testHeaders,borderHeader);
    String[] linesOfTable = tableResult.split("\n");
    assertTrue(Arrays.stream(linesOfTable).mapToInt(String::length).allMatch(r->r>=longDataPoint.length()));
  }

  @Test void printTableFromLists_longData_allLinesEqual() {
    String longDataPoint = "This cell of data is much long than the others, and all rows should adjust accordingly";
    List<String> col1Data = Collections.singletonList("Row1Col1");
    List<String> col2Data = Collections.singletonList(longDataPoint);
    List<List<String>> testData = Arrays.asList(col1Data,col2Data);
    String[] testHeaders = {"Test1","Test2"};
    String borderHeader = "Border Header";
    String tableResult = PrintUtils.printTableFromLists(testData,testHeaders,borderHeader);
    String[] linesOfTable = tableResult.split("\n");
    int firstRowLength = linesOfTable[0].length();
    assertTrue(Arrays.stream(linesOfTable).mapToInt(String::length).allMatch(r->r==firstRowLength));
  }
}