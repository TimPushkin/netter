package nl.cwts.util;

import java.util.Random;

/**
 * Utility functions for arrays.
 *
 * <p>
 * All methods in this class are static.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public final class Arrays
{
    /**
     * Calculates the sum of the values in an array.
     *
     * @param values Values
     *
     * @return Sum of values
     */
    public static double calcSum(double[] values)
    {
        double sum;
        int i;

        sum = 0;
        for (i = 0; i < values.length; i++)
            sum += values[i];
        return sum;
    }

    /**
     * Calculates the sum of the values in an array, considering only array
     * elements within a specified range.
     *
     * <p>
     * The sum is calculated over the elements
     * {@code values[beginIndex], ..., values[endIndex - 1]}.
     * </p>
     *
     * @param values     Values
     * @param beginIndex Begin index
     * @param endIndex   End index
     *
     * @return Sum of values
     */
    public static double calcSum(double[] values, int beginIndex, int endIndex)
    {
        double sum;
        int i;

        sum = 0;
        for (i = beginIndex; i < endIndex; i++)
            sum += values[i];
        return sum;
    }

    /**
     * Creates a double array of ones.
     *
     * @param nElements Number of elements
     *
     * @return Array of ones
     */
    public static double[] createDoubleArrayOfOnes(int nElements)
    {
        return(repeat(1.0, nElements));
    }

    /**
     * Creates an integer array of repeated elements.
     *
     * @param value Element to repeat
     * @param nElements Number of elements
     *
     * @return Array of repeated elements
     */
    public static double[] repeat(double value, int nElements)
    {
        double[] elements;
        elements = new double[nElements];
        java.util.Arrays.fill(elements, value);
        return elements;
    }

    /**
     * Generates a random permutation.
     *
     * <p>
     * A random permutation is generated of the integers
     * {@code 0, ..., nElements - 1}.
     * </p>
     *
     * @param nElements Number of elements
     * @param random    Random number generator
     *
     * @return Random permutation
     */
    public static int[] generateRandomPermutation(int nElements, Random random)
    {
        int i;
        int[] permutation;

        permutation = new int[nElements];
        for (i = 0; i < nElements; i++)
            permutation[i] = i;
        permuteRandomly(permutation, random);
        return permutation;
    }

    /**
     * Randomly permutes the elements.
     *
     * <p>
     * Randomly permutes the elements 0, ..., nElements - 1.
     * </p>
     *
     * @param elements Elements
     * @param random   Random number generator
     */
    public static void permuteRandomly(int[] elements, Random random)
    {
        int i, j, k;

        for (i = 0; i < elements.length; i++)
        {
            j = random.nextInt(elements.length);
            k = elements[i];
            elements[i] = elements[j];
            elements[j] = k;
        }
    }

    private Arrays()
    {
    }
}
