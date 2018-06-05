# Bug Location and Repair of Defects (BugLoRD) - Toolkit

### Purpose

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

### Installation

#### Prerequisites:
- Java JDK/JRE 1.8
- Maven (I am using v3.3.9)
- the [HUB-SE-framework](https://github.com/hub-se/HUB-SE-framework) (execute 'mvn install' to add the necessary jar-file to your local maven repository.)

Before you can install BugLoRD, you have to set the environment variables **JRE_HOME** and **JAVA_HOME** to an existing installation of Java 1.8. You can set them directly or you can add the following code to the **settings.xml** file that is located in your local mavan repository directory (.../.m2/settings.xml). If it doesn't exist, you may have to create it.

```xml
<profiles>
  <profile>
    <id>compiler</id>
    <properties>
      <JAVA_HOME>path\to\Java-1.8\Home\directory(JDK)</JAVA_HOME>
      <JRE_HOME>path\to\Java-1.8\Home\directory(JRE)</JRE_HOME>
    </properties>
  </profile>
</profiles>
  
<activeProfiles>
  <activeProfile>compiler</activeProfile>
</activeProfiles>
```
#### Compilation
To compile the tool, generate executable jar-files and JavaDocs, simply run 'mvn install' in the main project directory.

#### Bug reports
This project is under constant developement and is a research prototype. If you encounter any bugs, feel free to open an issue or write an [e-mail](mailto:heiden@informatik.hu-berlin.de) with details of the bug and I will fix it as soon as possible.
