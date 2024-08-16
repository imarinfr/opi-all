package org.lei.opi.core.definitions;

/**
 * Implement a circular array that can return the median.
 * Initially all values are set to the first entry.
 * Uses insertion sort assuming that the array is small.
 */
public class MedianList {

    /* The size of the array */
    private int size;

    /** Current values */
    private double []values;
    private double []tempValues; // for insertion sort

    /** Index into {@link values} of next available spot */
    private int index;

    public MedianList(int n) {
        size = n;
        values = null;
    }

    /**
     * Return Median of all values after x has been added.
     *
     * @param x New value to add
     * @return Median of all values after x has been added
     */
    public double getMedian(double x) {
            // First time around, fill values with value (also allocate the arrays).
        if (values == null) {
            tempValues = new double[size];
            values = new double[size];
            for (int i = 0 ; i < size ; i++)
                values[i] = x;
            index = 0;
            return x;
        } 

        values[index] = x;
        index = (index + 1) % size;
            
            // insertion sort temp int tempValues
        tempValues[0] = values[0];
        for (int i = 1; i < size ; i++) {
            tempValues[i] = values[i];
            for (int j = i; j > 0 && tempValues[j-1] > tempValues[j]; j--){
                double temp = tempValues[j];
                tempValues[j] = tempValues[j-1];
                tempValues[j-1] = temp;
            }
        }

        if (size % 2 == 0)
            return (tempValues[size / 2 - 1] + tempValues[size / 2]) / 2;
        else
            return tempValues[size / 2];
    }
}