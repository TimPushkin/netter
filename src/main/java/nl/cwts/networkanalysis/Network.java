package nl.cwts.networkanalysis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Network.
 *
 * <p>
 * Weighted nodes and weighted edges are supported. Directed edges are not
 * supported.
 * </p>
 *
 * <p>
 * Network objects are immutable.
 * </p>
 *
 * <p>
 * The adjacency matrix of the network is stored in a sparse compressed format.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class Network implements Serializable
{
    private static final long serialVersionUID = 1;

    /**
     * Number of nodes.
     */
    protected int nNodes;

    /**
     * Number of edges.
     *
     * <p>
     * Each edge is counted twice, once in each direction.
     * </p>
     */
    protected int nEdges;

    /**
     * Node weights.
     */
    protected double[] nodeWeights;

    /**
     * Index of the first neighbor of each node in the (@code neighbors} array.
     *
     * <p>
     * The neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}.
     * </p>
     */
    protected int[] firstNeighborIndices;

    /**
     * Neighbors of each node.
     */
    protected int[] neighbors;

    /**
     * Edge weights.
     */
    protected double[] edgeWeights;

    /**
     * Total edge weight of self links.
     */
    protected double totalEdgeWeightSelfLinks;

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]}. Edges do not have weights. If {@code sortedEdges} is
     * false, the list of edges does not need to be sorted and each edge must
     * be included only once. If {@code sortedEdges}is true, the list of edges
     * must be sorted and each edge must be included twice, once in each
     * direction.
     * </p>
     *
     * @param nodeWeights    Node weights
     * @param edges          Edge list
     * @param sortedEdges    Indicates whether the edge list is sorted
     * @param checkIntegrity Indicates whether to check the integrity of the
     *                       network
     */
    public Network(double[] nodeWeights, int[][] edges, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, edges, null, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]} and has weight {@code edgeWeights[i]}. If {@code
     * sortedEdges} is false, the list of edges does not need to be sorted and
     * each edge must be included only once. If {@code sortedEdges} is true,
     * the list of edges must be sorted and each edge must be included twice,
     * once in each direction.
     * </p>
     *
     * @param nodeWeights    Node weights
     * @param edges          Edge list
     * @param edgeWeights    Edge weights
     * @param sortedEdges    Indicates whether the edge list is sorted
     * @param checkIntegrity Indicates whether to check the integrity of the
     *                       network
     */
    public Network(double[] nodeWeights, int[][] edges, double[] edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, edges, edgeWeights, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. Edges do not have weights.
     * </p>
     *
     * @param nodeWeights          Node weights
     * @param firstNeighborIndices Index of the first neighbor of each node
     * @param neighbors            Neighbor list
     * @param checkIntegrity       Indicates whether to check the integrity of
     *                             the network
     */
    public Network(double[] nodeWeights, int[] firstNeighborIndices, int[] neighbors, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, firstNeighborIndices, neighbors, null, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. For each neighbor in the array {@code neighbors}, the
     * corresponding edge weight is provided in the array {@code edgeWeights}.
     * </p>
     *
     * @param nodeWeights          Node weights
     * @param firstNeighborIndices Index of the first neighbor of each node
     * @param neighbors            Neighbor list
     * @param edgeWeights          Edge weights
     * @param checkIntegrity       Indicates whether to check the integrity of
     *                             the network
     */
    public Network(double[] nodeWeights, int[] firstNeighborIndices, int[] neighbors, double[] edgeWeights, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, firstNeighborIndices, neighbors, edgeWeights, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]}. Edges do not have weights. If {@code sortedEdges} is
     * false, the list of edges does not need to be sorted and each edge must
     * be included only once. If {@code sortedEdges}is true, the list of edges
     * must be sorted and each edge must be included twice, once in each
     * direction.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param edges                            Edge list
     * @param sortedEdges                      Indicates whether the edge list
     *                                         is sorted
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, int[][] edges, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, edges, null, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]} and has weight {@code edgeWeights[i]}. If {@code
     * sortedEdges} is false, the list of edges does not need to be sorted and
     * each edge must be included only once. If {@code sortedEdges} is true,
     * the list of edges must be sorted and each edge must be included twice,
     * once in each direction.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param edges                            Edge list
     * @param edgeWeights                      Edge weights
     * @param sortedEdges                      Indicates whether the edge list
     *                                         is sorted
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, int[][] edges, double[] edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, edges, edgeWeights, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. Edges do not have weights.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param firstNeighborIndices             Index of the first neighbor of
     *                                         each node
     * @param neighbors                        Neighbor list
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, int[] firstNeighborIndices, int[] neighbors, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, firstNeighborIndices, neighbors, null, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. For each neighbor in the array {@code neighbors}, the
     * corresponding edge weight is provided in the array {@code edgeWeights}.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param firstNeighborIndices             Index of the first neighbor of
     *                                         each node
     * @param neighbors                        Neighbor list
     * @param edgeWeights                      Edge weights
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, int[] firstNeighborIndices, int[] neighbors, double[] edgeWeights, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, firstNeighborIndices, neighbors, edgeWeights, checkIntegrity);
    }

    /**
     * Returns the number of nodes.
     *
     * @return Number of nodes
     */
    public int getNNodes()
    {
        return nNodes;
    }

    /**
     * Returns the total node weight.
     *
     * @return Total node weight
     */
    public double getTotalNodeWeight()
    {
        return nl.cwts.util.Arrays.calcSum(nodeWeights);
    }

    /**
     * Returns the weight of each node.
     *
     * @return Weight of each node
     */
    public double[] getNodeWeights()
    {
        return nodeWeights.clone();
    }

    /**
     * Returns the total edge weight per node. The total edge weight of a node
     * equals the sum of the weights of the edges between the node and its
     * neighbors.
     *
     * @return Total edge weight per node
     */
    public double[] getTotalEdgeWeightPerNode()
    {
        return getTotalEdgeWeightPerNodeHelper();
    }

    /**
     * Creates induced subnetworks for the clusters in a clustering.
     *
     * @param clustering Clustering
     *
     * @return Subnetworks
     */
    public Network[] createSubnetworks(Clustering clustering)
    {
        double[] subnetworkEdgeWeights;
        int i;
        int[] subnetworkNeighbors, subnetworkNodes;
        int[][] nodesPerCluster;
        Network[] subnetworks;

        subnetworks = new Network[clustering.nClusters];
        nodesPerCluster = clustering.getNodesPerCluster();
        subnetworkNodes = new int[nNodes];
        subnetworkNeighbors = new int[nEdges];
        subnetworkEdgeWeights = new double[nEdges];
        for (i = 0; i < clustering.nClusters; i++)
            subnetworks[i] = createSubnetwork(clustering, i, nodesPerCluster[i], subnetworkNodes, subnetworkNeighbors, subnetworkEdgeWeights);
        return subnetworks;
    }

    /**
     * Creates a reduced (or aggregate) network based on a clustering.
     *
     * <p>
     * Each node in the reduced network corresponds to a cluster of nodes in
     * the original network. The weight of a node in the reduced network equals
     * the sum of the weights of the nodes in the corresponding cluster in the
     * original network. The weight of an edge between two nodes in the reduced
     * network equals the sum of the weights of the edges between the nodes in
     * the two corresponding clusters in the original network.
     * </p>
     *
     * @param clustering Clustering
     *
     * @return Reduced network
     */
    public Network createReducedNetwork(Clustering clustering)
    {
        double[] reducedNetworkEdgeWeights1, reducedNetworkEdgeWeights2;
        int i, j, k, l, m, n;
        int[] reducedNetworkNeighbors1, reducedNetworkNeighbors2;
        int[][] nodesPerCluster;
        Network reducedNetwork;

        reducedNetwork = new Network();

        reducedNetwork.nNodes = clustering.nClusters;

        reducedNetwork.nEdges = 0;
        reducedNetwork.nodeWeights = new double[clustering.nClusters];
        reducedNetwork.firstNeighborIndices = new int[clustering.nClusters + 1];
        reducedNetwork.totalEdgeWeightSelfLinks = totalEdgeWeightSelfLinks;
        reducedNetworkNeighbors1 = new int[nEdges];
        reducedNetworkEdgeWeights1 = new double[nEdges];
        reducedNetworkNeighbors2 = new int[clustering.nClusters - 1];
        reducedNetworkEdgeWeights2 = new double[clustering.nClusters];
        nodesPerCluster = clustering.getNodesPerCluster();
        for (i = 0; i < clustering.nClusters; i++)
        {
            j = 0;
            for (k = 0; k < nodesPerCluster[i].length; k++)
            {
                l = nodesPerCluster[i][k];

                reducedNetwork.nodeWeights[i] += nodeWeights[l];

                for (m = firstNeighborIndices[l]; m < firstNeighborIndices[l + 1]; m++)
                {
                    n = clustering.clusters[neighbors[m]];
                    if (n != i)
                    {
                        if (reducedNetworkEdgeWeights2[n] == 0)
                        {
                            reducedNetworkNeighbors2[j] = n;
                            j++;
                        }
                        reducedNetworkEdgeWeights2[n] += edgeWeights[m];
                    }
                    else
                        reducedNetwork.totalEdgeWeightSelfLinks += edgeWeights[m];
                }
            }

            for (k = 0; k < j; k++)
            {
                reducedNetworkNeighbors1[reducedNetwork.nEdges + k] = reducedNetworkNeighbors2[k];
                reducedNetworkEdgeWeights1[reducedNetwork.nEdges + k] = reducedNetworkEdgeWeights2[reducedNetworkNeighbors2[k]];
                reducedNetworkEdgeWeights2[reducedNetworkNeighbors2[k]] = 0;
            }
            reducedNetwork.nEdges += j;
            reducedNetwork.firstNeighborIndices[i + 1] = reducedNetwork.nEdges;
        }
        reducedNetwork.neighbors = Arrays.copyOfRange(reducedNetworkNeighbors1, 0, reducedNetwork.nEdges);
        reducedNetwork.edgeWeights = Arrays.copyOfRange(reducedNetworkEdgeWeights1, 0, reducedNetwork.nEdges);

        return reducedNetwork;
    }

    /**
     * Checks the integrity of the network.
     *
     * <p>
     * It is checked whether:
     * </p>
     *
     * <ul>
     * <li>variables have a correct value,</li>
     * <li>arrays have a correct length,</li>
     * <li>edges are sorted correctly,</li>
     * <li>edges are stored in both directions.</li>
     * </ul>
     *
     * <p>
     * An exception is thrown if the integrity of the network is violated.
     * </p>
     *
     * @throws IllegalArgumentException An illegal argument was provided in the
     *                                  construction of the network.
     */
    public void checkIntegrity() throws IllegalArgumentException
    {
        boolean[] checked;
        int i, j, k, l;

        // Check whether variables have a correct value and arrays have a
        // correct length.
        if (nNodes < 0)
            throw new IllegalArgumentException("nNodes must be non-negative.");

        if (nEdges < 0)
            throw new IllegalArgumentException("nEdges must be non-negative.");

        if (nEdges % 2 == 1)
            throw new IllegalArgumentException("nEdges must be even.");

        if (nodeWeights.length != nNodes)
            throw new IllegalArgumentException("Length of nodeWeight array must be equal to nNodes.");

        if (firstNeighborIndices.length != nNodes + 1)
            throw new IllegalArgumentException("Length of firstNeighborIndices array must be equal to nNodes + 1.");

        if (firstNeighborIndices[0] != 0)
            throw new IllegalArgumentException("First element of firstNeighborIndices array must be equal to 0.");

        if (firstNeighborIndices[nNodes] != nEdges)
            throw new IllegalArgumentException("Last element of firstNeighborIndices array must be equal to nEdges.");

        if (neighbors.length != nEdges)
            throw new IllegalArgumentException("Length of neighbors array must be equal to nEdges.");

        if (edgeWeights.length != nEdges)
            throw new IllegalArgumentException("Length of edgeWeights array must be equal to nEdges.");

        // Check whether edges are sorted correctly.
        for (i = 0; i < nNodes; i++)
        {
            if (firstNeighborIndices[i + 1] < firstNeighborIndices[i])
                throw new IllegalArgumentException("Elements of firstNeighborIndices array must be in non-decreasing order.");

            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
            {
                k = neighbors[j];

                if (k < 0)
                    throw new IllegalArgumentException("Elements of neighbors array must have non-negative values.");
                else if (k >= nNodes)
                    throw new IllegalArgumentException("Elements of neighbors array must have values less than nNodes.");

                if (j > firstNeighborIndices[i])
                {
                    l = neighbors[j - 1];
                    if (k < l)
                        throw new IllegalArgumentException("For each node, corresponding elements of neighbors array must be in increasing order.");
                    else if (k == l)
                        throw new IllegalArgumentException("For each node, corresponding elements of neighbors array must not include duplicate values.");
                }
            }
        }

        // Check whether edges are stored in both directions.
        checked = new boolean[nEdges];
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
                if (!checked[j])
                {
                    k = neighbors[j];

                    l = Arrays.binarySearch(neighbors, firstNeighborIndices[k], firstNeighborIndices[k + 1], i);
                    if (l < 0)
                        throw new IllegalArgumentException("Edges must be stored in both directions.");
                    if (edgeWeights[j] != edgeWeights[l])
                        throw new IllegalArgumentException("Edge weights must be the same in both directions.");

                    checked[j] = true;
                    checked[l] = true;
                }
    }

    private static void sortEdges(int[][] edges, double[] edgeWeights)
    {
        class EdgeComparator implements Comparator<Integer>
        {
            int[][] edges;

            public EdgeComparator(int[][] edges)
            {
                this.edges = edges;
            }

            public int compare(Integer i, Integer j)
            {
                if (edges[0][i] > edges[0][j])
                    return 1;
                if (edges[0][i] < edges[0][j])
                    return -1;
                if (edges[1][i] > edges[1][j])
                    return 1;
                if (edges[1][i] < edges[1][j])
                    return -1;
                return 0;
            }
        }

        double[] edgeWeightsSorted;
        int i, nEdges;
        int[][] edgesSorted;
        Integer[] indices;

        nEdges = edges[0].length;

        // Determine sorting order.
        indices = new Integer[nEdges];
        for (i = 0; i < nEdges; i++)
            indices[i] = i;
        Arrays.parallelSort(indices, new EdgeComparator(edges));

        // Sort edges.
        edgesSorted = new int[2][nEdges];
        for (i = 0; i < nEdges; i++)
        {
            edgesSorted[0][i] = edges[0][indices[i]];
            edgesSorted[1][i] = edges[1][indices[i]];
        }
        edges[0] = edgesSorted[0];
        edges[1] = edgesSorted[1];

        // Sort edge weights.
        if (edgeWeights != null)
        {
            edgeWeightsSorted = new double[nEdges];
            for (i = 0; i < nEdges; i++)
                edgeWeightsSorted[i] = edgeWeights[indices[i]];
            System.arraycopy(edgeWeightsSorted, 0, edgeWeights, 0, nEdges);
        }
    }

    private Network()
    {
    }

    private Network(int nNodes, double[] nodeWeights, boolean setNodeWeightsToTotalEdgeWeights, int[][] edges, double[] edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        double[] edgeWeights2;
        int i, j;
        int[][] edges2;

        if (!sortedEdges)
        {
            edges2 = new int[2][2 * edges[0].length];
            edgeWeights2 = (edgeWeights != null) ? new double[2 * edges[0].length] : null;
            i = 0;
            for (j = 0; j < edges[0].length; j++)
            {
                edges2[0][i] = edges[0][j];
                edges2[1][i] = edges[1][j];
                if (edgeWeights != null)
                    edgeWeights2[i] = edgeWeights[j];
                i++;
                if (edges[0][j] != edges[1][j])
                {
                    edges2[0][i] = edges[1][j];
                    edges2[1][i] = edges[0][j];
                    if (edgeWeights != null)
                        edgeWeights2[i] = edgeWeights[j];
                    i++;
                }
            }
            edges[0] = Arrays.copyOfRange(edges2[0], 0, i);
            edges[1] = Arrays.copyOfRange(edges2[1], 0, i);
            if (edgeWeights != null)
                edgeWeights = Arrays.copyOfRange(edgeWeights2, 0, i);
            sortEdges(edges, edgeWeights);
        }

        this.nNodes = nNodes;
        nEdges = 0;
        firstNeighborIndices = new int[nNodes + 1];
        neighbors = new int[edges[0].length];
        this.edgeWeights = new double[edges[0].length];
        totalEdgeWeightSelfLinks = 0;
        i = 1;
        for (j = 0; j < edges[0].length; j++)
            if (edges[0][j] != edges[1][j])
            {
                for (; i <= edges[0][j]; i++)
                    firstNeighborIndices[i] = nEdges;
                neighbors[nEdges] = edges[1][j];
                this.edgeWeights[nEdges] = (edgeWeights != null) ? edgeWeights[j] : 1;
                nEdges++;
            }
            else
                totalEdgeWeightSelfLinks += (edgeWeights != null) ? edgeWeights[j] : 1;
        for (; i <= nNodes; i++)
            firstNeighborIndices[i] = nEdges;
        neighbors = Arrays.copyOfRange(neighbors, 0, nEdges);
        this.edgeWeights = Arrays.copyOfRange(this.edgeWeights, 0, nEdges);

        this.nodeWeights = (nodeWeights != null) ? nodeWeights.clone() : (setNodeWeightsToTotalEdgeWeights ? getTotalEdgeWeightPerNodeHelper() : nl.cwts.util.Arrays.createDoubleArrayOfOnes(nNodes));

        if (checkIntegrity)
            checkIntegrity();
    }

    private Network(int nNodes, double[] nodeWeights, boolean setNodeWeightsToTotalEdgeWeights, int[] firstNeighborIndices, int[] neighbors, double[] edgeWeights, boolean checkIntegrity)
    {
        this.nNodes = nNodes;
        nEdges = neighbors.length;
        this.firstNeighborIndices = firstNeighborIndices.clone();
        this.neighbors = neighbors.clone();
        this.edgeWeights = (edgeWeights != null) ? edgeWeights.clone() : nl.cwts.util.Arrays.createDoubleArrayOfOnes(nEdges);
        totalEdgeWeightSelfLinks = 0;

        this.nodeWeights = (nodeWeights != null) ? nodeWeights.clone() : (setNodeWeightsToTotalEdgeWeights ? getTotalEdgeWeightPerNodeHelper() : nl.cwts.util.Arrays.createDoubleArrayOfOnes(nNodes));

        if (checkIntegrity)
            checkIntegrity();
    }

    private double[] getTotalEdgeWeightPerNodeHelper()
    {
        double[] totalEdgeWeightPerNode;
        int i;

        totalEdgeWeightPerNode = new double[nNodes];
        for (i = 0; i < nNodes; i++)
            totalEdgeWeightPerNode[i] = nl.cwts.util.Arrays.calcSum(edgeWeights, firstNeighborIndices[i], firstNeighborIndices[i + 1]);
        return totalEdgeWeightPerNode;
    }

    protected Network createSubnetwork(Clustering clustering, int cluster, int[] nodes, int[] subnetworkNodes, int[] subnetworkNeighbors, double[] subnetworkEdgeWeights)
    {
        int i, j, k;
        Network subnetwork;

        subnetwork = new Network();

        subnetwork.nNodes = nodes.length;

        if (subnetwork.nNodes == 1)
        {
            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[1];
            subnetwork.nodeWeights[0] = nodeWeights[nodes[0]];
            subnetwork.firstNeighborIndices = new int[2];
            subnetwork.neighbors = new int[0];
            subnetwork.edgeWeights = new double[0];
        }
        else
        {
            for (i = 0; i < nodes.length; i++)
                subnetworkNodes[nodes[i]] = i;

            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[subnetwork.nNodes];
            subnetwork.firstNeighborIndices = new int[subnetwork.nNodes + 1];
            for (i = 0; i < subnetwork.nNodes; i++)
            {
                j = nodes[i];
                subnetwork.nodeWeights[i] = nodeWeights[j];
                for (k = firstNeighborIndices[j]; k < firstNeighborIndices[j + 1]; k++)
                    if (clustering.clusters[neighbors[k]] == cluster)
                    {
                        subnetworkNeighbors[subnetwork.nEdges] = subnetworkNodes[neighbors[k]];
                        subnetworkEdgeWeights[subnetwork.nEdges] = edgeWeights[k];
                        subnetwork.nEdges++;
                    }
                subnetwork.firstNeighborIndices[i + 1] = subnetwork.nEdges;
            }
            subnetwork.neighbors = Arrays.copyOfRange(subnetworkNeighbors, 0, subnetwork.nEdges);
            subnetwork.edgeWeights = Arrays.copyOfRange(subnetworkEdgeWeights, 0, subnetwork.nEdges);
        }

        subnetwork.totalEdgeWeightSelfLinks = 0;

        return subnetwork;
    }
}
