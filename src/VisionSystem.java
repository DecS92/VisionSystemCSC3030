import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.print.attribute.standard.JobName;
import javax.swing.*;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;

public class VisionSystem {
	
	String[] four_training_order = { "car", "cow", "pear", "tomato" };
	//String[] all_training_order = {"apple", "car", "cow", "cup", "dog", "horse", "pear", "tomato"};
	int NUM_CLASSES = four_training_order.length;

	ArrayList<BufferedImage> trainingImages = new ArrayList<BufferedImage>();
	ArrayList<Image> originalHistograms = new ArrayList<Image>();
	ArrayList<BufferedImage> preprocessedImages = new ArrayList<BufferedImage>();
	ArrayList<Image> preprocessedHistograms = new ArrayList<Image>();
	ArrayList<BufferedImage> thresholdedImages = new ArrayList<BufferedImage>();
	ArrayList<BufferedImage> postprocessedImages = new ArrayList<BufferedImage>();
	
			

	
	JLabel noiseLabel;
	JLabel contrastLabel;
	JLabel alphaLabel;
	JLabel postLabel;
	JLabel postMaskLabel;
	
	JLabel [] preprocessedImgLabels; 
	JLabel [] ppHistoLabels;
	JLabel [] thresholdedImgLabels;
	JLabel [] postprocessedImgLabels;

	JFrame frame = new JFrame();
	
	JComboBox noiseReductionChoice;
	JComboBox contrastEnhanceChoice;
	JTextField alphaValue;
	JComboBox postProcChoice;
	JTextField postProcMaskField;

	JButton reprocess;
	
	JPanel mainPanel;
	JPanel ImagePanel;
	JPanel controlPanel;
	
	boolean reprocessBool = false;

	// constructor
	public VisionSystem() {
		
		// build/assign UI stuff
		noiseLabel =  new JLabel("Select noise reduction");
		contrastLabel =  new JLabel("Select contrast enhancement type");
		alphaLabel =  new JLabel("Enter alpha value for auto thresholding");
		postLabel =  new JLabel("Select post processing type");
		postMaskLabel =  new JLabel("Select post processing mask size");
		
		String [] noiseChoices = {"Mean", "Median" ,"None"};
		noiseReductionChoice = new JComboBox(noiseChoices);
		noiseReductionChoice.setSelectedIndex(0);
		
		String [] contrastChoices = {"Linear", "Histogram Equalisation"};
		contrastEnhanceChoice = new JComboBox(contrastChoices);
		contrastEnhanceChoice.setSelectedIndex(0);

		alphaValue = new JTextField("1.0");
		
		String [] postProcChoices = {"Open -> Close", "Close -> Open"};
		postProcChoice = new JComboBox(postProcChoices);
		postProcChoice.setSelectedIndex(0);
		
		postProcMaskField = new JTextField("3");

		
		reprocess = new JButton("Reprocess");
		reprocess.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try {
					process();
					redrawProcessedImageLabels();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel ImagePanel = new JPanel(new GridLayout(0,6));
		JPanel controlPanel = new JPanel(new GridLayout(0,5));
		
		controlPanel.add(noiseLabel);
		controlPanel.add(contrastLabel);
		controlPanel.add(alphaLabel);
		controlPanel.add(postLabel);
		controlPanel.add(postMaskLabel);
		
		controlPanel.add(noiseReductionChoice);
		controlPanel.add(contrastEnhanceChoice);
		controlPanel.add(alphaValue);
		controlPanel.add(postProcChoice);
		controlPanel.add(postProcMaskField);
		controlPanel.add(reprocess);

		
		// loads training images
		// also does initial image processing on launch with default values
		try {
			for (int i = 0; i < NUM_CLASSES; i++) {
				String training_class_dir = "training/" + four_training_order[i]+ "/";
				File classTrainingFolder = new File(training_class_dir);
				File[] listOfFiles = classTrainingFolder.listFiles();
				for (File file : listOfFiles) {
					if (file.isFile()) {
						String image_name = file.getName();
						BufferedImage image = readInImage(training_class_dir + image_name);
						
						// original image
						trainingImages.add(image);
						// original histogram before any processing
						originalHistograms.add(createHistogram(image));
					}
				}
			}
			
			// now we know how many files initialise Jlabel arrays
			preprocessedImgLabels = new JLabel[trainingImages.size()]; 
			ppHistoLabels = new JLabel[trainingImages.size()]; ;
			thresholdedImgLabels = new JLabel[trainingImages.size()]; ;
			postprocessedImgLabels = new JLabel[trainingImages.size()];
			
			// initial call to process with default values so something shows on launch
			process();

			for (int i = 0; i < trainingImages.size(); i++) {
				ImagePanel.add(new JLabel(new ImageIcon(trainingImages.get(i))));
				ImagePanel.add(new JLabel(new ImageIcon(originalHistograms.get(i))));
				preprocessedImgLabels[i] = new JLabel(new ImageIcon(preprocessedImages.get(i)));
				ImagePanel.add(preprocessedImgLabels[i]);
				ppHistoLabels[i] = new JLabel(new ImageIcon(preprocessedHistograms.get(i)));
				ImagePanel.add(ppHistoLabels[i]);
				thresholdedImgLabels[i] = new JLabel(new ImageIcon(thresholdedImages.get(i)));
				ImagePanel.add(thresholdedImgLabels[i]);
				postprocessedImgLabels[i] = new JLabel(new ImageIcon(postprocessedImages.get(i)));
				ImagePanel.add(postprocessedImgLabels[i]);
			}
			
			mainPanel.add(controlPanel);
			mainPanel.add(ImagePanel);
			JScrollPane scroll = new JScrollPane(mainPanel);
			frame.getContentPane().add(scroll);
			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			System.out.println("Error message");
			e.printStackTrace();
		}
	}
	
	private void redrawProcessedImageLabels() {
		for(int i = 0; i < trainingImages.size(); i++){
			preprocessedImgLabels[i].setIcon(new ImageIcon(preprocessedImages.get(i)));
			ppHistoLabels[i].setIcon(new ImageIcon(preprocessedHistograms.get(i)));
			thresholdedImgLabels[i].setIcon(new ImageIcon(thresholdedImages.get(i)));
			postprocessedImgLabels[i].setIcon(new ImageIcon(postprocessedImages.get(i)));
		}
		frame.repaint();
	}

	public static void main(String[] args) {
		new VisionSystem();
		}

	public BufferedImage readInImage(String filename) {
		BufferedImage img = ImageOp.readInImage(filename);
		return img;
	}

	public Image createHistogram(BufferedImage img) throws Exception {
		Histogram hist = new Histogram(img);
		GraphPlot gp = new GraphPlot(hist);
		return gp;
	}

	public void process() throws Exception {
		float alpha = Float.parseFloat(alphaValue.getText());
		int postProcMaskSize = Integer.parseInt(postProcMaskField.getText());
		boolean closeFirst = postProcChoice.getSelectedItem() == "Close -> Open";

		preprocessedImages.clear();
		preprocessedHistograms.clear();
		thresholdedImages.clear();
		postprocessedImages.clear();

		for (BufferedImage image : trainingImages) {
			// perform preprocessing - median noise reduction + contrast enhancement
			// contrast enhancement can be switched between linear stretch and histogram equalisation
			BufferedImage preProccedImg = Utility.preProcessAnImage(image, (String) noiseReductionChoice.getSelectedItem(), (String) contrastEnhanceChoice.getSelectedItem());
			preprocessedImages.add(preProccedImg);
			// histogram after any contrast change and noise reduction
			preprocessedHistograms.add(createHistogram(preProccedImg));
			// perform auto thresholding
			BufferedImage threshImg = Utility.performAutomaticThresholding(preProccedImg, alpha);
			thresholdedImages.add(threshImg);
			// perform post processing closes then opens if closeFirst true else opposite order
			BufferedImage postProcImg = Utility.postProcessAnImage(threshImg, closeFirst, postProcMaskSize);
			postprocessedImages.add(postProcImg);
		}
	}
}