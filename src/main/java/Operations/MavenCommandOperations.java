package Operations;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import Constants.Constants;

public class MavenCommandOperations {

	public static void generateDependencyTree()
	{
		try {
			// Start the process
			ProcessBuilder processBuilder = new ProcessBuilder(Constants.dependencyTreeCommand);
			processBuilder.directory(new java.io.File(Constants.pomLocation));
			Process process = processBuilder.start();

			// Read the output (optional, since output is redirected to a file)
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			// Wait for the process to complete
			int exitCode = process.waitFor();
			System.out.println("Exited with code: " + exitCode);

			// Verify the output file
			java.io.File outputFile = new java.io.File(Constants.dependencyTreeLocation);
			if (outputFile.exists()) {
				System.out.println("Dependency tree successfully written to " + outputFile.getAbsolutePath());
			} else {
				System.out.println("Failed to create dependency tree file.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void generateDependencyTreeWithPath(String dependencyPath, String dependencyName)
	{
		try {
			
//			System.out.println("path "+dependencyPath);
			String[] dependencyTreeCommandWithPath = { "C:\\Program Files\\apache-maven-3.9.8\\bin\\mvn.cmd", 
					"-f",
					dependencyPath,
					"dependency:tree", 
		            "-DoutputType=json", 
		            "-DoutputFile="+Constants.newPomFileDirectory+"\\"+dependencyName+"-"+"dependency-tree.json" };

//			System.out.println("command : "+dependencyTreeCommandWithPath[0]+dependencyTreeCommandWithPath[1]+dependencyTreeCommandWithPath[2]+dependencyTreeCommandWithPath[3]+dependencyTreeCommandWithPath[4]+dependencyTreeCommandWithPath[5]);
//			System.out.println("newPomFileDirectory : "+Constants.newPomFileDirectory);
			// Start the process
			ProcessBuilder processBuilder = new ProcessBuilder(dependencyTreeCommandWithPath);
			processBuilder.directory(new java.io.File(Constants.newPomFileDirectory));
			Process process = processBuilder.start();

			// Read the output (optional, since output is redirected to a file)
//			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			String line;
//			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
//			}

			// Wait for the process to complete
			int exitCode = process.waitFor();
//			System.out.println("Exited with code: " + exitCode);

			// Verify the output file
			java.io.File outputFile = new java.io.File(Constants.newPomFileDirectory+"\\"+dependencyName+"-"+"dependency-tree.json");
			if (outputFile.exists()) {
				System.out.println("Dependency tree successfully written to " + outputFile.getAbsolutePath());
			} else {
				System.out.println("Failed to create dependency tree file.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateDependencyCheckReport()
	{
		try {
			// Start the process
			ProcessBuilder processBuilder = new ProcessBuilder(Constants.dependencyCheckCommand);
			processBuilder.directory(new java.io.File(Constants.pomLocation));
			Process process = processBuilder.start();

			// Read the output (optional, since output is redirected to a file)
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			// Wait for the process to complete
			int exitCode = process.waitFor();
			System.out.println("Exited with code: " + exitCode);

			// Verify the output file
			java.io.File outputFile = new java.io.File(Constants.dependencyCheckReportLocation);
			if (outputFile.exists()) {
				System.out.println("Dependency tree successfully written to " + outputFile.getAbsolutePath());
			} else {
				System.out.println("Failed to create dependency tree file.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
