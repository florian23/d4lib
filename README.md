# d4lib - Aho-Corasick Pattern Matching

Java-Implementierung des [Aho-Corasick Algorithmus](https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm) zur gleichzeitigen Suche mehrerer Keywords in einem Text.

## Verwendung

```java
import d4lib.ahocorasick.PatternMatchingMachine;

var pmm = PatternMatchingMachine.create("he", "she", "his", "hers");
var locations = pmm.match("ushers");
locations.print();
// Key he an Positionen [2]
// Key hers an Positionen [2]
// Key she an Positionen [1]
```

Alternativ mit einer Liste:

```java
var keywords = List.of("he", "she", "his", "hers");
var pmm = PatternMatchingMachine.create(keywords);
var result = pmm.match("ushers").getLocations();
```

## Algorithmus

Die Implementierung folgt dem Paper von Alfred V. Aho und Margaret J. Corasick und besteht aus vier Algorithmen:

1. **Algorithmus 1** - Pattern Matching mittels deterministischer Delta-Funktion
2. **Algorithmus 2** - Aufbau der Goto-Funktion aus Keywords
3. **Algorithmus 3** - Aufbau der Failure- und Output-Funktion (BFS)
4. **Algorithmus 4** - Erzeugung der deterministischen Zustandsuebergangsfunktion

Keywords werden automatisch dedupliziert, von leeren Strings bereinigt und nach Laenge sortiert.

## Bauen und Testen

Voraussetzungen: Java 21+

```bash
# Kompilieren
javac -d target/classes src/main/java/d4lib/ahocorasick/*.java

# Demo ausfuehren
java -cp target/classes d4lib.ahocorasick.Demo

# Tests (JUnit 4 JAR benoetigt)
javac -cp "target/classes:path/to/junit-4.13.2.jar:path/to/hamcrest-core-1.3.jar" \
  -d target/test-classes src/test/java/d4lib/ahocorasick/*.java

java -cp "target/classes:target/test-classes:path/to/junit-4.13.2.jar:path/to/hamcrest-core-1.3.jar" \
  org.junit.runner.JUnitCore d4lib.ahocorasick.PatternMatchingMachineTest
```

Eine `pom.xml` fuer Maven ist enthalten. Maven-Build funktioniert, sobald eine Verbindung zu Maven Central besteht:

```bash
mvn compile
mvn test
```

## Projektstruktur

```
src/
  main/java/d4lib/ahocorasick/
    PatternMatchingMachine.java   - Kernalgorithmus (Aho-Corasick)
    KeywordLocations.java         - Ergebnis-Container
    Demo.java                     - Beispielanwendung
  test/java/d4lib/ahocorasick/
    PatternMatchingMachineTest.java - JUnit-Tests (10 Testfaelle)
```
