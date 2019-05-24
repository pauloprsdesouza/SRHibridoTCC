package tagging;
import java.util.List;
import java.util.ArrayList;
import database.DBFunctions;
import model.Cenario;

public class CBRecommender {

	public static long init;
	public static long end;

	public static void main(String[] args) {

		/*
		 * Quantidade de usuarios
		 */

		init = System.currentTimeMillis();
		//CreateScenario.createScenario();
				
		Integer[] listUsers = {11,96, 121, 129, 133, 190, 205, 208, 271, 279, 316, 318, 320, 342, 348, 359, 370, 395,
				409, 451, 460, 469, 471, 482, 489, 500, 505, 534, 540, 558, 570, 586, 619, 631, 662, 693, 694, 700, 707,
				729, 739, 741, 768, 770, 786, 787, 819, 829, 858, 887, 888, 910, 964, 969, 971, 975, 1015, 1166, 1244,
				1268, 1271, 1277, 1288, 1339, 1376, 1387, 1408, 1418, 1447, 1453, 1469, 1483, 1486, 1507, 1515, 1516,
				1518, 1523, 1584, 1587, 1588, 1593, 1602, 1616, 1619, 1623, 1629, 1644, 1662, 1678, 1686, 1705, 1719,
				1738, 1741, 1755, 1763, 1775, 1816, 1826 };
		
		
		for(int i=0; i < listUsers.length; i++) {
		
			List<Cenario> TestSets = DBFunctions.selectCenario(listUsers[i]);
			List <Integer> listTestSet= new ArrayList<Integer>();
		
			for (Cenario testSet : TestSets) {
					listTestSet.add(testSet.getId_filme());
			}
			
			TaggingFactory.createRecomedationSystem(TestSets, listUsers[i], listTestSet);
		}
	}
}
