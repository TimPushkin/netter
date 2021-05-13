# Netter

<img src="https://github.com/TimPushkin/netter/blob/view/src/main/resources/Netter.png" width="256" height="256" align="right" />

An app for complex network analysis

## Toolkit

### Netter uses

- __ForceAtlas2__ from [Gephi](https://gephi.org/)
- __Leiden algorithm__ from [networkanalysis](https://github.com/CWTSLeiden/networkanalysis)
- __Harmonic centrality algorithm__ from [JGraphT](https://jgrapht.org/)

### Netter works with

- Plain text
- [SQLite](https://www.sqlite.org/index.html) database
- [Neo4j](https://neo4j.com/) database

More details are available in the _Usage_ section

## Get started

- Download the source code or clone the repository

       git clone https://github.com/TimPushkin/netter.git

- Run Netter in the downloaded directory

       ./gradlew run

### Requirements

- Java JDK version 11
- [Gradle](https://gradle.org/) version 6.8

---

# Usage

After launch, import a network from a supported format using the top menu bar.

## Network format

Networks supported by Netter have the following properties:

- Links are unweighted and undirected

- Self-loops are allowed

- Unconnected networks are allowed

- Multiple (or "parallel") links are not allowed

- Nodes are defined by unique IDs (non-negative whole numbers)

- Nodes have community (a non-negative whole number) and centrality (a non-negative decimal number) as their main 
  characteristics

- Nodes have two-dimensional coordinates (decimal numbers) for the purpose of network layout

- If a there is a node with ID _N_ in a network, then all the nodes with IDs from _0_ to _N - 1_ are also in the network

To match the properties above, all input formats have the following properties:

- The format is divided into two parts: links description and nodes description. Node description is fully optional

- A link is defined by the IDs of the two nodes it is connecting (the IDs may be equal, and then it is a self-loop link)

- When a link is added to a network, its incident nodes are also automatically added with the default characteristics, 
which are: 0 for community, 1.0 for centrality, and (0.0, 0.0) for coordinates

- A node is defined by its unique ID and its main characteristics (community and centrality). Optionally, coordinates 
may also be provided

- When a node with ID _N_ is added (either by adding an incident link or by providing its description), all the missing 
nodes with IDs from _0_ to _N - 1_ are also automatically added with the default characteristics

All export formats have the following properties:

- All links are included for export, just like in the import format

- All nodes are included for export, and for each node its ID and all of its characteristics (including its coordinates) 
are exported

## Input / export formats

### Text file

    TODO

### SQLite

    TODO

### Neo4j

The connection is made according to the `bolt://` URI scheme. Database _username_ and _password_ are required. Nodes
with the label `NODE` will be read from the database. All the nodes should contain node `id` and may contain additional
information about `community`, `centrality` and coordinates `x`, `y`

Links with the label `LINK` will be read. They must connect the nodes with the label `NODE`

## Default layout

After loading the network, the _default circular layout_ will start. You will need to enter the `repulsion` value, in
this layout the `repulsion` is proportional to the radius of a circle.

## algorithms...
