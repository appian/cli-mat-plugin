package com.appiansupport.mat.utils;

import com.appiansupport.mat.console.listmanager.TableColumnPrinter;
import com.appiansupport.mat.constants.Messages;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

public class PrintUtils {
  public static final String DEFAULT_CHARSET = "UTF-8";
  public static final String TABLE_PREFIX = "| ";
  public static final String TABLE_SUFFIX = " |";
  public static final String TABLE_DELIM = " | ";
  public static final String TABLE_ENDLINE = "\r\n";
  public static final char TABLE_BORDER = '-';
  public static final char TABLE_FILLER = ' ';
  private static final String SECTION_DELIMITER = "=========";
  private static final String HEADER_DELIMITER = "=====";
  private static final String SUB_HEADER_DELIMITER = "===";
  private static final String NOTE_DELIMITER = "=";

  /**
   * @param content The String to print
   * @return The provided String with a section delimiter
   */
  public static String printSection(String content) {
    return printWithDelimiter(content.toUpperCase(), SECTION_DELIMITER);
  }

  /**
   * @param content The String to print
   * @return The provided String with a header-sized delimiter
   */
  public static String printHeader(String content) {
    return printWithDelimiter(content, HEADER_DELIMITER);
  }

  /**
   * @param content The String to print
   * @return The provided String with a sub-header-sized delimiter
   */
  public static String printSubHeader(String content) {
    return printWithDelimiter(content, SUB_HEADER_DELIMITER);
  }

  /**
   * @param content The String to print
   * @return The provided String with the smallest, single-character delimiter
   */
  public static String printNote(String content) {
    return printWithDelimiter(content, NOTE_DELIMITER);
  }

  private static String printWithDelimiter(String content, String delimiter) {
    return String.format("%s %s %s", delimiter, content, delimiter);
  }

  /**
   * @param content The String to print
   * @return The provided String with an "debug" header
   */
  public static String printDebug(String content) {
    return "DEBUG: " + content;
  }

  /**
   * @param content The String to print
   * @return The provided String with an "unexpected" header
   */
  public static String printUnexpected(String content) {
    return printDebug("[UNEXPECTED] " + content);
  }

  /**
   * Given a list of values, print each on a new line with indentation
   * @param data The list of String data to print
   * @return The indented data
   */
  public static String printNestedList(List<String> data) {
    TextStringBuilder output = new TextStringBuilder();
    for (int i = 0 ; i < data.size() ; i++) {
      output.appendln("%s%s", StringUtils.repeat(Messages.NESTED_PATH_INDENTATION, i), data.get(i));
    }
    return output.toString();
  }

  /**
   * Print the provided values, pluralized with 's' if there are multiple values
   * @param rowName The name of the values
   * @param values The values to print
   * @return The comma-separated values
   */
  public static String printPluralizedCommaSeparatedRow(String rowName, Collection<String> values) {
    if (values == null || values.isEmpty()) {
      return "";
    } else {
      return String.format("%s%s: %s", rowName, values.size() > 1 ? "s" : "", String.join(", ", values));
    }
  }

  /**
   * Print a complete line of the provided values, pluralized with 's' if there are multiple values
   * @param rowName The name of the values
   * @param values The values to print
   * @return A line of the comma-separated values, ending in newline
   */
  public static String printPluralizedCommaSeparatedRowLine(String rowName, Collection<String> values) {
    if (values == null || values.isEmpty()) {
      return "";
    } else {
      return String.format("%s%n", printPluralizedCommaSeparatedRow(rowName, values));
    }
  }

  /**
   * Print a table of T data represented by Lists with no border header.
   * @param records A list of T to print
   * @param headers The column headers to print
   * @param colPrinters The TableColumnPrinters for each column, which define what data of T is printed
   * @param <T> The data type to print
   * @return A String table of the data
   */
  public static <T> String printTableFromList(List<? extends T> records, String[] headers, TableColumnPrinter<T>[] colPrinters) {
    return printTableFromList(records, headers, colPrinters, null);
  }

  /**
   * Print a table of T data represented by Lists. Inspired by org.eclipse.mat.snapshot.Histogram.java
   * @param records A list of T to print
   * @param headers The column headers to print
   * @param colPrinters The TableColumnPrinters for each column, which define what data of T is printed
   * @param borderHeader A border header, which prints at the center of the upper table border, e.g. ---Border Header---
   * @param <T> The data type to print
   * @return A String table of the data
   */
  public static <T> String printTableFromList(List<? extends T> records, String[] headers, TableColumnPrinter<T>[] colPrinters, String borderHeader) {
    if (headers.length != colPrinters.length) {
      throw new IllegalArgumentException(String.format("Unable to generate table due to data mismatch: %d headers provided for %d column printers", headers.length, colPrinters.length));
    }
    int[] columnLengths = new int[headers.length];
    for (int i = 0; i < headers.length; i++) {
      columnLengths[i] = headers[i].length();
    }
    //Use the longest cell of every column to size columns
    for (T record : records) {
      for (int k = 0; k < colPrinters.length; k++) {
        int lengthOfCelljk = colPrinters[k].getData(record).length();
        if (lengthOfCelljk > columnLengths[k]) {
          columnLengths[k] = lengthOfCelljk;
        }
      }
    }

    final int rowLength = (Arrays.stream(columnLengths).sum()) + (TABLE_DELIM.length() * (columnLengths.length - 1)) + TABLE_PREFIX.length() + TABLE_SUFFIX.length();
    StringBuilder report = new StringBuilder(records.size() * rowLength);

    //fill headers
    //appendStringAndFillUp(report, null, TABLE_BORDER, rowLength);
    centerStringAndFillUp(report, borderHeader, TABLE_BORDER,rowLength);
    report.append(TABLE_ENDLINE);
    report.append(TABLE_PREFIX);
    appendStringAndFillUp(report, headers[0], TABLE_FILLER, columnLengths[0]);
    for (int l = 1; l < headers.length; l++) {
      report.append(TABLE_DELIM);
      appendStringAndFillUp(report, headers[l], TABLE_FILLER, columnLengths[l]);
    }
    report.append(TABLE_SUFFIX + TABLE_ENDLINE);
    appendStringAndFillUp(report, null, TABLE_BORDER, rowLength);
    report.append(TABLE_ENDLINE);

    //Fill data
    for (T record : records) {
      //left-align first column & right-align the rest
      report.append(TABLE_PREFIX);
      appendStringAndFillUp(report, colPrinters[0].getData(record), TABLE_FILLER, columnLengths[0]);
      for (int n = 1; n < headers.length; n++) {
        report.append(TABLE_DELIM);
        appendPreFillAndString(report, colPrinters[n].getData(record), TABLE_FILLER, columnLengths[n]);
      }
      report.append(TABLE_SUFFIX + TABLE_ENDLINE);
    }

    appendStringAndFillUp(report, null, TABLE_BORDER, rowLength);
    report.append(TABLE_ENDLINE);
    return report.toString();
  }

  /**
   * Print a table of data represented by Lists
   * @param data A List of String Lists, representing columns and rows of data
   * @param headers The column headers to print
   * @param borderHeader A border header, which prints at the center of the upper table border, e.g. ---Border Header---
   * @return A String table of the data
   */
  public static String printTableFromLists(List<? extends List<String>> data, String[] headers, String borderHeader) {
    if (headers.length != data.size()) {
      throw new IllegalArgumentException(String.format("Unable to generate table due to data mismatch: %d headers provided for %d columns", headers.length, data.size()));
    }
    if (data.isEmpty()) {
      return "";
    }
    final int numRows = data.get(0).size();
    if (numRows == 0) {
      return "";
    }
    if (!(data.stream().skip(1).allMatch(r -> r.size() == numRows))) {
      throw new IllegalArgumentException("Unable to generate table due to data mismatch: columns have different row counts");
    }
    int[] columnLengths = new int[headers.length];
    for (int i = 0; i < headers.length; i++) {
      columnLengths[i] = headers[i].length();
    }
    //Use the longest cell of every column to size columns
    int col = 0;
    for (List<String> column : data) {
      columnLengths[col] = Math.max(columnLengths[col], column.stream().mapToInt(String::length).max().orElse(0));
      col++;
    }

    final int maxDataRowLength = Arrays.stream(columnLengths).sum() + (TABLE_DELIM.length() * (columnLengths.length - 1)) + TABLE_PREFIX.length() + TABLE_SUFFIX.length();
    final int maxBorderRowLength = borderHeader == null ? 0 : (TABLE_BORDER + TABLE_FILLER + borderHeader + TABLE_FILLER + TABLE_BORDER).length();
    final int rowLength = Math.max(maxBorderRowLength, maxDataRowLength);

    //If the borderHeader is longer than all columns, add the remainder to the last column for alignment
    if (maxBorderRowLength > maxDataRowLength) {
      columnLengths[columnLengths.length-1] += maxBorderRowLength - maxDataRowLength;
    }

    StringBuilder report = new StringBuilder();

    //fill headers
    //appendStringAndFillUp(report, null, TABLE_BORDER, rowLength);
    centerStringAndFillUp(report, borderHeader, TABLE_BORDER, rowLength);
    report.append(TABLE_ENDLINE);
    report.append(TABLE_PREFIX);
    appendStringAndFillUp(report, headers[0], TABLE_FILLER, columnLengths[0]);
    for (int l = 1; l < headers.length; l++) {
      report.append(TABLE_DELIM);
      appendStringAndFillUp(report, headers[l], TABLE_FILLER, columnLengths[l]);
    }
    report.append(TABLE_SUFFIX + TABLE_ENDLINE);
    appendStringAndFillUp(report, null, TABLE_BORDER, rowLength);
    report.append(TABLE_ENDLINE);

    //Fill data
    for (int r = 0 ; r < numRows ; r++) {
      //left-align first column & right-align the rest
      report.append(TABLE_PREFIX);
      appendStringAndFillUp(report, data.get(0).get(r), TABLE_FILLER, columnLengths[0]);
      for (int c = 1; c < headers.length; c++) {
        report.append(TABLE_DELIM);
        appendPreFillAndString(report, data.get(c).get(r), TABLE_FILLER, columnLengths[c]);
      }
      report.append(TABLE_SUFFIX + TABLE_ENDLINE);
    }
    appendStringAndFillUp(report, null, TABLE_BORDER, rowLength);
    report.append(TABLE_ENDLINE);
    return report.toString();
  }

  private static void appendStringAndFillUp(StringBuilder report, String string, char character, int completeLength) {
    if (string != null) {
      report.append(string);
    }
    if (string != null) {
      completeLength -= string.length();
    }
    if (completeLength > 0) {
      for (int i = 0; i < completeLength; i++) {
        report.append(character);
      }
    }
  }

  private static void appendPreFillAndString(StringBuilder report, String string, char character, int completeLength) {
    if (string != null) {
      completeLength -= string.length();
    }
    if (completeLength > 0) {
      for (int i = 0; i < completeLength; i++) {
        report.append(character);
      }
    }
    if (string != null) {
      report.append(string);
    }
  }

  private static void centerStringAndFillUp(StringBuilder report, String string, char character, int completeLength) {
    if (string == null) {
      string = "";
    } else {
      string = " " + string + " ";
    }
    int fillLength = completeLength - string.length();
    int preFillLength = fillLength / 2 + ( fillLength % 2 != 0 ? 1 : 0 );
    int postFillLength = fillLength / 2;
    report.append(StringUtils.repeat(character, preFillLength));
    report.append(string);
    report.append(StringUtils.repeat(character, postFillLength));
  }
}