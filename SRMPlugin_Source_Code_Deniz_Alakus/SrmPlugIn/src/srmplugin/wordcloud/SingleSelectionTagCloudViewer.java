package srmplugin.wordcloud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.cloudio.internal.ui.TagCloud;
import org.eclipse.gef.cloudio.internal.ui.TagCloudViewer;
import org.eclipse.gef.cloudio.internal.ui.Word;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

@SuppressWarnings("restriction")
public class SingleSelectionTagCloudViewer extends TagCloudViewer {
	
	private Set<Word> selection = new HashSet<>();

	public SingleSelectionTagCloudViewer(TagCloud cloud) {
		super(cloud);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initSelectionListener() {
		super.getCloud().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				Word word = (Word) e.data;
				if (word == null)
					return;
				selection.clear();
				selection.add(word);
				getCloud().setSelection(selection);
				/* REALLY URGENT QUICKFIX
				boolean remove = selection.remove(word);
				if (!remove)
					selection.add(word);
				cloud.setSelection(selection);
				*/
			}

			@Override
			public void mouseDown(MouseEvent e) {

			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		getCloud().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Object> data = new ArrayList<>();
				@SuppressWarnings("unchecked")
				Set<Word> selected = (Set<Word>) e.data;
				for (Word word : selected) {
					if (word.data != null) {
						data.add(word.data);
					}
				}
				StructuredSelection selection = new StructuredSelection(data);
				fireSelectionChanged(new SelectionChangedEvent(SingleSelectionTagCloudViewer.this, selection));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	
}