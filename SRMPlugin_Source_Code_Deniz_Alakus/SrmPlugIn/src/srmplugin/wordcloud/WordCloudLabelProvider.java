package srmplugin.wordcloud;

import java.util.Date;

import org.eclipse.gef.cloudio.internal.ui.ICloudLabelProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import srmplugin.Preferences;

public class WordCloudLabelProvider extends BaseLabelProvider implements ICloudLabelProvider {

	private Font font;
	private int maxOccurrences;

	public WordCloudLabelProvider(Font font) {
		this.font = font;
		maxOccurrences = 1;
	}

	@Override
	public String getLabel(Object element) {
		return ((MyWord) element).getWord();
	}

	@Override
	public double getWeight(Object element) {
		return ((MyWord) element).getCount() / maxOccurrences;
	}

	@Override
	public Color getColor(Object element) {
		
		String noFile = ((MyWord) element).getWord();
		if(noFile.equals("The selected File does not exist in the Outputtable")){
			return new Color(Display.getCurrent(), 0, 0, 255);
		}

		java.awt.Color helpColor = new java.awt.Color(0, 0, 255);
		float[] hsb = new float[3];
		java.awt.Color.RGBtoHSB(helpColor.getRed(), helpColor.getGreen(), helpColor.getBlue(), hsb);
		

		String path = ((MyWord) element).getPath();
		hsb[1] = computeSaturation(path);
		System.out.println("HSB of " + path + ": " + hsb[0] + " " + hsb[1] + " " + hsb[2]);
		
		java.awt.Color helpColor2 = new java.awt.Color(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));		
		Color colorRes = new Color(Display.getCurrent(), helpColor2.getRed(), helpColor2.getGreen(), helpColor2.getBlue());

		return colorRes;
	}

	private float computeSaturation(String path) {

		Date lastCommitDate = DateTable.getLastCommitDate(path);
		long val = lastCommitDate.getTime();
		long upperbound = Preferences.now.getTime();
		long lowerbound = Preferences.getLimit().getTime();

		float mapped = map(val,lowerbound,upperbound,70,100) / 100;
		
		return mapped;
	}

	static public final float map(float value, float istart, float istop, float ostart, float ostop) {
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}

	@Override
	public FontData[] getFontData(Object element) {
		return font.getFontData();
	}

	@Override
	public float getAngle(Object element) {
		return (float) (0);
	}

	@Override
	public String getToolTip(Object element) {
		return ((MyWord) element).getPath();
	}

	public void setMaxOccurrences(int occurrences) {
		this.maxOccurrences = occurrences;
	}

	public int getMaxOccurrences() {
		return maxOccurrences;
	}

}
