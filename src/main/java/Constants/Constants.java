package Constants;

public class Constants {

	public static String pomLocation = "D:\\Anurag\\Workspaces\\MavenWorkspace\\demo3\\demo3";
	
	public static String dependencyTreeLocation = Constants.pomLocation+"\\dependency-tree.json";
	
	public static String dependencyCheckReportLocation = Constants.pomLocation+"\\target\\dependency-check-report.json";
	
	public static String[] dependencyTreeCommand = { "C:\\Program Files\\apache-maven-3.9.8\\bin\\mvn.cmd", 
            "dependency:tree", 
            "-DoutputType=json", 
            "-DoutputFile=dependency-tree.json" };
		
	public static String[] dependencyCheckCommand = { "C:\\Program Files\\apache-maven-3.9.8\\bin\\mvn.cmd", 
            "dependency-check:check"};
	
	public static String newPomFileDirectory = Constants.pomLocation+"\\tempPOMForDirectDependencies";
}
