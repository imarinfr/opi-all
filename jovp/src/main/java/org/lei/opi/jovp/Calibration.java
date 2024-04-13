package org.lei.opi.jovp;

import java.util.stream.IntStream;
import java.util.Arrays;


/**
 * Calibration data: gamma and inverse gamma functions.
 * 
 * @since 0.2.0
 */
public class Calibration {
    /** {@value WRONG_MAX_LUMINANCE} */
    private static final String WRONG_MAX_LUMINANCE = "Maximum luminance for the gamma functions cannot be negative";
    /** {@value WRONG_MAX_PIXEL} */
    private static final String WRONG_MAX_PIXEL = "Maximum pixel for the gamma functions cannot be negative";
    /** {@value WRONG_GAMMA_SIZE} */
    private static final String INCONSISTENT_GAMMA_SIZES = "Inconsistent lengths for the gamma functions. Lengths for R, G, and B where %s, %s, and %s, respectively";
    /** {@value UNSORTED_GAMMA_FUNCTION} */
    private static final String UNSORTED_GAMMA_FUNCTION = "The gamma function needs to be sorted";
    /** {@value WRONG_GAMMA_VALUE} */
    private static final String WRONG_GAMMA_VALUE = "Invalid gamma function. Some values our outside the range [0, 1]";
   
    /** Max luminance in cd/m^2 for [0]=R [1]=G [2]=B */
    double[] maxLum;
    /** Max pixel value for channels [0]=R [1]=G [2]=B */
    double[] maxPixel;
    /** For channels [0]=R [1]=G [2]=B, for each pixel levels [0, maxPixel), the luminance divided by maxLum  */
    double[][] gamma;
    /** For channels [0]=R [1]=G [2]=B, color values (ie pixel value / maxPixeL) in range [0,1] for integer lum values in cd/m^2 up to 0:maxLum */
    double[][] inverseGamma;

    /**
     * Calibration factory to create a Calibration object from 
     * 
     * @param RmaxLum maximum luminance in cd/m2 for Red
     * @param GmaxLum maximum luminance in cd/m2 for Green
     * @param BmaxLum maximum luminance in cd/m2 for Blue
     * @param Rgamma the gamma function from 0 to 1 for Red
     * @param Ggamma the gamma function from 0 to 1 for Green
     * @param Bgamma the gamma function from 0 to 1 for Blue
     *
     * @throws IllegalArgumentException For negative luminances, unsorted gammas, gamma values not in [0,1]
     *
     * @since 0.0.1
     */
    public Calibration(
        double RmaxLum, double GmaxLum, double BmaxLum, 
        double RmaxPixel, double GmaxPixel, double BmaxPixel, 
        double[] Rgamma, double[] Ggamma, double[] Bgamma) {

        if (RmaxLum < 0 || GmaxLum < 0 || BmaxLum < 0)
            throw new IllegalArgumentException(String.format(WRONG_MAX_LUMINANCE));

        if (RmaxPixel < 0 || RmaxPixel < 0 || RmaxPixel < 0)
            throw new IllegalArgumentException(String.format(WRONG_MAX_PIXEL));
      
        if (Rgamma.length != Ggamma.length || Rgamma.length != Bgamma.length)
            throw new IllegalArgumentException(String.format(INCONSISTENT_GAMMA_SIZES, Rgamma.length, Ggamma.length, Bgamma.length));
      
        if (IntStream.range(1, Rgamma.length).anyMatch(i -> Rgamma[i - 1] > Rgamma[i]) ||
            IntStream.range(1, Ggamma.length).anyMatch(i -> Ggamma[i - 1] > Ggamma[i]) ||
            IntStream.range(1, Bgamma.length).anyMatch(i -> Bgamma[i - 1] > Bgamma[i]))
            throw new IllegalArgumentException(UNSORTED_GAMMA_FUNCTION);
      
        if (Rgamma[0] < 0 || Rgamma[Rgamma.length - 1] > 1 ||
            Ggamma[0] < 0 || Ggamma[Ggamma.length - 1] > 1 ||
            Bgamma[0] < 0 || Bgamma[Bgamma.length - 1] > 1)
            throw new IllegalArgumentException(WRONG_GAMMA_VALUE);

        this.maxLum = new double[] {RmaxLum, GmaxLum, BmaxLum};
        this.maxPixel = new double[] {RmaxPixel, GmaxPixel, BmaxPixel};
        this.gamma = new double[][] {Rgamma, Ggamma, Bgamma};
        calculateInverse();
    }

    public double[] getMaxLum() { return this.maxLum;}

    /**
     * Fill in this.inverseGamma from this.gamma.
     * inverseGamma[.][lum] = gamma[.][closest gamma value to lum/maxLum]
     *
     * @since 0.0.1
     */
    private void calculateInverse() {
        int maxAllL = (int)Math.round(Arrays.stream(this.maxLum).max().getAsDouble());

        this.inverseGamma = new double[3][maxAllL + 1];
        for (int rgb = 0 ; rgb <= 2 ; rgb++) {
            int gammaIndex = 0; // this will move along gamma to the right finding the closest lum value
            for(int lum = 0 ; lum <= maxAllL ; lum++) {
                    // check if we should increment gammaIndex (but not beyond max)
                while (gammaIndex < this.gamma[rgb].length - 1   
                   && (Math.abs(lum / maxLum[rgb] - gamma[rgb][gammaIndex + 1]) < Math.abs(lum / maxLum[rgb] - gamma[rgb][gammaIndex])))
                    gammaIndex++;

                this.inverseGamma[rgb][lum] = (double)gammaIndex / this.maxPixel[rgb];
            }
        }
    }
  
    /**
     * Obtain pixel level (0:1) from luminance in cd/m^2 from the inverse gamma function
     *
     * @param luminances The [0]=R [1]=G [2]=B luminances value in cd/m^2
     * 
     * @return the device-dependent pixel level between 0 and 1
     * 
     * @throws IllegalArgumentException If any value is bad
     * 
     * @since 0.0.1
     */
    public double[] getColorValues(double[] luminances) {
      double[] color = new double[4];
      color[3] = 1.0;  // alpha

      for (int rgb = 0 ; rgb <= 2 ; rgb++)
        color[rgb] = luminances[rgb] < 0 ? 
            0 : 
            (luminances[rgb] > maxLum[rgb] ? 
                1 : 
                this.inverseGamma[rgb][(int)Math.round(luminances[rgb])]);

System.out.println("getColorValues: " + Arrays.toString(luminances) + " -> " + Arrays.toString(color));
      return color;
    }
  
}