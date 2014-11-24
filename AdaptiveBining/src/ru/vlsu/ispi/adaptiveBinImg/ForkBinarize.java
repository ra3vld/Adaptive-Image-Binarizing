/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vlsu.ispi.adaptiveBinImg;

import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveAction;

/**
 *
 * @author 1
 */
public class ForkBinarize extends RecursiveAction {

    BufferedImage source;
    BufferedImage destination;
    int start;
    int length;

    int threshold;
    int windowSize = 0;
    int brightTreshold = 128;

    /**
     *
     * @param source - source image
     * @param destination - result image
     * @param start - start line for task
     * @param length - number of line for task
     * @param threshold - maximal length
     * @param windowSize - side of square for averaging bright
     */
    public ForkBinarize(BufferedImage source, BufferedImage destination, int start, int length, int threshold, int windowSize) {
        this.source = source;
        this.destination = destination;
        this.start = start;
        this.length = length;
        this.threshold = threshold;
        this.windowSize = windowSize;
    }

    private void computeDirectly() {
        int avgBright = 0;
        int bright;
        int white = 0x00ffffff;
        int black = 0;
        //bypassing pixels in this task
        for (int y = start; y < start + length; y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int color;
                
                bright = getAvgBrightOfPixel(source.getRGB(x, y));

                avgBright = getWindowAvg(source, x, y, windowSize);
                //optimization for noncontrast image
                if (bright - avgBright < -25 || bright - avgBright > 15) {
                    color = bright >= avgBright ? white : black;
                } else {
                    color = bright > brightTreshold ? white : black;
                }
                destination.setRGB(x, y, color);
            }
        }
    }
    //get average bright of pixel
    private int getAvgBrightOfPixel(int rgba) {
        int sumBrightB = rgba & 0x000000ff;
        int sumBrightG = (rgba & 0x0000ff00) >> 8;
        int sumBrightR = (rgba & 0x00ff0000) >> 16;
        return (sumBrightB + sumBrightG + sumBrightR) / 3;
    }

    //processing avarege bright of window square with center with current pixel
    //for border pixel all aboard pixel doesn't calculate
    //(sic!) getRGB so slow; with simple int[][] instead Image up to 10 times faster
    //(sic!) every pixel calculating new window, its very slowly
    //
    private int getWindowAvg(BufferedImage img, int centerX, int centerY, int windowsSize) {
        int halfWin = windowsSize / 2;
        int minX = centerX - halfWin;
        minX = minX < 0 ? 0 : minX;
        int maxX = centerX + halfWin;
        maxX = maxX > img.getWidth() ? img.getWidth() : maxX;

        int minY = centerY - halfWin;
        minY = minY < 0 ? 0 : minY;
        int maxY = centerY + halfWin;
        maxY = maxY > img.getHeight() ? img.getHeight() : maxY;

        int sumBright = 0;
        int pixel;
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                pixel = img.getRGB(x, y);
                sumBright += getAvgBrightOfPixel(pixel);
            }
        }
        int lengthX = maxX - minX;
        int lengthY = maxY - minY;
        int avgBright = sumBright / (lengthX * lengthY);
        return avgBright;
    }

    //task is divided by lines
    @Override
    protected void compute() {
        if (length < threshold) {//compute directly
            computeDirectly();
        } else {
            int split = length / 2;

            invokeAll(new ForkBinarize(source, destination, start, split, threshold, windowSize),
                    new ForkBinarize(source, destination, start + split, length - split, threshold, windowSize));
        }
    }

}
