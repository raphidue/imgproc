package controllers;

import play.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//for buffered image
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class Helper {

    // constant upload path
    public static final String PATH = Play.application().path().getAbsolutePath() + "/public/uploads";

    // generate a histogram for 8-bit images
    public static ObjectNode generateHisto(String id) {
        int[] H = new int[256];
        String tmp;
        Integer tmpInt;
        ObjectNode respJSON = Json.newObject();

        try {
            // read image
            BufferedImage im = ImageIO.read(new File(PATH + "/" + id));

            int w = im.getWidth();
            int h = im.getHeight();

            // iterate trough image
            for (int v = 0; v < h; v++) {
                for (int u = 0; u < w; u++) {
                    // count pixel values in array (histogram)
                    int i = getPix(im, u, v);
                    H[i] = H[i] + 1;
                }
            }
            // convert histogram array to json object
            for (int i = 0; i < H.length; i++) {
                tmpInt = new Integer(i);
                tmp = tmpInt.toString();
                respJSON.put(tmp, new Integer(H[i]));
            }
        } catch (IOException ioe) {
            respJSON.put("error", "Error on generating a histogram...");
        }
        return respJSON;
    }

    // generate histogram for binary images
    public static ObjectNode generateBinaryHisto(String id) {
        int[] H = new int[2];
        String tmp;
        Integer tmpInt;
        ObjectNode respJSON = Json.newObject();

        try {
            // read image
            BufferedImage im = ImageIO.read(new File(PATH + "/" + id));

            int w = im.getWidth();
            int h = im.getHeight();

            // iterate trough image
            for (int v = 0; v < h; v++) {
                for (int u = 0; u < w; u++) {
                    // count pixel values in array (histogram)
                    int i = getPix(im, u, v);
                    H[i] = H[i] + 1;
                }
            }
            // convert histogram to json object
            for (int i = 0; i < H.length; i++) {
                tmpInt = new Integer(i);
                tmp = tmpInt.toString();
                respJSON.put(tmp, new Integer(H[i]));
            }
        } catch (IOException ioe) {
            respJSON.put("error", "Error on generating a binary histogram...");
        }
        return respJSON;
    }

    // convert a 8-bit image to binary image by threshold-value
    public static BufferedImage getBinaryImage(int threshold, BufferedImage greyImage) {
        int w = greyImage.getWidth();
        int h = greyImage.getHeight();
        BufferedImage binaryImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

        // iterate trough image
        for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
                // check lesser or greater than threshold value
                if (getPix(greyImage, u, v) >= threshold) {
                    setPix(binaryImage, u, v, 1);
                } else {
                    setPix(binaryImage, u, v, 0);
                }
            }
        }
        return binaryImage;
    }

    // apply a border extension to a image
    public static BufferedImage copyImage(BufferedImage src, String mode) {

        int w = src.getWidth();
        int h = src.getHeight();

        // copy image with larger width and height
        BufferedImage copy = new BufferedImage(w + 2, h + 2, BufferedImage.TYPE_BYTE_GRAY);

        int intMode = modeStringToInt(mode);

        switch (intMode) {
            // white border
            case 0:
                for (int v = 0; v < h + 2; v++) {
                    for (int u = 0; u < w + 2; u++) {
                        setPix(copy, u, v, 255);
                    }
                }
                // copy image in extended image
                for (int v = 0; v < h; v++) {
                    for (int u = 0; u < w; u++) {
                        setPix(copy, u + 1, v + 1, getPix(src, u, v));
                    }
                }
                break;
            // black border
            case 1:
                for (int v = 0; v < h + 2; v++) {
                    for (int u = 0; u < w + 2; u++) {
                        setPix(copy, u, v, 0);
                    }
                }
                // copy image in extended image
                for (int v = 0; v < h; v++) {
                    for (int u = 0; u < w; u++) {
                        setPix(copy, u + 1, v + 1, getPix(src, u, v));
                    }
                }
                break;
            // continue extension
            case 2:
                // copy image in extended image
                for (int v = 0; v < h; v++) {
                    for (int u = 0; u < w; u++) {
                        setPix(copy, u + 1, v + 1, getPix(src, u, v));
                    }
                }
                // upper border
                for (int u = 0; u < w + 2; u++) {
                    setPix(copy, u, 0, getPix(copy, u, 1));
                }

                // bottom border
                for (int u = 0; u < w + 2; u++) {
                    setPix(copy, u, h + 1, getPix(copy, u, h));
                }

                // left border
                for (int v = 0; v < h + 2; v++) {
                    setPix(copy, 0, v, getPix(copy, 1, v));
                }

                // right border
                for (int v = 0; v < h + 2; v++) {
                    setPix(copy, w + 1, v, getPix(copy, w, v));
                }

                // left upper corner
                setPix(copy, 0, 0, getPix(copy, 1, 1));

                // left lower corner
                setPix(copy, 0, h + 1, getPix(copy, 1, h));

                // right upper corner
                setPix(copy, w + 1, 0, getPix(copy, w, 1));

                // right lower corner
                setPix(copy, w + 1, h + 1, getPix(copy, w, h));
                break;
            default:
        }

        return copy;
    }

    // apply a border extension to a binary image
    public static BufferedImage copyBinaryImage(BufferedImage src, String mode) {

        int w = src.getWidth();
        int h = src.getHeight();

        // copy image with larger width and height
        BufferedImage copy = new BufferedImage(w + 2, h + 2, BufferedImage.TYPE_BYTE_BINARY);

        int intMode = modeStringToInt(mode);

        switch (intMode) {
            // white border
            case 0:
                for (int v = 0; v < h + 2; v++) {
                    for (int u = 0; u < w + 2; u++) {
                        setPix(copy, u, v, 1);
                    }
                }
                // copy image to extended image
                for (int v = 0; v < h; v++) {
                    for (int u = 0; u < w; u++) {
                        setPix(copy, u + 1, v + 1, getPix(src, u, v));
                    }
                }
                break;
            // black border
            case 1:
                for (int v = 0; v < h + 2; v++) {
                    for (int u = 0; u < w + 2; u++) {
                        setPix(copy, u, v, 0);
                    }
                }
                // copy image to extended image
                for (int v = 0; v < h; v++) {
                    for (int u = 0; u < w; u++) {
                        setPix(copy, u + 1, v + 1, getPix(src, u, v));
                    }
                }
                break;
            // continue extension
            case 2:
                // copy image to extended image
                for (int v = 0; v < h; v++) {
                    for (int u = 0; u < w; u++) {
                        setPix(copy, u + 1, v + 1, getPix(src, u, v));
                    }
                }

                // upper border
                for (int u = 0; u < w + 2; u++) {
                    setPix(copy, u, 0, getPix(copy, u, 1));
                }

                // lower border
                for (int u = 0; u < w + 2; u++) {
                    setPix(copy, u, h + 1, getPix(copy, u, h));
                }

                // left border
                for (int v = 0; v < h + 2; v++) {
                    setPix(copy, 0, v, getPix(copy, 1, v));
                }

                // right border
                for (int v = 0; v < h + 2; v++) {
                    setPix(copy, w + 1, v, getPix(copy, w, v));
                }

                // upper left corner
                setPix(copy, 0, 0, getPix(copy, 1, 1));

                // lower left corner
                setPix(copy, 0, h + 1, getPix(copy, 1, h));

                // upper right corner
                setPix(copy, w + 1, 0, getPix(copy, w, 1));

                // lower right corner
                setPix(copy, w + 1, h + 1, getPix(copy, w, h));
                break;
            default:
        }

        return copy;
    }

    // convert json object to filter matrix
    public static double[][] convertJsonToMatrix(JsonNode json) {
        double[][] filter = new double[3][3];

        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                String str = "r" + r + "c" + c;
                double tmp = Double.parseDouble(json.findPath(str).toString());
                filter[r - 1][c - 1] = tmp;
            }
        }

        return filter;
    }

    // convert json object to binary filter matrix
    public static int[][] convertBinaryJsonToMatrix(JsonNode json) {
        int[][] filter = new int[3][3];

        for (int r = 1; r <= 3; r++) {
            for (int c = 1; c <= 3; c++) {
                String str = "r" + r + "c" + c;
                int tmp = Integer.parseInt(json.findPath(str).toString());
                filter[r - 1][c - 1] = tmp;
            }
        }

        return filter;
    }

    // check condition for pixel bounds
    public static int checkPixel(int pixel) {
        if (pixel > 255)
            pixel = 255;
        if (pixel < 0)
            pixel = 0;
        return pixel;
    }

    // conversions for border extensions
    public static int modeStringToInt(String mode) {
        int intMode = -1;

        if (mode.contains("WHITE")) {
            intMode = 0;
        } else if (mode.contains("BLACK")) {
            intMode = 1;

        } else {
            intMode = 2;
        }

        return intMode;
    }

    public static void setPix(BufferedImage im, int x, int y, int value) {
        im.getRaster().setSample(x, y, 0, value);
    }

    public static int getPix(BufferedImage im, int x, int y) {
        return im.getRaster().getPixel(x, y, (int[]) null)[0];
    }
}
