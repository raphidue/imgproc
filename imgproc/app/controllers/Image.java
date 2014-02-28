package controllers;

import controllers.Binary.Collision;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import play.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//for buffered image
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class Image extends Controller {
    // constant upload path
    public static final String PATH = Play.application().path().getAbsolutePath() + "/public/uploads";

    // upload image
    public static Result upload(String id) {
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart picture = body.getFile("picture");
        File theDir = new File(PATH);

        // create directory if not exists
        if (!theDir.exists()) {
            theDir.mkdir();
        }


        if (picture != null) {
            File file = picture.getFile();

            String myUploadPath = PATH + "/" + id;
            file.renameTo(new File(myUploadPath));

            try {
                // read image
                BufferedImage input = ImageIO.read(new File(myUploadPath));

                // 8-bit image
                BufferedImage im = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g2d = im.createGraphics();

                // convert image to 8-bit image
                g2d.drawImage(input, 0, 0, null);

                // write image to filesystem
                ImageIO.write(im, "PNG", new File(myUploadPath));

            } catch (IOException ioe) {
            }
            return ok(views.html.image.render(id));
        } else {
            flash("error", "Missing file");

            // redirect to home
            return redirect(routes.Application.index());
        }
    }

    // function to show histogram as json-object
    public static Result showHist(String id) {
        ObjectNode respJSON = Json.newObject();

        // generate the json from image id
        respJSON = generateHisto(id);

        return ok(respJSON);
    }

    // function to count regions in a labeled image
    public static Result showLabels(String id) {

        ObjectNode respJSON = Json.newObject();

        try {
            // read image
            BufferedImage im = ImageIO.read(new File(PATH + "/" + id));

            // create histogram
            int w = im.getWidth();
            int h = im.getHeight();

            // hashset for distinct selection
            HashSet<Integer> distinct = new HashSet<Integer>();

            // iterate trough image
            for (int v = 0; v < h; v++) {
                for (int u = 0; u < w; u++) {
                    distinct.add(getPix(im, u, v));
                }
            }

            // - 1: dont count the 0 value
            respJSON.put("labels", distinct.size() - 1);

        } catch (IOException ioe) {
            respJSON.put("error", "Error in showLabels()...");
        }
        return ok(respJSON);
    }

    // function to show a binary histogram as json-object
    public static Result showBinaryHist(String id) {
        ObjectNode respJSON = Json.newObject();

        // generate binary histogram from image-id
        respJSON = generateBinaryHisto(id);

        return ok(respJSON);
    }

    public static Result smoothing() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in smoothing-filter...");
        } else {
            double[][] filter;
            String id = json.findPath("id").toString();

            // convert filter-json to filter matrix
            filter = convertJsonToMatrix(json);
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // create histogram
                int w = im.getWidth();
                int h = im.getHeight();

                // border extenstion
                BufferedImage copy;
                copy = copyImage(im, "BLACK");

                // apply filter
                for (int v = 1; v <= h; v++) {
                    for (int u = 1; u <= w; u++) {
                        double sum = 0;
                        for (int j = -1; j <= 1; j++) {
                            for (int i = -1; i <= 1; i++) {
                                // get pixel
                                int p = getPix(copy, u + i, v + j);
                                // get the corresponding filter coefficient:
                                double c = filter[j + 1][i + 1];
                                sum = sum + c * p;
                            }
                        }

                        int q = (int) Math.round(sum);

                        // check pixel bounds
                        q = checkPixel(q);

                        // set Pixel
                        setPix(im, u - 1, v - 1, q);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // generate histogram as json
                respJSON = generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing smoothing-filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result median() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in median-filter...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // create histogram
                int w = im.getWidth();
                int h = im.getHeight();

                // border extension
                BufferedImage copy;
                copy = copyImage(im, "CONTINUE");

                // filter operation
                int[] P = new int[9];

                for (int v = 1; v <= h; v++) {
                    for (int u = 1; u <= w; u++) {

                        // fill the pixel vector P for filter position (u,v)
                        int k = 0;
                        for (int j = -1; j <= 1; j++) {
                            for (int i = -1; i <= 1; i++) {
                                P[k] = getPix(copy, u + i, v + j);
                                k++;
                            }
                        }
                        // sort the pixel vector and take middle element
                        Arrays.sort(P);
                        setPix(im, u - 1, v - 1, P[4]);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // generate histogram as json
                respJSON = generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing median filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result weightedMedian() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        // convert json to matrix
        double[][] filter;
        filter = convertJsonToMatrix(json);

        if (json == null) {
            return badRequest("Expecting Json data in weighted median-filter...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                int w = im.getWidth();
                int h = im.getHeight();

                // border extension
                BufferedImage copy;
                copy = copyImage(im, "CONTINUE");

                // filter operation
                int[] P = new int[9];

                for (int v = 1; v <= h; v++) {
                    for (int u = 1; u <= w; u++) {

                        // fill the pixel vector P for filter position (u,v)
                        int k = 0;
                        for (int j = -1; j <= 1; j++) {
                            for (int i = -1; i <= 1; i++) {
                                double c = filter[j + 1][i + 1];
                                P[k] = getPix(copy, u + i, v + j) * (int) c;
                                k++;
                            }
                        }
                        // sort the pixel vector and take the middle element
                        Arrays.sort(P);
                        setPix(im, u - 1, v - 1, P[4]);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // create histogram
                respJSON = generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing weighted median-filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result toBinary() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in toBinary()...");
        } else {
            String id = json.findPath("id").toString();

            // threshold value for manually binary conversion
            int threshold = Integer.parseInt(json.findPath("threshold").toString());
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // check if 8-bit image
                if (im.getType() == 10) {
                    // convert to binary by the threshold-value
                    im = getBinaryImage(threshold, im);
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // create histogram
                respJSON = generateBinaryHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing to convert to binary...");
            }
            return ok(respJSON);
        }
    }

    public static Result minimum() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in minimum-filter...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // create histogram
                int w = im.getWidth();
                int h = im.getHeight();

                // border extension
                BufferedImage copy;
                copy = copyImage(im, "WHITE");

                // filter operation
                int[] P = new int[9];

                for (int v = 1; v <= h; v++) {
                    for (int u = 1; u <= w; u++) {

                        // fill the pixel vector P for filter position (u,v)
                        int k = 0;
                        for (int j = -1; j <= 1; j++) {
                            for (int i = -1; i <= 1; i++) {
                                P[k] = getPix(copy, u + i, v + j);
                                k++;
                            }
                        }
                        // sort the pixel vector and take 1 element
                        Arrays.sort(P);
                        setPix(im, u - 1, v - 1, P[0]);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // create histogram
                respJSON = generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing minimum filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result maximum() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in maximum-filter...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // create histogram
                int w = im.getWidth();
                int h = im.getHeight();

                // border extension
                BufferedImage copy;
                copy = copyImage(im, "BLACK");

                // filter operation
                int[] P = new int[9];

                for (int v = 1; v <= h; v++) {
                    for (int u = 1; u <= w; u++) {

                        // fill the pixel vector P for filter position (u,v)
                        int k = 0;
                        for (int j = -1; j <= 1; j++) {
                            for (int i = -1; i <= 1; i++) {
                                P[k] = getPix(copy, u + i, v + j);
                                k++;
                            }
                        }
                        // sort the pixel vector and take the last element
                        Arrays.sort(P);
                        setPix(im, u - 1, v - 1, P[8]);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // create histogram
                respJSON = generateHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing maximum filter...");
            }
            return ok(respJSON);
        }
    }

    public static Result region() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in region labeling...");
        } else {
            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // check if a 8-bit image
                if (im.getType() == 10) {
                    // convert to binary image withe the threshold-value 127
                    im = getBinaryImage(127, im);
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
                processing = copyBinaryImage(im, "CONTINUE");

                // create 8-bit copy from binary image, to processing region labeling
                BufferedImage copy;
                copy = new BufferedImage(processing.getWidth(), processing.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

                // copy binary to 8-bit
                for (int y = 0; y < processing.getHeight(); y++) {
                    for (int x = 0; x < processing.getWidth(); x++) {
                        setPix(copy, x, y, getPix(processing, x, y));
                    }
                }

                //PASS 1 - ASSIGN INITIAL LABELS
                label = 2;

                for (int y = 1; y <= h; y++) {
                    for (int x = 1; x <= w; x++) {

                        // new labelpixel reached
                        if (getPix(copy, x, y) == 1) {

                            // reset neighbours array and count of foreground-pixels
                            foregroundPix = 0;
                            neighbours[0] = 0;
                            neighbours[1] = 0;
                            neighbours[2] = 0;
                            neighbours[3] = 0;

                            // -------------------------- check Neighbours ---------------------------

                            // check top pixel
                            if (getPix(copy, x, y - 1) > 1) {
                                foregroundPix++;
                                neighbours[0] = getPix(copy, x, y - 1);
                            }
                            // check left pixel
                            if (getPix(copy, x - 1, y) > 1) {
                                foregroundPix++;
                                neighbours[1] = getPix(copy, x - 1, y);
                            }
                            // check topleft pixel
                            if (getPix(copy, x - 1, y - 1) > 1) {
                                foregroundPix++;
                                neighbours[2] = getPix(copy, x - 1, y - 1);
                            }
                            // check topright pixel
                            if (getPix(copy, x + 1, y - 1) > 1) {
                                foregroundPix++;
                                neighbours[3] = getPix(copy, x + 1, y - 1);
                            }

                            // all neighbours are background pixels
                            if (foregroundPix == 0) {
                                setPix(copy, x, y, label);
                                label++;
                                // exactly one of the neighbours has a label value, no conflict
                            } else if (foregroundPix == 1) {
                                for (int i = 0; i < 4; i++) {
                                    // select the first value which appears in array
                                    if (neighbours[i] != 0) {
                                        setPix(copy, x, y, neighbours[i]);
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
                                            setPix(copy, x, y, tmp);
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
                        if (setNumber[getPix(copy, x, y)] != 0) {
                            setPix(copy, x, y, setNumber[getPix(copy, x, y)]);
                        }
                    }
                }

                // copy to output image
                BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
                for (int y = 1; y <= h; y++) {
                    for (int x = 1; x <= w; x++) {
                        setPix(out, x - 1, y - 1, getPix(copy, x, y));
                    }
                }
                ImageIO.write(out, "PNG", new File(uploadPath));

                respJSON.put("success", "Success on processing region labeling...");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing region labeling...");
            }
            return ok();
        }
    }

    public static Result dilate() {
        // catch post-request as json object
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data on dilation filter...");
        } else {
            int[][] filter;

            // convert json to matrix
            filter = convertBinaryJsonToMatrix(json);

            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // check if 8-bit image
                if (im.getType() == 10) {
                    // convert to binary image by threshold-value 127
                    im = getBinaryImage(127, im);
                }

                int w = im.getWidth();
                int h = im.getHeight();
                int newValue;

                // border extension
                BufferedImage copy;
                copy = copyBinaryImage(im, "CONTINUE");

                // filter operation
                for (int i = 1; i <= h; i++) {
                    for (int j = 1; j <= w; j++) {
                        newValue = 0;

                        // maximum filter
                        for (int k = -1; k <= 1; k++) {
                            for (int l = -1; l <= 1; l++) {
                                int filterVal = filter[k + 1][l + 1];
                                if (filterVal == 1) {
                                    filterVal = -255;
                                }
                                int bufferVal = getPix(copy, j + l, i + k);
                                bufferVal += filterVal;

                                // check max
                                if (bufferVal > newValue) {
                                    newValue = bufferVal;
                                }
                            }
                        }
                        // check pixel bounds
                        newValue = checkPixel(newValue);
                        setPix(im, j - 1, i - 1, newValue);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // generate histogram as json
                respJSON = generateBinaryHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing dilation...");
            }
            return ok(respJSON);
        }
    }

    public static Result erode() {
        // catch post-request as json
        ObjectNode respJSON = Json.newObject();
        JsonNode json = request().body().asJson();

        if (json == null) {
            return badRequest("Expecting Json data in erosion filter...");
        } else {
            int[][] filter;

            // convert json to matrix
            filter = convertBinaryJsonToMatrix(json);

            String id = json.findPath("id").toString();
            String uploadPath = PATH + "/" + id + ".png";

            try {
                BufferedImage im = ImageIO.read(new File(uploadPath));

                // check if 8-bit image
                if (im.getType() == 10) {
                    // convert to binary image by threshold-value 127
                    im = getBinaryImage(127, im);
                }

                int w = im.getWidth();
                int h = im.getHeight();
                int newValue;

                // border extension
                BufferedImage copy;
                copy = copyBinaryImage(im, "CONTINUE");

                // filter operation
                for (int i = 1; i <= h; i++) {
                    for (int j = 1; j <= w; j++) {
                        newValue = 255;

                        // minimum filter
                        for (int k = -1; k <= 1; k++) {
                            for (int l = -1; l <= 1; l++) {
                                int filterVal = filter[k + 1][l + 1];
                                if (filterVal == 1) {
                                    filterVal = -255;
                                }
                                int bufferVal = getPix(copy, j + l, i + k);
                                bufferVal -= filterVal;

                                // check min
                                if (bufferVal < newValue) {
                                    newValue = bufferVal;
                                }
                            }
                        }
                        // check pixel bounds
                        newValue = checkPixel(newValue);
                        setPix(im, j - 1, i - 1, newValue);
                    }
                }
                ImageIO.write(im, "PNG", new File(uploadPath));

                // generate histogram as JSON
                respJSON = generateBinaryHisto(id + ".png");
            } catch (IOException ioe) {
                respJSON.put("error", "Error on processing erosion...");
            }
            return ok(respJSON);
        }

    }

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
    private static double[][] convertJsonToMatrix(JsonNode json) {
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
    private static int[][] convertBinaryJsonToMatrix(JsonNode json) {
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
    private static int checkPixel(int pixel) {
        if (pixel > 255)
            pixel = 255;
        if (pixel < 0)
            pixel = 0;
        return pixel;
    }

    // conversions for border extensions
    private static int modeStringToInt(String mode) {
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

    private static void setPix(BufferedImage im, int x, int y, int value) {
        im.getRaster().setSample(x, y, 0, value);
    }

    private static int getPix(BufferedImage im, int x, int y) {
        return im.getRaster().getPixel(x, y, (int[]) null)[0];
    }
}