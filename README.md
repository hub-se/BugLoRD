# Bug Location and Repair of Defects (BugLoRD) - Toolkit

A tool collection for locating and repairing bugs, including tools or toolchains for

- searching and running all JUnit tests from a Java project,
- generating spectrum based fault localization (SBFL) rankings,
- combining SBFL rankings with other rankings, generated from other sources,
- checking changes between two project versions with the support of [ChangeDistiller](https://bitbucket.org/sealuzh/tools-changedistiller/wiki/Home),
- collecting and plotting data.

Additional functionality includes
- a frontend to the [Defects4J](https://github.com/rjust/defects4j) benchmark for easier, more central access,
- an AST based language model builder, using the [JavaParser](https://github.com/javaparser/javaparser) project,
- tools for syntactic and semantic tokenization of Java source files.

Needs the [utilities](https://github.com/hub-se/utilities) project as a requirement to compile. (Download and execute 'mvn install' to add the needed jar-file to your local maven repository.)

To compile the tool, generate executable jar-files and JavaDoc, simply run 'mvn install' in the main project directory.

All tools are work in progress and may contain various bugs. If you find any, feel free to write an e-mail to 
  heiden @ informatik.hu-berlin.de 
with details of the encountered bug and I will fix it as soon as possible.
