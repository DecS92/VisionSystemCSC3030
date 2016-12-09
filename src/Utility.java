import java.awt.image.BufferedImage;
import java.awt.image.Raster;


public class Utility {
	int smallMax, mediumMax;
	int[] areas, perimeters;
	ImageData[] imgData;
	public Utility()
	{
		smallMax =0;
		mediumMax =0;
	}

	public static short[] thresholdLut(int t, int alphaSign) {
		short[] lut = new short[256];
		for (int i = 0; i < 256; i++) {
			if (alphaSign > 0) {
				if (i >= t) {
					lut[i] = 255;
				} else {
					lut[i] = 0;
				}
			}
			else{
				if (i < t) {
						lut[i] = 255;
					} else {
						lut[i] = 0;
					}
				
			}
		}
		return lut;
	}

	public static short[] brightnessLut(int c){
		short[] lut = new short[256];
		for(int i=0; i<256; i++){
			 if(i < -c){
				 lut[i] = 0;
			 }
			 else if(i > 255 - c){
				 lut[i] = 255;
			 }
			 else{
				 lut[i] = (short)(i + c); 
			 }
		}
		return lut;
	}


	public static short[] linearStretchLut(float m, float c) {
		short[] lut = new short[256];
		for (int i = 0; i < 256; i++) {
			if (i < (-c / m)) {
				lut[i] = 0;
			} else if (i > (255 - c) / m) {
				lut[i] = 255;
			} else {
				lut[i] = (short) (m * i + c);
			}
		}
		return lut;
	}
	
	public short[] powerLawLut(float gamma){
		short[] lut = new short[256];
		for (int i = 0; i < 256; i++) {
		 lut[i] = (short) (Math.pow(i, gamma) / Math.pow(255, gamma-1));
		}
		return lut;
		}

	public static short [] histogramEqualisationLut(Histogram hist){
		short[] lut = new short[256];
		for (int i = 0; i < 256; i++) {
			try{
			lut[i] = (short)Math.max(0, 256 * hist.getCumulativeFrequency(i) / hist.getNumSamples() - 1);
			} catch (Exception e){
				System.out.println("Probably Histogram exception");
				e.printStackTrace();				
			}
		}
		return lut;
	}

	public static BufferedImage increaseBrightness(BufferedImage source, int brightnessUp){
		short[] lut = brightnessLut(brightnessUp);
		BufferedImage brighterImage = ImageOp.pixelop(source, lut); 
		return brighterImage;
		}

	public static BufferedImage contrastLinearStretch(BufferedImage source, float m, float c){
		short[] lut = linearStretchLut(m, c);
		BufferedImage linearStretchedImage = ImageOp.pixelop(source, lut); 
		return linearStretchedImage;
		}
	
	public static BufferedImage autoContrastLinearStretch(BufferedImage source)throws HistogramException{
		Histogram hist = new Histogram(source);
		int min = hist.getMinValue();
		int max = hist.getMaxValue();
		float m = 255f / (max - min);
		float c = -1* m * min;
		short[] lut = linearStretchLut(m, c);
		BufferedImage linearStretchedImage = ImageOp.pixelop(source, lut); 
		return linearStretchedImage;
		}

	public BufferedImage contrastPowerLaw(BufferedImage source, float gamma){
		short[] lut = powerLawLut(gamma);
		BufferedImage powerLawedImage = ImageOp.pixelop(source, lut); 
		return powerLawedImage;
		}
	
	public static BufferedImage contrastHistogramEqualisation(BufferedImage source) throws HistogramException{
		Histogram hist = new Histogram(source);
		hist.getMinValue();
		short[] lut = histogramEqualisationLut(hist);
		BufferedImage histoEqualisedImage = ImageOp.pixelop(source, lut); 
		return histoEqualisedImage;
		}
	
	public static BufferedImage lowPassNoiseReduction(BufferedImage source){
		final float[] LOWPASS3X3 = {
				1/9.f,1/9.f,1/9.f,
				1/9.f,1/9.f,1/9.f,
				1/9.f,1/9.f,1/9.f
				};
		BufferedImage reducedNoiseImage = ImageOp.convolver(source, LOWPASS3X3);
		return reducedNoiseImage;		
	}
	
	public static BufferedImage medianNoiseReduction(BufferedImage source, int maskSize){
		return ImageOp.median(source, maskSize);
	}
	
	public static BufferedImage performEdgeExtraction(BufferedImage source){
		final float[] HIGHPASS1X2 = {-10.f,10.f, 0.f,0.f}; 
		final float[] HIGHPASS2X1 = {-10.f,0.f,	10.f,0.f};
		BufferedImage a = ImageOp.convolver(source, HIGHPASS1X2);
		BufferedImage b = ImageOp.convolver(source, HIGHPASS2X1);
		return ImageOp.imagrad(a, b);
	}
	
	public static BufferedImage thresholdAnImage(BufferedImage source, int t, int alphaSign){
		short[] lut = thresholdLut(t, alphaSign);
		BufferedImage thresholdedImage = ImageOp.pixelop(source, lut); 
		return thresholdedImage;
		}
	
	public static int greyMean(BufferedImage source){
		int width = source.getWidth();
		int height = source.getHeight();
		int numPix = width * height; 
		int total = 0;
		Raster rast = source.getRaster();
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				int g = rast.getSample(j,i,0);
				total +=g;			
			}
		}		
		return total/numPix;
		}
	
	
	public static int alphaMean(BufferedImage source, int t1, int t2){
		int width = source.getWidth();
		int height = source.getHeight();
		int count = 0; 
		int total = 0;
		Raster rast = source.getRaster();
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				int g = rast.getSample(j,i,0);
				if(g<t1 || g>t2){
					continue;
				}
				else{
					total +=g;
					count ++;
				}
			}
		}		
		return total/count;
		}

	public static double standardDeviation(BufferedImage source){
		int mean = greyMean(source);
		int width = source.getWidth();
		int height = source.getHeight();
		int numPix = width * height;
		double deviatonTotal = 0;
		Raster rast = source.getRaster();
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				int g = rast.getSample(j,i,0);
				deviatonTotal += Math.pow(g-mean, 2);
			}
		}
		double res = Math.sqrt(deviatonTotal / (numPix-1));
		return res;
		}

	public static BufferedImage performAutomaticThresholding(BufferedImage source, float alpha){
		int alphaSign = getAlphaSign(source);
		System.out.println(alphaSign);
		System.out.println("mean was " + greyMean(source));
		int threshold = (int)(greyMean(source) +   alphaSign * alpha * standardDeviation(source));
		System.out.println("sd = " + standardDeviation(source));
		System.out.println("threshold = " + threshold);
		return thresholdAnImage(source, threshold, alphaSign);
	} 
	
	public static int getAlphaSign(BufferedImage source){
		int t1 = Math.max(0,(int)(greyMean(source) + -1 * standardDeviation(source)));
		int t2 = Math.min(255,(int)(greyMean(source) + 1 * standardDeviation(source)));
		int mean = greyMean(source);
		int bgGoneMean = alphaMean(source, t1, t2);	
		System.out.println("alpha mean was" + bgGoneMean);
		if(bgGoneMean>mean){
			return -1;
		}		
		else{
			return 1;
		}
	}
	
	public static BufferedImage preProcessAnImage(BufferedImage source, String reduceNoise, String linStretchPreferred){
		BufferedImage ppImage = source;
		//median noise reduction if requested
		if(reduceNoise=="Median"){
			ppImage = medianNoiseReduction(source,3);
		} else if(reduceNoise=="Mean"){
			ppImage = lowPassNoiseReduction(source);
		}
		//contrast - linear stretch if preferred, else use histogram equalisation
		try{
			if(linStretchPreferred=="Linear"){
				ppImage = autoContrastLinearStretch(ppImage);
			} else{
				ppImage = contrastHistogramEqualisation(ppImage);
			}		
		}
		catch (Exception e){
			e.printStackTrace();			
		}
		return ppImage;
	}

	public static BufferedImage postProcessAnImage(BufferedImage source, boolean closeFirst, int maskSize){
		if (closeFirst) {
			source = ImageOp.close(source, maskSize);
			source = ImageOp.open(source, maskSize);
		} else {
			source = ImageOp.open(source, maskSize);
			source = ImageOp.close(source, maskSize);
		}
		return source;
	}
	
	public static int getWhitePixArea(BufferedImage source){
		int width = source.getWidth();
		int height = source.getHeight();
		int total = 0;
		Raster rast = source.getRaster();
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				int g = rast.getSample(j,i,0);
				total +=g;			
			}
		}		
		return total/255;
	}
	
	public static int simplePerimeter(BufferedImage source){
		int area = getWhitePixArea(source);
		BufferedImage closed = ImageOp.close(source, 3);
		int closedArea = getWhitePixArea(closed);
		return area - closedArea;
	}
	
	public static double compactness(int area, int perimeter){
		double pSquared = Math.pow(perimeter, 2.0);
		double res =  pSquared/area;
		return res;	
	}
}

