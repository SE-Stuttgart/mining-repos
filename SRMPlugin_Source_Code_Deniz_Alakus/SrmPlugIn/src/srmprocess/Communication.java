package srmprocess;

import java.util.HashMap;
//import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class Communication {

	public Communication() {
		/**
		 * The constructor
		 */
	}

	public static boolean control = true;

	// -----------------------------------------------------------
	// Allgemeine Rueckgabe

	public String view(String str) {
		return str;
	}

	/**
	 * public List<String> view(List<String> liststr) { return liststr; }
	 */

	// -----------------------------------------------------------
	// Event wird gekappselt
	public void ViewCommunication(String key, Object obj, String comProtokoll) {

		BundleContext ctx = FrameworkUtil.getBundle(Communication.class).getBundleContext();
		ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
		EventAdmin eventAdmin = ctx.getService(ref);
		Map<String, Object> properties = new HashMap<String, Object>();

		properties.put(key, obj);
		Event event = new Event(comProtokoll, properties);
		eventAdmin.sendEvent(event);
	}

}
