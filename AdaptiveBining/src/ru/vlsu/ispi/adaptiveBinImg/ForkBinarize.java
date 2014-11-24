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

    boolean adaptive = false;
    int threshold;
    int windowSize = 0;
    int brightTreshold = 128;
    int[][] raster;

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
        for (int y = start; y < start + length; y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int color;
                bright = getAvgBright(source.getRGB(x, y));

                avgBright = getWindowAvg(source, x, y, windowSize);

                if (bright - avgBright < -25 || bright - avgBright > 15) {
                    color = bright >= avgBright ? white : black;
                } else {
                    color = bright > brightTreshold ? white : black;

                }
                destination.setRGB(x, y, color);
            }
        }
    }

    private static int getAvgBright(int rgba) {
        int sumBrightB = rgba & 0x000000ff;
        int sumBrightG = (rgba & 0x0000ff00) >> 8;
        int sumBrightR = (rgba & 0x00ff0000) >> 16;
        return (sumBrightB + sumBrightG + sumBrightR) / 3;
    }

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

        int sumBrightR = 0, sumBrightG = 0, sumBrightB = 0;
        int pixel;
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                pixel = img.getRGB(x, y);
                sumBrightB += pixel & 0x000000ff;
                sumBrightG += (pixel & 0x0000ff00) >> 8;
                sumBrightR += (pixel & 0x00ff0000) >> 16;
            }
        }
        int lengthX = maxX - minX;
        int lengthY = maxY - minY;
        int sumBright = (sumBrightB + sumBrightG + sumBrightR) / 3;
        int avgBright = sumBright / (lengthX * lengthY);
        return avgBright;
    }

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
