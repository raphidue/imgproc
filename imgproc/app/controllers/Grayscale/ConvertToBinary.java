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
import java.net.URL;

public class ConvertToBinary {
    public static ObjectNode processing(JsonNode json, String PATH) {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();

        String id = json.findPath("id").toString();

        // threshold value for manually binary conversion
        int threshold = Integer.parseInt(json.findPath("threshold").toString());
        String uploadPath = PATH + id + ".png";

        try {
            System.out.println("================================================" + uploadPath);
            BufferedImage im = ImageIO.read(new URL(uploadPath));

            // check if 8-bit image
            if (im.getType() == 10) {
                // convert to binary by the threshold-value
                im = Helper.getBinaryImage(threshold, im);
            }
            String idPlusPng = id + ".png";
            Helper.uploadBufferedImageToAws(im, idPlusPng);
            // create histogram
            respJSON = Helper.generateBinaryHisto(id + ".png");
        } catch (Exception ioe) {
            respJSON.put("error", "Error on processing to convert to binary...");
        }
        return respJSON;
    }
}