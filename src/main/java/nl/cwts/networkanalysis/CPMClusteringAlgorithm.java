package nl.cwts.networkanalysis;

/**
 * Abstract base class for clustering algorithms that use the CPM quality
 * function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public abstract class CPMClusteringAlgorithm implements Cloneable
{
    /**
     * Default resolution parameter.
     */
    public static final double DEFAULT_RESOLUTION = 1;

    /**
     * Resolution parameter.
     */
    protected double resolution;

    /**
     * Constructs a CPM clustering algorithm with a specified resolution
     * parameter.
     *
     * @param resolution Resolution parameter
     */
    public CPMClusteringAlgorithm(double resolution)
    {
        this.resolution = resolution;
    }

    /**
     * Clones the algorithm.
     *
     * @return Cloned algorithm
     */
    public CPMClusteringAlgorithm clone()
    {
        try
        {
            return (CPMClusteringAlgorithm)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * Returns the resolution parameter.
     *
     * @return Resolution parameter
     */
    public double getResolution()
    {
        return resolution;
    }

    /**
     * Sets the resolution parameter.
     *
     * @param resolution Resolution parameter
     */
    public void setResolution(double resolution)
    {
        this.resolution = resolution;
    }
}
