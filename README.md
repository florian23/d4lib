# d4lib - Aho-Corasick Pattern Matching

Java-Implementierung des [Aho-Corasick Algorithmus](https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm) zur gleichzeitigen Suche mehrerer Keywords in einem Text.

## Verwendung

```java
PatternMatchingMachine pmm = PatternMatchingMachine.create("he", "she", "his", "hers");
KeywordLocations locations = pmm.match("ushers");
locations.print();
// Key he an Positionen [2]
// Key hers an Positionen [2]
// Key she an Positionen [1]
```

Alternativ mit einer Liste:

```java
List<String> keywords = Arrays.asList("he", "she", "his", "hers");
PatternMatchingMachine pmm = PatternMatchingMachine.create(keywords);
Map<String, List<Integer>> result = pmm.match("ushers").getLocations();
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
javac -d target/classes src/main/java/algorithm/stringmatching/*.java

# Ausfuehren
java -cp target/classes algorithm.stringmatching.PatternMatchingMachine

# Tests (JUnit 4 JAR benoetigt)
javac -cp "target/classes:path/to/junit-4.13.2.jar:path/to/hamcrest-core-1.3.jar" \
  -d target/test-classes src/test/java/algorithm/stringmatching/*.java

java -cp "target/classes:target/test-classes:path/to/junit-4.13.2.jar:path/to/hamcrest-core-1.3.jar" \
  org.junit.runner.JUnitCore algorithm.stringmatching.PatternMatchingMachineTest
```

Eine `pom.xml` fuer Maven ist enthalten. Maven-Build funktioniert, sobald eine Verbindung zu Maven Central besteht:

```bash
mvn compile
mvn test
```

## Projektstruktur

```
src/
  main/java/algorithm/stringmatching/
    PatternMatchingMachine.java   - Kernalgorithmus (Aho-Corasick)
    KeywordLocations.java         - Ergebnis-Container
  test/java/algorithm/stringmatching/
    PatternMatchingMachineTest.java - JUnit-Tests (11 Testfaelle)
```
