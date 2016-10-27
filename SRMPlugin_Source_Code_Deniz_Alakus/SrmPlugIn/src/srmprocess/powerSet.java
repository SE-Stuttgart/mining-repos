package srmprocess;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

//potenz menge islemi yapilmaktaki Klassifikationda kullaniliyor
public class powerSet {

	public powerSet() {
	}

	// also er kann 15 commitId potentzmege teilen f�r jede 15 eintrage braucht
	// er 55 sekunde
	// Ist das sinn vollllllll?????????????

	@SuppressWarnings({ "rawtypes", "unchecked" })
	Set<Set<String>> powerSetofNodes(Set<String> hs) {
		Set<Set<String>> powerSet = new HashSet<>(), runSet = new HashSet<>(), thisSet = new HashSet<>();

		while (powerSet.size() < (Math.pow(2, hs.size()) - 1)) {
			if (powerSet.isEmpty()) {
				for (String o : hs) {
					Set<String> s = new TreeSet<>();
					s.add(o);
					runSet.add(s);
					powerSet.add(s);
				}
				continue;
			}
			for (String o : hs) {
				for (Set<String> s : runSet) {
					Set<String> s2 = new TreeSet<>();
					s2.addAll(s);
					s2.add(o);
					powerSet.add(s2);
					thisSet.add(s2);
				}
			}
			runSet.clear();
			runSet.addAll(thisSet);
			thisSet.clear();
		}
		powerSet.add(new TreeSet());
		return powerSet;
	}

}
