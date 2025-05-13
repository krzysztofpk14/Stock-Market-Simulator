package com.krzysztofpk14.app;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class RepositoryApp {
  public static void main(String[] args) {
    if (args.length < 1) {
      usage();
    } else {
      RepositoryReporter reporter = new RepositoryReporter();
      InputStream is;
      try {
        // first argument is an Orchestra file name
        is = new FileInputStream(args[0]);
        reporter.report(is, System.out);
      } catch (FileNotFoundException e) {
        System.err.println(e.getMessage());
      }
    }
  }

  public static void usage() {  
    System.err.println("Usage: RepositoryApp <xml-file>");
  }
}