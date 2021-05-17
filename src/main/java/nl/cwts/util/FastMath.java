package nl.cwts.util;

/**
 * Fast implementations of mathematical functions.
 *
 * <p>
 * All methods in this class are static.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public final class FastMath
{
    /**
     * Calculates {@code exp(exponent)} using a fast implementation.
     *
     * @param exponent Exponent
     *
     * @return exp(exponent)
     */
    public static double fastExp(double exponent)
    {
        if (exponent < -256d)
            return 0;

        exponent = 1d + exponent / 256d;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        return exponent;
    }

    private FastMath()
    {
    }
}
