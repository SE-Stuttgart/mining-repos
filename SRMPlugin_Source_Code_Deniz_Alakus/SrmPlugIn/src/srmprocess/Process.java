package srmprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Process {

	public Process() {
		// TODO Auto-generated constructor stub
	}

	DBConnection dataBaseCon = DBConnection.getDBConnection();
	powerSet ps = new powerSet();
	// Variablen für Cluster Analyse
	public static List<List<String>> ClusterToDiagram;
	private Hashtable<List<String>, List<List<String>>> Cluster;
	private List<List<String>> TempCluster;
	public static List<List<String>> ClusterInput;
	public static List<List<String>> ClusterErgebnis;
	// Variablen für Klaasifikation Analyse
	public static List<List<String>> KlassifikationErgebnis;
	private List<String> CommitIDGruppekey;
	private boolean CommitIDGruppeControl = true;
	private static Hashtable<List<String>, List<List<String>>> CommitIDGruppe;
	private static Hashtable<List<String>, Integer> CommitIDGruppeSort;
	private List<List<String>> TempCommitIDGruppe;

	// ----Diese Methode wird in der CoupledChnages.java nach der Path
	// bestimmung angeruffen.
	public void Run(String buffer, String perspective) {
		// Ausgewählte Path/File wird inder Outputtable gesucht.
		dataBaseCon.ReadOutputTable(buffer);
		ClusterInput = new ArrayList<>();
		ClusterInput = DBConnection.sqlprocedureInput;
		// Cluster Analyse
		Cluster();
		// Nur aufrufen falls wir in der richtigen Perspective sind
		if (perspective.equals("SrmPlugIn.perspective2")) {
			// Klassifikation Analyse
			SubSet();
		}
	}

	// ---------------Cluster Analyse------------------
	public void Cluster() {
		int i, j, k, l, m;
		ClusterToDiagram = new ArrayList<>();
		Cluster = new Hashtable<List<String>, List<List<String>>>();
		for (i = ClusterInput.size() - 1; i >= 0; i--) {
			// Falls Transaktionen auf kein mal als Subcluster von einem Cluster
			// gespeichert würde:
			if (searchCluster(i)) {
				TempCluster = new ArrayList<>();
				for (k = ClusterInput.size() - 1; k > -1; k--) {
					if (i != k) {
						m = 0;
						for (l = 2; l < ClusterInput.get(k).size(); l++) {
							for (j = 2; j < ClusterInput.get(i).size(); j++) {
								// Formel: ({d_k^(CommitId (1…n))⊆d_i^(CommitId
								// (1…n)) }⋀{d_k^(Item (1…n))⊆d_i^(Item (1…n))
								// })
								if (ClusterInput.get(k).get(l).equals(ClusterInput.get(i).get(j))
										&& m < Integer.parseInt(ClusterInput.get(k).get(0))
												+ Integer.parseInt(ClusterInput.get(k).get(1))
										&& !ClusterInput.get(k).get(l).equals("null")) {
									m++;
									break;
								}
							} // for j
						} // for l
							// Falls die Oben genante formel gilt, wird die
							// Transaktionen "d_k" in der Tempvalue gespeichert.
						if (m == Integer.parseInt(ClusterInput.get(k).get(1))
								+ Integer.parseInt(ClusterInput.get(k).get(0))) {
							TempCluster.add(ClusterInput.get(k));
						}
					} // if in k
					else {
						continue;
					}
				} // for k
				// Nach der ganze durchlauf, falls min ein TRansaktion als
				// subcluster bezeichnet/ in Tempvalue gespeichert
				// dann wird die Key und Tempvalue als in Hastable gespeichert
				if (k == -1 && !TempCluster.isEmpty()) {
					Cluster.put(ClusterInput.get(i), TempCluster); // Hastable
																	// kayit
																	// yaptik
					// Nach der ganze durchlauf, falls kein TRansaktion als
					// subcluster bezeichnet/ in Tempvalue gespeichert,
					// dann wird die Key als Tempvalue gespeichert, dananch Key
					// und tempvalue in Hastable gespeichert
				} else if (k == -1 && TempCluster.isEmpty()) {
					TempCluster.add(ClusterInput.get(i));
					Cluster.put(ClusterInput.get(i), TempCluster);
				} // else if
			} // 1. if
			else {
				continue;
			}
		}
		// Alle Key Werte von "Cluster" HAstable werden in der separeten
		// Arraylist gespeichert.
		setClusterToList();
		// Falls die Cluster Ergebnisse in der Diagram angezeigt wurde.
		// setClusterToDiagram();
	}

	// --------------aktuelle di daha önce herhangi bir cluster islemin de
	// valuolarak kullanilmamis ise true kullanilmis ise false degeri geri
	// dönmektedir
	public boolean searchCluster(int index) {
		for (Map.Entry<List<String>, List<List<String>>> entry : Cluster.entrySet()) {
			List<List<String>> values = entry.getValue();
			if (values.contains(ClusterInput.get(index))) {
				// CLuster olarak kullanilmak istenen bu deger önce herhangi bir
				// clusterda valu oalrak kullanildgi icin tekrar cluster olak
				// kullanilmasinin arastirmayacgiz
				return false;
			}
		}
		// CLuster olarak kullanlbiiriz daha önceki clusterlarin valu
		// degerlerinin icinde yoktur.
		return true;
	}

	// ---------------Die Werte, die in Coupled Chages angezeit werden, werden
	// hier von der Key "Cluster" extrahiert.
	// Falls In Coupled Changes ein File Gruppe auswählt würde, wird direkt sie
	// aus dieser Array rausgefunden und weiter arbeitet.
	private void setClusterToList() {
		ClusterErgebnis = new ArrayList<>();
		for (Map.Entry<List<String>, List<List<String>>> entry : Cluster.entrySet()) {
			List<String> key = entry.getKey();
			ClusterErgebnis.add(key);
		}
	}

	// Jeder Key von Cluster hat mit der Vlaue CLuster eingebunden.
	// Key von Cluster besteht aus Items. Diese Items werden in zugehörige Value
	// des Key auch gesucht angezahlt für Diagram zeichnen.

	/**
	 * private void setClusterToDiagram(){ int TmpAnzahl=0;//prozent hesaplamada
	 * kullanilmakta
	 * 
	 * for (Map.Entry<List<String>, List<List<String>>> entry :
	 * Cluster.entrySet()) { List<String> key = entry.getKey();
	 * List<List<String>> values = entry.getValue(); List<String> tmpStr = new
	 * ArrayList<>();
	 * 
	 * for(int x=2+Integer.parseInt(key.get(0));x<key.size();x++){//file support
	 * +2 den basliyoz ki sadece file kiyalsla yapmak icin int temp=1; for(int
	 * y=0;y<values.size();y++){ for(int
	 * z=2+Integer.parseInt(values.get(y).get(0));z<values.get(y).size();z++){
	 * ////file support +2 den basliyoz ki sadece file kiyalsla yapmak icin
	 * if(key.get(x).equals(values.get(y).get(z))&&!key.get(x).equals("null")&&!
	 * values.get(y).get(z).equals("null")){ temp++; break; } } }
	 * if(!key.get(x).equals("null")){ TmpAnzahl+=temp; // prozent hesaplamak
	 * icin tmpStr.add(key.get(x).substring(key.get(x).lastIndexOf("/")));
	 * tmpStr.add(String.valueOf(temp)); } }
	 * 
	 * // werden Prozent werte des Items in Zahl Foramt 0,00 convertiert.
	 * for(int w=1;w<tmpStr.size();w+=2){ tmpStr.set(w,
	 * String.valueOf(roundScale(Double.parseDouble(tmpStr.get(w))*(100/
	 * TmpAnzahl)))); }
	 * 
	 * 
	 * ClusterToDiagram.add(tmpStr); TmpAnzahl=0;
	 * 
	 * } }
	 */

	/**
	 * 
	 * Die statische rint()-Methode lässt sich auch einsetzen, wenn Zahlen auf
	 * zwei Nachkommastellen gerundet werden sollen. Ist d vom Typ double, so
	 * ergibt der Ausdruck Math.rint(d*100.0)/100.0 die gerundete Zahl.
	 * 
	 */

	/**
	 * private double roundScale( double d ) { return Math.rint( d * 100 ) /
	 * 100.; }
	 * 
	 */

	// --------- Klassifikation Analyse------------

	private void SubSet() {
		CommitIDGruppe = new Hashtable<List<String>, List<List<String>>>();
		CommitIDGruppeSort = new Hashtable<List<String>, Integer>();
		KlassifikationErgebnis = new ArrayList<>();
		// Jeder Transaktionen werden iterativ durch.
		for (int i = 0; i < DBConnection.sqlcommitInput.size(); i++) {
			Set<String> hs = new HashSet<String>();
			// Anzahl der Commit-IDs der TRansaktionen weniger als 10 betracht.
			if (Integer.parseInt(DBConnection.sqlcommitInput.get(i).get(0)) <= 10) {
				for (int j = 2; j < Integer.parseInt(DBConnection.sqlcommitInput.get(i).get(0)) + 2; j++) {
					// Commit-IDs für die Potenzmenge Methode vorbereitet.
					hs.add(DBConnection.sqlcommitInput.get(i).get(j));
				}
			} else {
				// Falls Anzahl der Commit-IDs mehr als 10 betrachtet, dann
				// werden von der erste 10 Commit-IDs weiter analysiert.
				for (int j = 2; j < 12; j++) {
					// Commit-IDs für die Potenzmenge Methode vorbereitet.
					hs.add(DBConnection.sqlcommitInput.get(i).get(j));
				}
			}
			// Zuerst werden Potenztmenge von der Commit-IDs bestimmt.
			for (Set<String> s : ps.powerSetofNodes(hs)) {
				// Falls akt. Potenzmenge>=2 dann wird es weiter analysiert.
				if (s.size() >= 2) {
					// akt. Potenzmge als zuesrt String betrachtet, danacht in
					// Arraylist gespeichert,
					// damit werden sie weiter analysiert.
					String[] array = s.toArray(new String[s.size()]);
					CommitIDGruppekey = new ArrayList<>();
					for (int h = 0; h < array.length; h++) {
						CommitIDGruppekey.add(array[h]);
					}

					CommitIDGruppeControl = false;
					// Falls akt. Potenzmenge schonmal auf keinen mal als Key Werte von CommitIDGruppe genutzt wurde.
					if (searchSubSet())
					{
						Klassifikation(); // Dann Kalssifikationsanaylse durch geführt.
					} else {
						continue;
					}
				}
			}
		}
	}

	// -------------Hier werden inhalt der akt. Potnezmnge in den Key von
	// CommitIDGruppe durchgesucht.

	public boolean searchSubSet() {
		int index;
		List<String> key = null;
		for (Map.Entry<List<String>, List<List<String>>> entry : CommitIDGruppe.entrySet()) {
			key = entry.getKey();
			index = 0;
			int j = 0;
			for (int i = 0; i < key.size(); i++) {
				j = 0;
				for (; j < CommitIDGruppekey.size(); j++) {
					if (!key.get(i).equals(CommitIDGruppekey.get(j)) && index < CommitIDGruppekey.size()) {
						index++;
						break;
					}
				} // for j
			} // for i
				// Inhalt der akt. Pontenzmege bereits in "Key" CommitIDGruppe
				// vorhanden.
			if (index != CommitIDGruppekey.size() && j == key.size()) {
				CommitIDGruppeControl = true;
				return false;
			} 
			// Inhalt der akt. Potenzmenge in "Key" CommitIDGruppe nicht existiert;
			// bzw Inhalt der akt. Potenzmenge teilweise in CommitIDGruppe übereinstimmt, aber nicht ganz.
			else if (index == CommitIDGruppekey.size() && j == key.size()) {
				CommitIDGruppeControl = true;
				return true;
			}
		} // for map
		// alle Key von CommitIDGruppe durchgesucht und akt. Potenzmenge auch
		// teilwiese nicht vorhanden ist.
		if (CommitIDGruppeControl == false) {
			CommitIDGruppeControl = true;
			return true;
		} else {
			return false;
		}
	}

	// -----------------Klassifikation --------------
	public void Klassifikation() {
		int j, k, m;
		Integer Support = 0;
		TempCommitIDGruppe = new ArrayList<>();
		for (int i = 0; i < DBConnection.sqlcommitInput.size(); i++) {
			m = 0;
			j = 2;
			for (k = 0; k < CommitIDGruppekey.size(); k++) {
				for (; j < Integer.parseInt(DBConnection.sqlcommitInput.get(i).get(0)) + 2; j++) {
					// 2 baslatiyoz cünkü ilk iki deger de commitId yok ve
					// sadece support kadar sirayi kontrol ediyoz
					// ikzaten elimizde okdar commit id var
					if (DBConnection.sqlcommitInput.get(i).get(j).equals(CommitIDGruppekey.get(k))
							&& !DBConnection.sqlcommitInput.get(i).get(j).equals("null")
							&& m < CommitIDGruppekey.size()) {
						m++;
						break;
					}
				} // for j
			} // for k
			if (k == CommitIDGruppekey.size() && m != CommitIDGruppekey.size()) {
			} else if (k == CommitIDGruppekey.size() && m == CommitIDGruppekey.size()) {
				TempCommitIDGruppe.add(DBConnection.sqlcommitInput.get(i));
				Support++;
			}
		} // for i
		CommitIDGruppe.put(CommitIDGruppekey, TempCommitIDGruppe); // Hastable kayit yaptik
		CommitIDGruppeSort.put(CommitIDGruppekey, Support);
	}

	/**
	 * Transfer as List and sort it:
	 * http://stackoverflow.com/questions/5176771/sort-hashtable-by-values
	 * absteigend sortieren (größter Wert zuerst)
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<Map.Entry<?, Integer>> sortValue() {
		// Transfer as List and sort it
		ArrayList<Map.Entry<?, Integer>> l = new ArrayList(CommitIDGruppeSort.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<?, Integer>>() {
			public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		return l;
	}

	// Hier werden Tranaktionen, die inder Vlau des Key Werts gekoppelt sind
	// aufsteigend durch "Support" sortiert (kleinster Wert zuerst)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<Map.Entry<?, Integer>> sortValueforCluster(
			Hashtable<List<String>, Integer> searchRegressionforSort) {
		// Transfer as List and sort it
		ArrayList<Map.Entry<?, Integer>> l = new ArrayList(searchRegressionforSort.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<?, Integer>>() {
			public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return l;
	}

	/**
	 * Werden im Ranking Information Tab des Commit Changes View eine Commit-ID
	 * Gruppe ausgewählwen, dann gekoppelte Transaktionen dieser Gruppe aus der
	 * "Vlaue" CommitIDGruppe bestimmt. Danach werden diese TRanaktionen durch
	 * Support Wert aufsteigend sortiert.
	 * 
	 */

	@SuppressWarnings("unchecked")
	public void searchKlassifikation(List<String> commitsearch) {
		int index;
		List<List<String>> searchErgebnis = new ArrayList<>();
		Hashtable<List<String>, Integer> searchRegressionErgebnisforSort = new Hashtable<List<String>, Integer>();
		ArrayList<Entry<?, Integer>> CommitSortforCluster;// = new ArrayList<>();
		for (Map.Entry<List<String>, List<List<String>>> entry : CommitIDGruppe.entrySet()) {
			List<String> key = entry.getKey();
			List<List<String>> values = entry.getValue();
			if (key.size() == commitsearch.size()) {
				index = 0;
				int i = 0;
				for (; i < key.size(); i++) {
					for (int j = 0; j < commitsearch.size(); j++) {
						if (key.get(i).equals(commitsearch.get(j)) && index < commitsearch.size()) {
							index++;
							break;
						}
					} // for j
				} // for i
				if (index == commitsearch.size() && i == key.size()) {
					for (int z = 0; z < values.size(); z++) {
						searchRegressionErgebnisforSort.put(values.get(z), Integer.parseInt(values.get(z).get(1)));
					}
					CommitSortforCluster = sortValueforCluster(searchRegressionErgebnisforSort);
					for (int y = 0; y < CommitSortforCluster.size(); y++) {
						searchErgebnis.add((java.util.List<String>) CommitSortforCluster.get(y).getKey());
					}
					ClusterInput = new ArrayList<>();
					ClusterInput = searchErgebnis;
					Cluster();
				}
			}
		} // for map
	}
}
