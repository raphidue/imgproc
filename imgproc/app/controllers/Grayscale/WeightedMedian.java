package controllers.Grayscale;

import controllers.Helper;

import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//for buffered image
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class WeightedMedian {
    public static ObjectNode processing(JsonNode json, String PATH) {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();

        // convert json to matrix
        double[][] filter;
        filter = Helper.convertJsonToMatrix(json);

        String id = json.findPath("id").toString();
        String uploadPath = PATH + "/" + id + ".png";

        try {
            BufferedImage im = ImageIO.read(new File(uploadPath));

            int w = im.getWidth();
            int h = im.getHeight();

            // border extension
            BufferedImage copy;
            copy = Helper.copyImage(im, "CONTINUE");

            // filter operation
            int[] P = new int[9];

            for (int v = 1; v <= h; v++) {
                for (int u = 1; u <= w; u++) {

                    // fill the pixel vector P for filter position (u,v)
                    int k = 0;
                    for (int j = -1; j <= 1; j++) {
                        for (int i = -1; i <= 1; i++) {
                            double c = filter[j + 1][i + 1];
                            P[k] = Helper.getPix(copy, u + i, v + j) * (int) c;
                            k++;
                        }
                    }
                    // sort the pixel vector and take the middle element
                    Arrays.sort(P);
                    Helper.setPix(im, u - 1, v - 1, P[4]);
                }
            }
            ImageIO.write(im, "PNG", new File(uploadPath));

            // create histogram
            respJSON = Helper.generateHisto(id + ".png");
        } catch (IOException ioe) {
            respJSON.put("error", "Error on processing weighted median-filter...");
        }
        return respJSON;

    }
}