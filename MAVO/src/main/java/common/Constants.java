package common;

public class Constants {

	public static int ROWS_PER_PAGE = 100;  // Number of results per page

	public static String DB_DIRECTORY_VUL = "D:/Anurag/pom";
	public static String DB_NAME_VUL = "VulnerabilityCache.db";
	public static String DB_URL_VUL = "jdbc:sqlite:" + DB_DIRECTORY_VUL + "/" + DB_NAME_VUL;
	
	public static String DB_DIRECTORY_COMB = "D:/Anurag/pom";
	public static String DB_NAME_COMB = "CombinationsCache.db";
	public static String DB_URL_COMB = "jdbc:sqlite:" + DB_DIRECTORY_COMB + "/" + DB_NAME_COMB;
	
	public static String originalPomFileLocation = "D:\\Anurag\\pom\\pom.xml";

	public static String originalPomLocation = "D:\\Anurag\\pom";

	public static String newTempPomLocation = originalPomLocation+"\\tempPOMForDirectDependencies";

	public static String newTempPomFileLocation = newTempPomLocation+"\\temppom.xml";
	
	public static String dependencyTreeLocation = newTempPomLocation+"\\dependency-tree.json";

	public static String dependencyCheckReportLocation = newTempPomLocation + "\\dependency-check-report.json";

	public static String filePath = newTempPomLocation + "\\VulMap.txt";
}
