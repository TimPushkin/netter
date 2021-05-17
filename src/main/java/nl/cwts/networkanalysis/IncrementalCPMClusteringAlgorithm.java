package nl.cwts.networkanalysis;

/**
 * Abstract base class for incremental clustering algorithms that use the CPM
 * quality function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public abstract class IncrementalCPMClusteringAlgorithm extends CPMClusteringAlgorithm implements IncrementalClusteringAlgorithm
{

    /**
     * Constructs an incremental CPM clustering algorithm with a specified
     * resolution parameter.
     *
     * @param resolution Resolution parameter
     */
    public IncrementalCPMClusteringAlgorithm(double resolution)
    {
        super(resolution);
    }
}
