import java.awt.image.BufferedImage;

public class Classification 
{
	int smallMax, mediumMax;
	int[] areas, perimeters;
	ImageData[] imgData;
	public Classification()
	{
		smallMax =0;
		mediumMax =0;
	}
	
	public String classifyAnImage(int imageNumber, String si, String sh, BufferedImage source,int p, int a, int[] l, double c)
	{
		String size = classifySize(si,a);
		String shape = "";
		if(sh.equals("LD"))
			shape = classifyLinearDiscriminantRegularity((double)a,(double)p);
		else if (sh.equals("NN"))
			shape = classifyNearestNeighbourRegularity((double)a,(double)p);
		else if (sh.equals("C"))
			shape = classifyCompactness(c);
		else
			System.out.println("No/Incorrect shape method added into classifyAnImage.  Should be LD or NN");
		if(size.equals("None"))
			return "" + imageNumber;
		else
			return "" + imageNumber + " is " + size + " and is " + shape + " in shape. It is located at " + l[0] + "," + l[1];
	}
	
	public String classifySize(String t,int a)
	{
		if(a<=5)
		{
			return "None";
		}
		if(t.equals("LD"))
		{
			if(a<=smallMax)
			{
				return "Small";
			}
			else if(a<=mediumMax)
			{
				return "Medium";
			}
			else
			{
				return "Large";
			}
		}
		else if(t.equals("NN"))
		{
			int h1 = Integer.MAX_VALUE, h2 = Integer.MAX_VALUE , h3 = Integer.MAX_VALUE;
			ImageData[] i = new ImageData[3];
			//areaDifs[i] = Math.abs(areas[i]-a);
			for(int j=0;j<areas.length;j++)
			{
				if(Math.abs(imgData[j].area - a) <= h1)
				{
					h3 = h2;
					h2 = h1;
					h1 = Math.abs(imgData[j].area - a);
					i[2] = i[1];
					i[1] = i[0];
					i[0] = imgData[j];
				}
				else if(Math.abs(imgData[j].area - a) <= h2)
				{
					h3 = h2;
					h2 = Math.abs(imgData[j].area - a);
					i[2] = i[1];
					i[1] = imgData[j];
				}
				else if(Math.abs(imgData[j].area - a) <= h3)
				{
					h3 = Math.abs(imgData[j].area - a);
					i[2] = imgData[j];
				}
			}
			int s = 0,m = 0,l = 0;
			for(int k = 0; k<i.length;k++)
			{
				
				if (i[k].size.equals("Small"))
				{	s++;	}
				else if (i[k].size.equals("Medium"))
				{	m++;	}
				else if (i[k].size.equals("Large"))
				{	l++;	}
			}
			if(l>m && l>s)
			{return "Large";}
			else if(m>l && m>s)
			{return "Medium";}
			else if(s>m && s>l)
			{return "Small";}
		}
		return "ERROR!";
	}
	
	public String classifyNearestNeighbourRegularity(double a, double p)
	{		
		double h1 = Double.MAX_VALUE, h2 = Double.MAX_VALUE , h3 = Double.MAX_VALUE;
			ImageData[] i = new ImageData[3];
			double diffsq, diff;
			
			for(int j=0;j<areas.length;j++)
			{
				diffsq = Math.pow(Math.abs(imgData[j].area - a),2) + Math.pow(Math.abs(imgData[j].perimeter - p),2);
			diff = Math.sqrt(diffsq);
				if(diff <= h1)
				{
					h3 = h2;
					h2 = h1;
					h1 = diff;
					i[2] = i[1];
					i[1] = i[0];
					i[0] = imgData[j];
				}
				else if(diff <= h2)
				{
					h3 = h2;
					h2 = diff;
					i[2] = i[1];
					i[1] = imgData[j];
				}
				else if(diff <= h3)
				{
					h3 = diff;
					i[2] = imgData[j];
				}
			}
			int r = 0, ir = 0;
			for(int k = 0; k<i.length;k++)
			{
				
				if (i[k].reg.equals("Regular"))
				{	r++;	}
				else if (i[k].reg.equals("Irregular"))
				{	ir++;	}
			}
			if(r>ir)
			{return "Regular";}
			else if(ir > r)
			{return "Irregular";}
		
		return "";
	}
	
	public String classifyLinearDiscriminantRegularity(double a, double p)
	{
		double y, m, c;
		m = 0.138;
		c = 22.46;
		
		y=(m*a) + c;
		if (p>y)
		{return "Irregular";
		}
		else 
		{return "Regular";
		}
	}
	
	public String classifyCompactness(double c)
	{
		if((c>=9)&&(c<=16))
			return "Regular";
		else
			return "Irregular";
	}
	
	public void trainClassifications(int[] a, int[]p)
	{
		areas = a;
		perimeters = p;
		int smallmean = (a[0]+a[1]+a[2])/3;
		int mediummean = (a[3]+a[4]+a[5])/3;
		int largemean = (a[6]+a[7]+a[8])/3;
		smallMax = (smallmean+mediummean)/2;
		mediumMax = (mediummean+largemean)/2;
		
		int totala = 0;
		int totalp = 0;
		
		for(int i=0;i<a.length;i++)
		{
			totala += a[i];
			totalp += p[i];
		}
		
		imgData = new ImageData[9];
		
		for(int j=0;j<imgData.length;j++)
		{
			imgData[j] = new ImageData("","", a[j], p[j]);
		}

		imgData[0].size = "Small";
		imgData[0].reg = "Regular";
		imgData[1].size = "Small";
		imgData[1].reg = "Regular";
		imgData[2].size = "Small";
		imgData[2].reg = "Regular";
		imgData[3].size = "Medium";
		imgData[3].reg = "Irregular";		
		imgData[4].size = "Medium";
		imgData[4].reg = "Irregular";
		imgData[5].size = "Medium";
		imgData[5].reg = "Irregular";
		imgData[6].size = "Large";
		imgData[6].reg = "Regular";
		imgData[7].size = "Large";
		imgData[7].reg = "Regular";	
		imgData[8].size = "Large";
		imgData[8].reg = "Regular";			
	}
}

class ImageData
{
	String size, reg;
	int area, perimeter;
	
	public ImageData(String s, String r, int a, int p)
	{
		size = s;
		reg = r;
		area = a;
		perimeter = p;
	}
}







	

    