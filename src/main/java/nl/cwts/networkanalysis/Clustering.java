package nl.cwts.networkanalysis;

import java.io.Serializable;

/**
 * Clustering of the nodes in a network.
 *
 * <p>
 * Each node belongs to exactly one cluster.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class Clustering implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1;

    /**
     * Number of nodes.
     */
    protected int nNodes;

    /**
     * Number of clusters.
     */
    protected int nClusters;

    /**
     * Cluster of each node.
     */
    protected int[] clusters;

    /**
     * Constructs a singleton clustering for a specified number of nodes.
     *
     * @param nNodes Number of nodes
     */
    public Clustering(int nNodes)
    {
        this.nNodes = nNodes;
        initSingletonClustersHelper();
    }

    /**
     * Clones the clustering.
     *
     * @return Cloned clustering
     */
    public Clustering clone()
    {
        Clustering clonedClustering;

        try
        {
            clonedClustering = (Clustering)super.clone();
            clonedClustering.clusters = clusters.clone();
            return clonedClustering;
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
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
     * Returns whether cluster is empty or not.
     *
     * @return Cluster is empty for each cluster
     */
    public boolean[] getClusterIsNotEmpty()
    {
        boolean[] clusterIsNotEmpty;
        int c, i;

        clusterIsNotEmpty = new boolean[nClusters];
        for (i = 0; i < nNodes; i++)
        {
            c = clusters[i];
            if (!clusterIsNotEmpty[c])
                clusterIsNotEmpty[c] = true;
        }
        return clusterIsNotEmpty;
    }

   /**
     * Returns the cluster of each node.
     *
     * @return Cluster of each node
     */
    public int[] getClusters()
    {
        return clusters.clone();
    }

    /**
     * Returns the number of nodes per cluster.
     *
     * @return Number of nodes per cluster
     */
    public int[] getNNodesPerCluster()
    {
        int i;
        int[] nNodesPerCluster;

        nNodesPerCluster = new int[nClusters];
        for (i = 0; i < nNodes; i++)
            nNodesPerCluster[clusters[i]]++;
        return nNodesPerCluster;
    }

    /**
     * Returns a list of nodes per cluster.
     *
     * @return List of nodes per cluster
     */
    public int[][] getNodesPerCluster()
    {
        int i;
        int[] nNodesPerCluster;
        int[][] nodesPerCluster;

        nodesPerCluster = new int[nClusters][];
        nNodesPerCluster = getNNodesPerCluster();
        for (i = 0; i < nClusters; i++)
        {
            nodesPerCluster[i] = new int[nNodesPerCluster[i]];
            nNodesPerCluster[i] = 0;
        }
        for (i = 0; i < nNodes; i++)
        {
            nodesPerCluster[clusters[i]][nNodesPerCluster[clusters[i]]] = i;
            nNodesPerCluster[clusters[i]]++;
        }
        return nodesPerCluster;
    }

    /**
     * Removes empty clusters.
     *
     * <p>
     * Clusters are relabeled to follow a strictly consecutive numbering {@code
     * 0, ..., nClusters - 1}.
     * </p>
     *
     */
    public void removeEmptyClusters()
    {
        removeEmptyClustersLargerThan(0);
    }

    /**
     * Removes empty clusters and relabels clusters to follow consecutive
     * numbering only for clusters larger than the specified minimum number of
     * clusters.
     *
     * <p>
     * Each empty cluster larger that {@code minimumCluster} is reassigned to
     * the lowest available cluster, in the order of the existing clusters. For
     * example, if {@code minimumCluster = 5} and cluster 2 and 7 are empty,
     * then cluster 8 is relabeled to 7 (and 9 to 8, etc...), but clusters 0-4
     * remain as they are.
     * </p>
     *
     * @param minimumCluster Minimum cluster to start relabeling from.
     */
    public void removeEmptyClustersLargerThan(int minimumCluster)
    {
        boolean[] clusterIsNotEmpty;
        int i, j;
        int[] newClusters;

        clusterIsNotEmpty = getClusterIsNotEmpty();

        // Do not relabel until minimumCluster
        newClusters = new int[nClusters];
        for (j = 0; j < minimumCluster; j++)
            newClusters[j] = j;

        // Relabel starting from minimumCluster
        i = j;
        for ( ; j < nClusters; j++)
            if (clusterIsNotEmpty[j])
            {
                newClusters[j] = i;
                i++;
            }
        nClusters = i;
        for (i = 0; i < nNodes; i++)
            clusters[i] = newClusters[clusters[i]];
    }

    /**
     * Merges the clusters based on a clustering of the clusters.
     *
     * @param clustering Clustering of the clusters
     */
    public void mergeClusters(Clustering clustering)
    {
        int i;

        for (i = 0; i < nNodes; i++)
            clusters[i] = clustering.clusters[clusters[i]];
        nClusters = clustering.nClusters;
    }

    private void initSingletonClustersHelper()
    {
        int i;

        clusters = new int[nNodes];
        for (i = 0; i < nNodes; i++)
            clusters[i] = i;
        nClusters = nNodes;
    }
}
