package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html._include.*;
import views.html.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.File;
import play.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//for buffered image
import java.awt.Graphics2D; 
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.Arrays;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Image extends Controller {

	public static Result upload(String id) {		
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile("picture");
		String path = Play.application().path().getAbsolutePath() + "/public/uploads";
		File theDir = new File(path);
				
		// erstelle Ordner uploads wenn nicht existiert
		if (!theDir.exists()) {
			boolean result = theDir.mkdir();  
		}

	  
		if (picture != null) {
			String fileName = picture.getFilename();
			String contentType = picture.getContentType(); 
			File file = picture.getFile();

			String myUploadPath = path + "/" + id;
			file.renameTo(new File(myUploadPath));			
			
			try {
				// Bild einlesen
				BufferedImage input = ImageIO.read(new File(myUploadPath));
				
				// in 8bit Bild konvertieren 
				BufferedImage im = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY); 
				Graphics2D g2d = im.createGraphics();
				
				// Bild rendern
				g2d.drawImage(input,0,0,null);
				
				// das resultierende Bild Speichern
				ImageIO.write(im,"PNG",new File(myUploadPath));
				
			} catch(IOException ioe) {
			}			
			return ok(views.html.image.render(id));
		} else {
			flash("error", "Missing file");
			return redirect(routes.Application.index());    
		}
	}
	
	public static Result showHist(String id) {
		ObjectNode respJSON = Json.newObject();
		
		respJSON = generateHisto(id);
						
		return ok(respJSON);	
	}
	
	public static Result showBinaryHist(String id) {
		ObjectNode respJSON = Json.newObject();
		
		respJSON = generateBinaryHisto(id);
						
		return ok(respJSON);	
	}
	
	public static Result smoothing() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();
		
		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			double[][] filter;
			String id = json.findPath("id").toString();
			filter = convertJsonToMatrix(json);
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				// Histogramm erstellen
				int w = im.getWidth();
				int h = im.getHeight();

				BufferedImage copy;
				copy = copyImage(im, "BLACK");
			
				// Filteroperation
				for (int v = 1; v <= h; v++) {
					for (int u = 1; u <= w; u++) {
						double sum = 0;
						for (int j = -1; j <= 1; j++) {
							for (int i = -1; i <= 1; i++) {
								//int p = copy.getPixel(u+i,v+j);
								int p = copy.getRaster().getPixel(u+i, v+j, (int[]) null)[0];
								// get the corresponding filter coefficient:
								double c = filter[j+1][i+1];
								sum = sum + c * p;
							}
						}
						
						int q = (int) Math.round(sum);
						q = checkPixel(q);
						im.getRaster().setSample(u-1,v-1,0,q);
						//orig.putPixel(u,v,q);
					}
				}


				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".png");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing smoothing filter...");
			}
			return ok(respJSON);			
		}
	}

	public static Result median(){
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();

		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				// Histogramm erstellen
				int w = im.getWidth();
				int h = im.getHeight();

				//-----------------to here
				BufferedImage copy;
				copy = copyImage(im, "CONTINUE");
				// Filteroperation
				int[] P = new int[9];

				for (int v=1; v<=h; v++) {
					for (int u=1; u<=w; u++) {
			 
						//fill the pixel vector P for filter position (u,v)
						int k = 0;
						for (int j=-1; j<=1; j++) {
							for (int i=-1; i<=1; i++) {
								P[k] = copy.getRaster().getPixel(u+i, v+j, (int[]) null)[0];
								k++;
							}
						}
						//sort the pixel vector and take 1 element
						Arrays.sort(P);
						im.getRaster().setSample(u-1,v-1,0,P[4]);
					}
				}


				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".png");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing median filter...");
			}
			return ok(respJSON);			
		}
	}
	
	public static Result weightedMedian(){
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();
		double[][] filter;
		filter = convertJsonToMatrix(json);		

		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				// Histogramm erstellen
				int w = im.getWidth();
				int h = im.getHeight();

				//-----------------to here
				BufferedImage copy;
				copy = copyImage(im, "CONTINUE");
				// Filteroperation
				int[] P = new int[9];

				for (int v=1; v<=h; v++) {
					for (int u=1; u<=w; u++) {
			 
						//fill the pixel vector P for filter position (u,v)
						int k = 0;
						for (int j=-1; j<=1; j++) {
							for (int i=-1; i<=1; i++) {
								double c = filter[j+1][i+1];
								P[k] = copy.getRaster().getPixel(u+i, v+j, (int[]) null)[0] * (int)c;
								k++;
							}
						}
						//sort the pixel vector and take 1 element
						Arrays.sort(P);
						im.getRaster().setSample(u-1,v-1,0,P[4]);
					}
				}


				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".png");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing median filter...");
			}
			return ok(respJSON);			
		}
	}
	
	public static Result toBinary() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			String id = json.findPath("id").toString();
			int threshold = Integer.parseInt(json.findPath("threshold").toString());
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				// Histogramm erstellen
				int w = im.getWidth();
				int h = im.getHeight();
				
				im = getBinaryImage(threshold, im);
				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateBinaryHisto(id + ".png");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing convert to binary...");
			}
			return ok(respJSON);			
		}
	}
	
	public static Result minimum() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();

		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				// Histogramm erstellen
				int w = im.getWidth();
				int h = im.getHeight();

				//-----------------to here
				BufferedImage copy;
				copy = copyImage(im, "WHITE");
				
				// Filteroperation
				int[] P = new int[9];

				for (int v=1; v<=h; v++) {
					for (int u=1; u<=w; u++) {
			 
						//fill the pixel vector P for filter position (u,v)
						int k = 0;
						for (int j=-1; j<=1; j++) {
							for (int i=-1; i<=1; i++) {
								P[k] = copy.getRaster().getPixel(u+i, v+j, (int[]) null)[0];
								k++;
							}
						}
						//sort the pixel vector and take 1 element
						Arrays.sort(P);
						im.getRaster().setSample(u-1,v-1,0,P[0]);
					}
				}


				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".png");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing minimum filter...");
			}
			return ok(respJSON);			
		}
	}

	public static Result maximum() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();

		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				// Histogramm erstellen
				int w = im.getWidth();
				int h = im.getHeight();

				BufferedImage copy;
				copy = copyImage(im, "BLACK");
				// Filteroperation
				int[] P = new int[9];

				for (int v=1; v<=h; v++) {
					for (int u=1; u<=w; u++) {
			 
						//fill the pixel vector P for filter position (u,v)
						int k = 0;
						for (int j=-1; j<=1; j++) {
							for (int i=-1; i<=1; i++) {
								P[k] = copy.getRaster().getPixel(u+i, v+j, (int[]) null)[0];
								k++;
							}
						}
						//sort the pixel vector and take 1 element
						Arrays.sort(P);
						im.getRaster().setSample(u-1,v-1,0,P[8]);
					}
				}


				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateHisto(id + ".png");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing maximum filter...");
			}
			return ok(respJSON);			
		}
	}
	
	public static Result region() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();
		
		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
				
				if (im.getType() == 10) {
					// Konvertierung in ein Binärbild
					im = getBinaryImage(127, im);
				}
				
				Map<Integer,Integer> collisionMap = new HashMap<Integer, Integer>();
				int w = im.getWidth();
				int h = im.getHeight();
				int[] neighbours = new int[4];
				int label;
				int foregroundPix;
				
				BufferedImage copy;
				copy = copyImage(im, "CONTINUE");
				
				//PASS 1 - ASSIGN INITIAL LABELS
				label = 2;
				foregroundPix = 0;
				for(int y = 1; y <= h; y++) {
					for(int x = 1; x <= w; x++) {
						// new labelpixel reached
						if(copy.getRaster().getPixel(x, y, (int[]) null)[0] == 1) {
							// *************** check Neighbours ***********************
							// check top pixel
							if(copy.getRaster().getPixel(x, y-1, (int[]) null)[0] > 1) {
								foregroundPix++;
								neighbours[0] = copy.getRaster().getPixel(x, y-1, (int[]) null)[0];
							}
							// check left pixel
							if(copy.getRaster().getPixel(x-1, y, (int[]) null)[0] > 1) {
								foregroundPix++;
								neighbours[1] = copy.getRaster().getPixel(x-1, y, (int[]) null)[0];
							}
							// check topleft pixel
							if(copy.getRaster().getPixel(x-1, y-1, (int[]) null)[0] > 1) {
								foregroundPix++;
								neighbours[2] = copy.getRaster().getPixel(x-1, y-1, (int[]) null)[0];
							}
							// check topright pixel
							if(copy.getRaster().getPixel(x+1, y-1, (int[]) null)[0] > 1) {
								foregroundPix++;
								neighbours[3] = copy.getRaster().getPixel(x+1, y-1, (int[]) null)[0];
							}
                
							// all neighbours are background pixels
							if(foregroundPix == 0) {
								copy.getRaster().setSample(x,y,0,label);
								label++;
								// exactly one of the neighbours has a label value
							} else if(foregroundPix == 1) {
								for(int i = 0; i < 4; i++) {
									// select the first value which appears in map
									if(neighbours[i] != 0) {
										copy.getRaster().setSample(x,y,0,neighbours[i]);
										break;
									}
								}
								// serveral neighbours have label values
							} else if(foregroundPix > 1) {
								boolean firstEntry = true;
								int tmp = 0;
								for(int i = 0; i < 4; i++) {                         
									if(neighbours[i] != 0) {
										// select the first appaering label value
										if(firstEntry == true) {
											tmp = neighbours[i];
											copy.getRaster().setSample(x,y,0,tmp);
											firstEntry = false;
											// all other neighbours register in collisionMap 
										} else if(tmp != neighbours[i]) {
											// PASS 2 - RESOLVE LABEL COLLISIONS
											int key = -1;
											if(tmp > neighbours[i]) {
												collisionMap.put(new Integer(tmp), new Integer(neighbours[i]));
												// use of transitivity characteristic
												for (Map.Entry<Integer, Integer> entry : collisionMap.entrySet()) {
													if (entry.getValue() == tmp) {
														collisionMap.put(new Integer(entry.getKey()), new Integer(neighbours[i]));
													}
												}
											} else {
												collisionMap.put(new Integer(neighbours[i]),new Integer(tmp));
												// use of transitivity characteristic
												for (Map.Entry<Integer, Integer> entry : collisionMap.entrySet()) {
													if (entry.getValue() == neighbours[i]) {
														collisionMap.put(new Integer(entry.getKey()), new Integer(tmp));
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}  
			    // PASS 3 - RELABEL THE IMAGE
			    // copy to output image
			    for(int y = 1; y <= h; y++) {
			        for(int x = 1; x <= w; x++) {
						// Kollisionen lösen
			            if(collisionMap.get(copy.getRaster().getPixel(x, y, (int[]) null)[0]) != null) {
			            	//element found;
							copy.getRaster().setSample(x, y, 0, collisionMap.get(copy.getRaster().getPixel(x, y, (int[]) null)[0]));
			            }
			        }
			    }
				
			    for(int y = 1; y <= h; y++) {
			        for(int x = 1; x <= w; x++) {
						im.getRaster().setSample(x-1, y-1, 0, copy.getRaster().getPixel(x, y, (int[]) null)[0]);
					}
				}
				
				ImageIO.write(copy,"PNG",new File("public/uploads/test.png")); 
				
				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing region labeling...");
			}
			return ok(respJSON);
		}
		
	}
	
	public static Result dilate() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();
		
		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			int[][] filter;
			filter = convertBinaryJsonToMatrix(json);
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
					
				// Falls noch kein Binärbild
				if (im.getType() == 10) {
					// Konvertierung in ein Binärbild
					im = getBinaryImage(127, im);
				} 
					
				int w = im.getWidth();
				int h = im.getHeight();
				int newValue;					
				
				BufferedImage copy;
				copy = copyBinaryImage(im, "CONTINUE");
					
					
				for(int i = 1; i <= h; i++) {
					for(int j = 1; j <= w; j++) {
						newValue = 0;
						
						// Maximumfilter                    
						for(int k = -1; k <= 1; k++) {
							for(int l = -1; l <= 1; l++) {
								int filterVal = filter[k+1][l+1];
								if(filterVal == 1) {
									filterVal = -255;
								}
								int bufferVal = copy.getRaster().getPixel(j+l, i+k, (int[]) null)[0]; 
								bufferVal += filterVal;
								
								// check max
								if (bufferVal > newValue) {
									newValue = bufferVal;
								}
							}
						}
						newValue = checkPixel(newValue);
						im.getRaster().setSample(j-1, i-1, 0, newValue);
					}
				}
					
				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateBinaryHisto(id + ".png");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing region labeling...");
			}
			return ok(respJSON);
		}
		
	}
	
	public static Result erode() {
		ObjectNode respJSON = Json.newObject();
		JsonNode json = request().body().asJson();
		
		if(json == null) {
			return badRequest("Expecting Json data");
		} else {
			int[][] filter;
			filter = convertBinaryJsonToMatrix(json);
			String id = json.findPath("id").toString();
			String uploadPath = Play.application().path().getAbsolutePath() + "/public/uploads/" + id + ".png";
			try {
				BufferedImage im = ImageIO.read(new File(uploadPath));
					
				// Falls noch kein Binärbild
				if (im.getType() == 10) {
					// Konvertierung in ein Binärbild
					im = getBinaryImage(127, im);
				} 
					
				int w = im.getWidth();
				int h = im.getHeight();
				int newValue;					
				
				BufferedImage copy;
				copy = copyBinaryImage(im, "CONTINUE");
					
					
				for(int i = 1; i <= h; i++) {
					for(int j = 1; j <= w; j++) {
						newValue = 255;
						
						// Minimumfilter                    
						for(int k = -1; k <= 1; k++) {
							for(int l = -1; l <= 1; l++) {
								int filterVal = filter[k+1][l+1];
								if(filterVal == 1) {
									filterVal = -255;
								}
								int bufferVal = copy.getRaster().getPixel(j+l, i+k, (int[]) null)[0]; 
								bufferVal -= filterVal;
								
								// check min
								if (bufferVal < newValue) {
									newValue = bufferVal;
								}
							}
						}
						newValue = checkPixel(newValue);
						im.getRaster().setSample(j-1, i-1, 0, newValue);
					}
				}
					
				ImageIO.write(im,"PNG",new File(uploadPath)); 
				
				// Histogramm erstellen
				respJSON = generateBinaryHisto(id + ".png");
			} catch(IOException ioe) {
				respJSON.put("error", "Error on processing region labeling...");
			}
			return ok(respJSON);
		}
		
	}

	// generiert ein Histogramm
	public static ObjectNode generateHisto(String id) {
		int[] H = new int[256];
		String tmp;
		Integer tmpInt;
		ObjectNode respJSON = Json.newObject();
		
		try {
			// Bild einlesen
			BufferedImage im = ImageIO.read(new File(Play.application().path().getAbsolutePath() + "/public/uploads/" + id));
		
			// Histogramm erstellen
			int w = im.getWidth();
			int h = im.getHeight();			
		
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					// Pixel abspeichern 
					int i = im.getRaster().getPixel(u, v, (int[]) null)[0];
					H[i] = H[i] + 1;						
				}
			}
			
			// Histogramm in JSON Object verpacken
			for (int i = 0; i < H.length; i++) {	
				tmpInt = new Integer(i);
				tmp = tmpInt.toString();			
				respJSON.put(tmp, new Integer(H[i]));
			}
		} catch(IOException ioe) {
			respJSON.put("error", "Error on creating histogram...");
		}
		return respJSON;
	}
	
	// generiert ein Histogramm für binäre Bilder
	public static ObjectNode generateBinaryHisto(String id) {
		int[] H = new int[2];
		String tmp;
		Integer tmpInt;
		ObjectNode respJSON = Json.newObject();
		
		try {
			// Bild einlesen
			BufferedImage im = ImageIO.read(new File(Play.application().path().getAbsolutePath() + "/public/uploads/" + id));
		
			// Histogramm erstellen
			int w = im.getWidth();
			int h = im.getHeight();			
		
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					// Pixel abspeichern 
					int i = im.getRaster().getPixel(u, v, (int[]) null)[0];
					H[i] = H[i] + 1;						
				}
			}
			
			// Histogramm in JSON Object verpacken
			for (int i = 0; i < H.length; i++) {	
				tmpInt = new Integer(i);
				tmp = tmpInt.toString();			
				respJSON.put(tmp, new Integer(H[i]));
			}
		} catch(IOException ioe) {
			respJSON.put("error", "Error on creating histogram...");
		}
		return respJSON;
	}  
	
	// Bestimmt aufgrund des ausgewählten Schwellwertes ein Binärbild
	public static BufferedImage getBinaryImage(int threshold, BufferedImage greyImage) {
		int w = greyImage.getWidth();
		int h = greyImage.getHeight();
		BufferedImage binaryImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY); 
		
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				if(greyImage.getRaster().getPixel(u,v,(int[]) null)[0] >= threshold) {
					binaryImage.getRaster().setSample(u,v,0,1);
				} else {
					binaryImage.getRaster().setSample(u,v,0,0);
				}
			}
		}
		return binaryImage;		
	}
	
	// speichert das Bild in einen vergrößertes Bild für die Randbehandlung
	public static BufferedImage copyImage(BufferedImage src, String mode) {

		int w = src.getWidth();
		int h = src.getHeight();
		
		// Kopie des Bildes sowie randbehandlung des bildes
		BufferedImage copy = new BufferedImage(w+2, h+2, BufferedImage.TYPE_BYTE_GRAY);
		
		switch(mode) {
			// Kopiebild auf komplett auf Weiß setzen
			case "WHITE":
			for (int v = 0; v < h+2; v++) {
				for (int u = 0; u < w+2; u++) {
					copy.getRaster().setSample(u, v, 0, 255);
				}
			} 
			// Bild in das vergrößerte Bild kopieren
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					copy.getRaster().setSample(u+1, v+1, 0, src.getRaster().getPixel(u, v, (int[]) null)[0]);
				}
			} 
			break;
			// Kopiebild auf komplett auf Schwarz setzen
			case "BLACK":
			for (int v = 0; v < h+2; v++) {
				for (int u = 0; u < w+2; u++) {
					copy.getRaster().setSample(u, v, 0, 0);
				}
			}
			// Bild in das vergrößerte Bild kopieren
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					copy.getRaster().setSample(u+1, v+1, 0, src.getRaster().getPixel(u, v, (int[]) null)[0]);
				}
			} 
			break;
			// Randpixel auf an die Ränder erweitern
			case "CONTINUE":
			// Kopieren vom Bild in das vergrößerte Bild
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					copy.getRaster().setSample(u+1,v+1, 0, src.getRaster().getPixel(u,v,(int[]) null)[0]);
				}
			}
			// Oberer Rand
			for(int u = 0; u < w+2; u++) {
				copy.getRaster().setSample(u,0,0,copy.getRaster().getPixel(u,1,(int[]) null)[0]);
			}
	
			// Unterer Rand
			for(int u = 0; u < w+2; u++) {
				copy.getRaster().setSample(u,h+1,0,copy.getRaster().getPixel(u,h,(int[]) null)[0]);
			}

			// Linker Rand
			for(int v = 0; v < h+2; v++) {
				copy.getRaster().setSample(0,v,0,copy.getRaster().getPixel(1,v,(int[]) null)[0]);
			}
			
			// Rechter Rand
			for(int v = 0; v < h+2; v++) {
				copy.getRaster().setSample(w+1,v,0,copy.getRaster().getPixel(w,v,(int[]) null)[0]);
			}
		
			// Obere linke Ecke
			copy.getRaster().setSample(0,0,0,copy.getRaster().getPixel(1,1,(int[]) null)[0]);
			// Untere linke Ecke	
			copy.getRaster().setSample(0,h+1,0,copy.getRaster().getPixel(1,h,(int[]) null)[0]);
			// Obere rechte Ecke
			copy.getRaster().setSample(w+1,0,0,copy.getRaster().getPixel(w,1,(int[]) null)[0]);
			// Untere rechte Ecke
			copy.getRaster().setSample(w+1,h+1,0,copy.getRaster().getPixel(w,h,(int[]) null)[0]);
			break;
			default:
		}

		return copy;		
	}
	
	// speichert das Bild in einen vergrößertes Bild für die Randbehandlung
	public static BufferedImage copyBinaryImage(BufferedImage src, String mode) {

		int w = src.getWidth();
		int h = src.getHeight();
		
		// Kopie des Bildes sowie randbehandlung des bildes
		BufferedImage copy = new BufferedImage(w+2, h+2, BufferedImage.TYPE_BYTE_BINARY);
		
		switch(mode) {
			// Kopiebild auf komplett auf Weiß setzen
			case "WHITE":
			for (int v = 0; v < h+2; v++) {
				for (int u = 0; u < w+2; u++) {
					copy.getRaster().setSample(u, v, 0, 1);
				}
			} 
			// Bild in das vergrößerte Bild kopieren
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					copy.getRaster().setSample(u+1, v+1, 0, src.getRaster().getPixel(u, v, (int[]) null)[0]);
				}
			} 
			break;
			// Kopiebild auf komplett auf Schwarz setzen
			case "BLACK":
			for (int v = 0; v < h+2; v++) {
				for (int u = 0; u < w+2; u++) {
					copy.getRaster().setSample(u, v, 0, 0);
				}
			}
			// Bild in das vergrößerte Bild kopieren
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					copy.getRaster().setSample(u+1, v+1, 0, src.getRaster().getPixel(u, v, (int[]) null)[0]);
				}
			} 
			break;
			// Randpixel auf an die Ränder erweitern
			case "CONTINUE":
			// Kopieren vom Bild in das vergrößerte Bild
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					copy.getRaster().setSample(u+1,v+1, 0, src.getRaster().getPixel(u,v,(int[]) null)[0]);
				}
			}
			// Oberer Rand
			for(int u = 0; u < w+2; u++) {
				copy.getRaster().setSample(u,0,0,copy.getRaster().getPixel(u,1,(int[]) null)[0]);
			}
	
			// Unterer Rand
			for(int u = 0; u < w+2; u++) {
				copy.getRaster().setSample(u,h+1,0,copy.getRaster().getPixel(u,h,(int[]) null)[0]);
			}

			// Linker Rand
			for(int v = 0; v < h+2; v++) {
				copy.getRaster().setSample(0,v,0,copy.getRaster().getPixel(1,v,(int[]) null)[0]);
			}
			
			// Rechter Rand
			for(int v = 0; v < h+2; v++) {
				copy.getRaster().setSample(w+1,v,0,copy.getRaster().getPixel(w,v,(int[]) null)[0]);
			}
		
			// Obere linke Ecke
			copy.getRaster().setSample(0,0,0,copy.getRaster().getPixel(1,1,(int[]) null)[0]);
			// Untere linke Ecke	
			copy.getRaster().setSample(0,h+1,0,copy.getRaster().getPixel(1,h,(int[]) null)[0]);
			// Obere rechte Ecke
			copy.getRaster().setSample(w+1,0,0,copy.getRaster().getPixel(w,1,(int[]) null)[0]);
			// Untere rechte Ecke
			copy.getRaster().setSample(w+1,h+1,0,copy.getRaster().getPixel(w,h,(int[]) null)[0]);
			break;
			default:
		}

		return copy;		
	}
	
	// Wandelt ein JSON in eine Matrix
	private static double[][] convertJsonToMatrix(JsonNode json) {
		double[][] filter = new double[3][3];

		for (int r = 1; r <= 3; r++) {
			for (int c = 1; c <= 3; c++) {
				String str = "r" + r + "c" + c;
				double tmp = Double.parseDouble(json.findPath(str).toString());
				filter[r-1][c-1] = tmp;
			}
		}

		return filter;
	}
	
	// Wandelt ein JSON in eine Matrix
	private static int[][] convertBinaryJsonToMatrix(JsonNode json) {
		int[][] filter = new int[3][3];

		for (int r = 1; r <= 3; r++) {
			for (int c = 1; c <= 3; c++) {
				String str = "r" + r + "c" + c;
				int tmp = Integer.parseInt(json.findPath(str).toString());
				filter[r-1][c-1] = tmp;
			}
		}

		return filter;
	}
	
	// Pixelgrenzen beachten 
	private static int checkPixel(int pixel) {
		if (pixel > 255)
			pixel = 255;
		if (pixel < 0)
			pixel = 0; 
		return pixel;
	}
}
