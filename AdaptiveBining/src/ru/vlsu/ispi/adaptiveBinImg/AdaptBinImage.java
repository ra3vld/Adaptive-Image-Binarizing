/*
 Adaptive binarization of image with using simple window of averaging.
 Highly is not optimal.

 */
package ru.vlsu.ispi.adaptiveBinImg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import javax.imageio.ImageIO;

/**
 *
 * @author 1
 */
public class AdaptBinImage {

    /**
     * @param args the command line arguments -i input file, 
     -o output file 
     -w window size (default 10) 
     -t threshold for computing in single task (default 100) 
     -p size of pool 
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        int threshold = 100;
        int windowSize = 10;
        int poolSize = 8;
        String inputPath = "C:\\Users\\1\\Dropbox\\Public\\pict\\Wed May 25 20-36-41.jpg";
        String outputPath = "out.png";

        String param;
        String info;
        int argIndex = 0;
        while (!"".equals(param = getNextaram(args, argIndex++))) {
            if (!"".equals(info = getNextaram(args, argIndex++))) {
                if ("-i".equals(param)) {
                    inputPath = info;
                } else if ("-o".equals(param)) {
                    outputPath = info;
                } else if ("-t".equals(param)) {
                    threshold = Integer.getInteger(info);
                } else if ("-w".equals(param)) {
                    windowSize = Integer.getInteger(info);
                } else if ("-p".equals(param)) {
                    poolSize = Integer.getInteger(info);
                }
            }
        }
        File inputFile = new File(inputPath);
        if (inputFile.exists()) {
            BufferedImage source = ImageIO.read(inputFile);
            BufferedImage dst = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

            ForkJoinPool fjp = new ForkJoinPool(poolSize);
            ForkBinarize fb = new ForkBinarize(source, dst, 0, source.getHeight(), threshold, windowSize);

            //(sic!)curemt realisation too much slow.
            fjp.invoke(fb);

            ImageIO.write(dst, "png", new File(outputPath));
        } else {
            System.err.println("File not found!");
        }
    }

    private static String getNextaram(String[] args, int index) {
        String result = "";
        if (args.length > index) {
            result = args[index];
        }
        return result;
    }
}
