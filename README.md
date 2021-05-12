# Netter ![N|Solid](https://github.com/TimPushkin/netter/blob/view/src/main/resources/icon.png)

An app for complex network analysis

## Toolkit

### Netter uses

- __ForceAtlas2__ by [Gephi](https://gephi.org/)
- __Leiden algorithm__ by [networkanalysis](https://github.com/CWTSLeiden/networkanalysis)
- __Harmonic centrality algorithm__ by [JGraphT](https://jgrapht.org/)

### Netter works with

- Text file
- [SQLite](https://www.sqlite.org/index.html) database
- [Neo4j](https://neo4j.com/) database

more details in the _Usage_ section

## Get started

---

- Clone the repository and go into

       git clone git@github.com:TimPushkin/netter.git

- Run Netter

       ./gradlew run

### Requirements

- Java JDK version 11
- [Gradle](https://gradle.org/) version 6.8

Netter hasn't been tested on other versions yet

# Usage

---

After launch import the network from a supported format using the top bar.

### Text file

    TODO

### SQLite

    TODO

### Neo4j

The connection is made according to the `bolt://` URI scheme. Enter database _username_ and _password_. Nodes with the
label `NODE` will be read from database. All nodes should contain additional information
about `id`, `community`, `centrality`

Links with the label `LINK` will be read. They must connect the nodes with the label `NODE`

__NOTE:__ `id` and `community` must be `Int`, and `centrality` - `Double`
for all formats

## Default layout

After loading the network, the _default circular layout_ will start. You will need to enter the `repulsion` value, in
this layout the `repulsion` is proportional to the radius of a circle.

## algorithms...
