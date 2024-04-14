package org.lei.opi.jovp;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;


/**
 * Calibration data: gamma and inverse gamma functions.
 * 
 * @since 0.2.0
 */
public class Calibration {
    /** {@value WRONG_MAX_LUMINANCE} */
    private static final String WRONG_MAX_LUMINANCE = "Maximum luminance for the gamma functions cannot be negative";
    /** {@value WRONG_INV_GAMMA_SIZE} */
    private static final String WRONG_INV_GAMMA_SIZE = "Inverse gamma array for %s should be length %s, not %s";
    /** {@value UNSORTED_GAMMA_FUNCTION} */
    private static final String UNSORTED_GAMMA_FUNCTION = "The inverse gamma arrays should be non-decreasing";
    /** {@value ILLEGAL_GAMMA_FUNCTION} */
    private static final String ILLEGAL_GAMMA_FUNCTION = "The inverse gamma arrays should not contain numbers in [0, maxPixel]";

    /** Number of decimal places for luminance in cd/m^2 */
    double lumPrecision;
    /** Max color over all 3 channels [0]=R [1]=G [2]=B (eg 255, 1024) */
    int maxPixel;
    /** Max luminance in cd/m^2 */
    double maxLum;
    /** For channels [0]=R [1]=G [2]=B, color values (ie pixel value / maxPixel in range [0,1]) for 
     *  integer lum values in cd/m^2 * 10^lumPrecision in the range [0, maxLum * 10^lumPrecision] */
    double[][] inverseGamma;

    private double scale;  //  Math.pow(10, lumPrecision);

    /**
     * Calibration factory to create a Calibration object from 
     * 
     * @param lumPrecision Number of decimal places for luminance in cd/m^2
     * @param maxLum Maximum luminance in cd/m^2
     * @param maxPixel Maximum pixel value (eg 255 or 1024)
     * @param RinvGamma the inv gamma function from 0 to maxLum[0] * 10^lumPrecision for Red
     * @param GinvGamma the inv gamma function from 0 to maxLum[1] * 10^lumPrecision for Green
     * @param BinvGamma the inv gamma function from 0 to maxLum[2] * 10^lumPrecision for Blue
     *
     * @throws IllegalArgumentException For negative luminances, unsorted gammas, gamma values not in [0,1]
     *
     * @since 0.0.1
     */
    public Calibration(
        double lumPrecision,
        double maxLum, 
        int maxPixel, 
        double[] RinvGamma, double[] GinvGamma, double[] BinvGamma) {

        if (maxLum < 0)
            throw new IllegalArgumentException(String.format(WRONG_MAX_LUMINANCE));
      
        this.scale = Math.pow(10, lumPrecision);
        this.maxPixel = maxPixel;
        this.maxLum = maxLum;

        IntStream.range(0, 3).forEach(i -> {
        long l = Math.round(scale * maxLum) + 1;
        if (RinvGamma.length != l)
            throw new IllegalArgumentException(String.format(WRONG_INV_GAMMA_SIZE, "Red", l, RinvGamma.length));
        });
      
        if (IntStream.range(1, RinvGamma.length).anyMatch(i -> RinvGamma[i - 1] > RinvGamma[i]) ||
            IntStream.range(1, GinvGamma.length).anyMatch(i -> GinvGamma[i - 1] > GinvGamma[i]) ||
            IntStream.range(1, BinvGamma.length).anyMatch(i -> BinvGamma[i - 1] > BinvGamma[i]))
            throw new IllegalArgumentException(UNSORTED_GAMMA_FUNCTION);

        if (RinvGamma[0] < 0 || GinvGamma[0] < 0 || BinvGamma[0] < 0)
            throw new IllegalArgumentException(ILLEGAL_GAMMA_FUNCTION);

        if (RinvGamma[RinvGamma.length - 1] > maxPixel || GinvGamma[GinvGamma.length - 1] > maxPixel || BinvGamma[BinvGamma.length - 1] > maxPixel)
            throw new IllegalArgumentException(ILLEGAL_GAMMA_FUNCTION);
      
        this.inverseGamma = new double[][] {
            DoubleStream.of(RinvGamma).map(i -> i / (double)maxPixel).toArray(),
            DoubleStream.of(GinvGamma).map(i -> i / (double)maxPixel).toArray(),
            DoubleStream.of(BinvGamma).map(i -> i / (double)maxPixel).toArray()
        };
    }

    public double getMaxLum() { return this.maxLum;}
    public int getMaxPixel() { return this.maxPixel;}
    public double getLumPrecision() { return this.lumPrecision;}

    /**
     * Obtain pixel level (0:1) from luminance in cd/m^2 from the inverse gamma function
     *
     * @param lum The [0]=R [1]=G [2]=B luminances value in cd/m^2
     * 
     * @return the device-dependent pixel levels between 0 and 1 for R, G, B
     * 
     * @since 0.0.1
     */
    public double[] getColorValues(double[] lum) {
        double[] color = new double[4];

        color[3] = 1.0;  // alpha
       
        IntStream.range(0, 3).forEach(i -> {
            if (lum[i] > maxLum) {
                System.err.println("Luminance out of range: " + lum[i] + " using " + maxLum);
                lum[i] = maxLum;
            }
            int index = (int)Math.round(scale * lum[i]);
            color[i] = inverseGamma[i][index];
        });
       
        return color;
    }
}