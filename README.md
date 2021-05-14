# Netter

<img src="https://github.com/TimPushkin/netter/blob/view/src/main/resources/Netter.png" width="256" height="256" align="right" />

An app for complex network analysis.

## Toolkit

### Netter uses

- __ForceAtlas2__ from [Gephi](https://gephi.org/)
- __Leiden algorithm__ from [networkanalysis](https://github.com/CWTSLeiden/networkanalysis)
- __Harmonic centrality algorithm__ from [JGraphT](https://jgrapht.org/)

### Netter works with

- Plain text
- [SQLite](https://www.sqlite.org/index.html) database
- [Neo4j](https://neo4j.com/) database

More details are available in the _Documentation_ section.

## Get started

- Download the source code or clone the repository:

       git clone https://github.com/TimPushkin/netter.git

- Run Netter in the downloaded directory:

       ./gradlew run

### Requirements

- Java JDK version 11
- [Gradle](https://gradle.org/) version 6.8

---

# Documentation

After launch, import a network from a supported format using the top menu bar.

## General IO information

### Network specifications

Networks supported by Netter have the following properties:

- Links are unweighted and undirected.

- Self-loops are allowed.

- Unconnected networks are allowed.

- Multiple (or "parallel") links are not allowed.

- Nodes are defined by unique IDs (non-negative whole numbers).

- Nodes have community (a non-negative whole number) and centrality (a non-negative decimal number) as their main 
  characteristics.

- Nodes have two-dimensional coordinates (decimal numbers) for the purpose of network layout.

- If there is a node with ID _N_ in a network, then all the nodes with IDs from _0_ to _N - 1_ are also in the network.

### Input

To match the properties above, all input formats have the following properties:

- The format is divided into two parts: links' description and nodes' description.

- A link is defined by the IDs of the two nodes it is connecting (the IDs may be equal, then it is a self-looped link).

- When a link is added to a network, its incident nodes are also automatically added with the default characteristics, 
which are: 0 for community, 0.5 for centrality, and (0.0, 0.0) for coordinates.

- A node is defined by its unique ID and its main characteristics (community and centrality). Optionally, coordinates 
may also be provided.

- When a node with ID _N_ is added (either by adding an incident link or by providing its description), all the missing 
nodes with IDs from _0_ to _N - 1_ are also automatically added with the default characteristics.

### Output

All output formats have the following properties:

- All links are included for export, just like in the import format.

- All nodes are included for export, and for each node its ID and all of its characteristics (including its coordinates) 
are exported.

## Import - export format specifications

### Plain text file

Input is divided into two blocks: links' description and nodes' description, both of which are optional. The blocks 
should be divided with an empty line. After the second block there may be either an end of file or an empty line. If it 
is an empty line, any information after it is not taken into account.

Each line in the links' description block must consist of two IDs separated with a whitespace - these are the IDs of the 
nodes, which are connected by a link being described. If no further information about the nodes is provided, all of 
these nodes will have the default parameters. If repetitive links occur in an imported file, only one of them will be 
added to the network.

In the following example three links - 4-6, 0-1, and 0-10 - will be added to the resulting network (remember, that the 
network will contain all the skipped nodes too):

    4 6
    6 4
    1 0
    0 8

Each line in the nodes' description must consist of either 3 or 5 columns separated with a whitespace (lines with 
different columns number may be mixed). In both cases the first column represents the ID of a node being described, the 
second represents its community, and the third represents its centrality. If the line consists of 5 columns, then the 
last two represent the coordinates of the node: first x and then y. If several lines describe a node with the same ID, 
then only the last one will have an effect.

In the following example two nodes - the first one with ID 2, from community 0, with centrality of 0.0, with coordinates 
(0.0, 0.0), and the second one with ID 4, from community 3, with centrality of 5.0, and with coordinates (0.0, 0.0) -  
are created (remember, that the resulting network will contain all the skipped nodes too):

    4 2 1.2
    2 0 0.0 4.6 -22
    4 3 5

The examples above will be successfully imported separately (into different networks), although the second one must 
contain a blank line before it. Combining these examples, we will get a single input, that will be also successfully 
imported (into a single network):

    4 6
    6 4
    1 0
    0 8
    
    4 2 1.2
    2 0 0.0 4.6 -22
    4 3 5

More examples of the correct inputs may be found in the [test resource files](https://github.com/TimPushkin/netter/tree/dev/src/test/resources/txt-inputs).

### SQLite

The input data should be contained in the tables under the names: `links` and `nodes`. The input file may contain either one of these tables or both at the same time.

The `links` table must necessarily contain columns named: `id1` and `id2`.  These columns must contain the values of the vertex ids.

The `nodes` table must contain columns named: `id`, `community`, `centrality`, `x`, `y`. 
The `id`, `community` and `centrality` columns must necessarily contain data. 
Columns `x` and `y` may contain values of type `null`.

`id`, `id1`, `id2` and `community` must be Integer. `centrality`, `x` and `y` must be Double.

The output data has the same format.

### Neo4j

The connection is made according to the `bolt://` URI scheme. Database credentials such as _username_ and _password_ are 
required. Nodes with the label `NODE` will be read from the database. All the nodes should contain node `id` and may 
contain additional information about `community`, `centrality` and coordinates `x`, `y`.

Links with the label `LINK` will be read. They must connect the nodes with the label `NODE`.
#### Examples of creating valid nodes and links

    CREATE (n:NODE{ id:$id_value, [community:$community_value], [centrality:$centrality_value], [x:$x_value], [y:$y_value] })

    MATCH (n1:NODE{ id:$id1_value }) MATCH (n2:NODE{ id:$id2_value }) CREATE (n1)-[:LINK]->(n2)

## Layout algorithms

### Circular layout

The _circular layout_ places the nodes into a circle.

This layout has the following parameters:

- **Repulsion** (non-negative decimal number) - it is proportional to the radius of the circle. The greater the 
repulsion, the wider is the circle.

### ForceAtlas2

The _ForceAtlas2_ layout uses force-directed technics to draw the network.

This layout has the following parameters:

- **Loops number** (positive whole number) - the number of the cycles of the algorithm.

- **Adjust sizes** (checkbox) - prevents nodes from overlapping by their default (unchangeable) size.

- **Barnes-Hut optimization** (checkbox) - enables approximate computation of the repulsion force, increasing the speed 
of the algorithm.

- **LinLog mode** (checkbox) - enables the usage of a logarithmic attraction force.

- **Strong gravity mode** (checkbox) - sets a strong force that attracts distant nodes to the center.

- **Jitter tolerance** (positive decimal number) - controls the allowed amount of swinging of the nodes. The lower the 
tolerance, the greater the precision.

- **Scaling ratio** (positive decimal number) - controls the attraction and repulsion forces. The greater the scaling, 
the sparser the network.

- **Gravity** (decimal number) - defines the force that prevents disconnected components from drifting away. The greater 
the gravity, the greater the force that attracts nodes to the center.

- **Barnes-Hut theta** (decimal number) - controls the approximate computation of the repulsion force. The greater the 
theta the less precise the approximation.

## Community detection algorithm

### Leiden algorithm

The _Leiden algorithm_ is a community detection algorithm, based on the Louvain algorithm. It provides better partitions 
in less calculation time, especially for large networks.

This algorithm has the following parameters:

- **Resolution** (positive decimal number) - controls the size of the communities. Higher resolutions lead to more 
communities.

## Centrality identifying algorithm

### Harmonic centrality

_Harmonic centrality_ is a version of closeness centrality, that is able to work with unconnected networks. It is 
usually used in social network analysis.

Harmonic centrality is calculated as
![equation](https://latex.codecogs.com/gif.latex?C_%7BH%7D%28x%29%20%3D%20%5Csum_%7Bx%5Cneq%20y%7D%5E%7B%7D%20%5Cfrac%7B1%7D%7Bd%28x%2Cy%29%7D)

Where d(x,y) is the distance from x to y

Harmonic centrality can be normalized by dividing by N-1, where N is the number of nodes in the graph.
