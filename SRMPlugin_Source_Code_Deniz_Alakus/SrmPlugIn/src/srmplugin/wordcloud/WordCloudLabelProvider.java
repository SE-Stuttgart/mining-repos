package srmplugin.wordcloud;

import org.eclipse.gef.cloudio.internal.ui.ICloudLabelProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class WordCloudLabelProvider extends BaseLabelProvider implements ICloudLabelProvider{

	private Font font;
	private int maxOccurrences;

	public WordCloudLabelProvider(Font font) {
		this.font = font;
		maxOccurrences = 1;
	}

	@Override
	public String getLabel(Object element) {
		return ((myWord) element).getWord();
	}

	@Override
	public double getWeight(Object element) {
		return ((myWord) element).getCount() / maxOccurrences;
	}

	@Override
	public Color getColor(Object element) {
		return Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
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
		return ((myWord) element).getPath();
	}
	
	public void setMaxOccurrences(int occurrences) {
		this.maxOccurrences = occurrences;
	}
	public int getMaxOccurrences() {
		return maxOccurrences;
	}

}
