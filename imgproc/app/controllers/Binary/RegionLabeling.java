package controllers.Binary;

import controllers.Helper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import java.util.*;
import java.net.URL;


public class RegionLabeling {
    public static ObjectNode processing(JsonNode json, String PATH) {
        ObjectNode respJSON = Json.newObject();

        String id = json.findPath("id").toString();
        String uploadPath = PATH + id + ".png";

        try {
            BufferedImage im = ImageIO.read(new URL(uploadPath));

            // check if a 8-bit image
            if (im.getType() == 10) {
                // convert to binary image withe the threshold-value 127
                im = Helper.getBinaryImage(127, im);
            }

            // map to hold the label collissions
            Map<Collision, Collision> collisionMap = new HashMap<Collision, Collision>();

            int w = im.getWidth();
            int h = im.getHeight();

            // neighbours-array
            int[] neighbours = new int[4];
            int label;
            int foregroundPix;

            // border extension
            BufferedImage processing;
            processing = Helper.copyBinaryImage(im, "CONTINUE");

            // create 8-bit copy from binary image, to processing region labeling
            BufferedImage copy;
            copy = new BufferedImage(processing.getWidth(), processing.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

            // copy binary to 8-bit
            for (int y = 0; y < processing.getHeight(); y++) {
                for (int x = 0; x < processing.getWidth(); x++) {
                    Helper.setPix(copy, x, y, Helper.getPix(processing, x, y));
                }
            }

            //PASS 1 - ASSIGN INITIAL LABELS
            label = 2;

            for (int y = 1; y <= h; y++) {
                for (int x = 1; x <= w; x++) {

                    // new labelpixel reached
                    if (Helper.getPix(copy, x, y) == 1) {

                        // reset neighbours array and count of foreground-pixels
                        foregroundPix = 0;
                        neighbours[0] = 0;
                        neighbours[1] = 0;
                        neighbours[2] = 0;
                        neighbours[3] = 0;

                        // -------------------------- check Neighbours ---------------------------

                        // check top pixel
                        if (Helper.getPix(copy, x, y - 1) > 1) {
                            foregroundPix++;
                            neighbours[0] = Helper.getPix(copy, x, y - 1);
                        }
                        // check left pixel
                        if (Helper.getPix(copy, x - 1, y) > 1) {
                            foregroundPix++;
                            neighbours[1] = Helper.getPix(copy, x - 1, y);
                        }
                        // check topleft pixel
                        if (Helper.getPix(copy, x - 1, y - 1) > 1) {
                            foregroundPix++;
                            neighbours[2] = Helper.getPix(copy, x - 1, y - 1);
                        }
                        // check topright pixel
                        if (Helper.getPix(copy, x + 1, y - 1) > 1) {
                            foregroundPix++;
                            neighbours[3] = Helper.getPix(copy, x + 1, y - 1);
                        }

                        // all neighbours are background pixels
                        if (foregroundPix == 0) {
                            Helper.setPix(copy, x, y, label);
                            label++;
                            // exactly one of the neighbours has a label value, no conflict
                        } else if (foregroundPix == 1) {
                            for (int i = 0; i < 4; i++) {
                                // select the first value which appears in array
                                if (neighbours[i] != 0) {
                                    Helper.setPix(copy, x, y, neighbours[i]);
                                    break;
                                }
                            }
                            // serveral neighbours have label values
                        } else if (foregroundPix > 1) {
                            boolean firstEntry = true;
                            int tmp = 0;
                            for (int i = 0; i < 4; i++) {
                                if (neighbours[i] != 0) {
                                    // select the first appaering label value
                                    if (firstEntry == true) {
                                        tmp = neighbours[i];
                                        Helper.setPix(copy, x, y, tmp);
                                        firstEntry = false;
                                        // all other neighbours register in collisionMap
                                    } else if (tmp != neighbours[i]) {

                                        Collision c;

                                        // first position should be lesser than second position
                                        if (tmp > neighbours[i])
                                            c = new Collision(neighbours[i], tmp);
                                        else
                                            c = new Collision(tmp, neighbours[i]);

                                        // register collissions
                                        if (!collisionMap.containsKey(c))
                                            collisionMap.put(c, c);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PASS 2 - RESOLVE LABEL COLLISIONS

            // the table setNumber[i] indicates to which set the element i belongs:
            // k == setNumber[e] means that e is in set k
            int[] setNumber = new int[label];

            // initially, we assign a separate set to each element e:
            for (int e = 0; e < label; e++) {
                setNumber[e] = e;
            }

            // inspect all collissions c=(a,b) one by one [note that a<b]
            for (Collision c : collisionMap.keySet()) {
                int A = setNumber[c.a]; // element a is currently in set A
                int B = setNumber[c.b]; // element b is currently in set B

                // Merge sets A and B (unless they are the same) by
                // moving all elements of set B into set A
                if (A != B) {
                    for (int i = 0; i < label; i++) {
                        if (setNumber[i] == B)
                            setNumber[i] = A;
                    }
                }
            }

            // PASS 3 - RELABEL THE IMAGE
            for (int y = 1; y <= h; y++) {
                for (int x = 1; x <= w; x++) {
                    // relabel
                    if (setNumber[Helper.getPix(copy, x, y)] != 0) {
                        Helper.setPix(copy, x, y, setNumber[Helper.getPix(copy, x, y)]);
                    }
                }
            }

            // copy to output image
            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 1; y <= h; y++) {
                for (int x = 1; x <= w; x++) {
                    Helper.setPix(out, x - 1, y - 1, Helper.getPix(copy, x, y));
                }
            }
            Helper.uploadBufferedImageToAws(im, id + ".png");

            respJSON.put("success", "Success on processing region labeling...");
        } catch (IOException ioe) {
            respJSON.put("error", "Error on processing region labeling...");
        }
        return respJSON;
    }
}