package Operations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import Constants.Constants;
import DTO.DependencyDTO;

public class TreeToDotConvertor {

	
	public static void jsonTODOT(DependencyDTO root)
	{
		StringBuilder dotFormat = new StringBuilder();
        dotFormat.append("digraph G {\n");
        dotFormat.append("node [style=filled];");
        traverseDOT(root, dotFormat);
        dotFormat.append("}");
        
        System.out.println(dotFormat);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.pomLocation+"\\tree.dot"))) {
            writer.write(dotFormat.toString());
            System.out.println("DOT file created: " + Constants.pomLocation+"\\tree.dot");
        } catch (IOException e) {
            System.err.println("Error writing DOT file: " + e.getMessage());
        }
        runGraphviz("tree.dot", "tree.png");

	}
	
	public static void traverseDOT(DependencyDTO root, StringBuilder dotFormat)
	{
		List<DependencyDTO> children = root.getChildren();
		if(root.getVulnerabilities().size()>0)
		{
			dotFormat.append("\""+root.getArtifactId()+" : "+root.getVulnerabilities().size()+"\""+"[fillcolor=red];");
		}
		for(DependencyDTO child : children)
		{
			if(child.getVulnerabilities().size()>0)
			{
				dotFormat.append("\""+child.getArtifactId()+" : "+child.getVulnerabilities().size()+"\""+"[fillcolor=red];");
			}
			dotFormat.append("\""+root.getArtifactId()+" : "+root.getVulnerabilities().size()+"\""+" -> "+"\""+child.getArtifactId()+" : "+child.getVulnerabilities().size()+"\""+";\n");
			traverseDOT(child, dotFormat);
		}
	}
    
    private static void runGraphviz(String dotFileName, String outputFileName) {
        try {
        	// Construct the command
            String[] command = {"C:\\Program Files\\Graphviz\\bin\\dot", "-Tpng", dotFileName, "-o", outputFileName};

            System.out.println(command.toString());
            // Execute the command
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(Constants.pomLocation)); // Set working directory
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Graph generated: " + outputFileName);
            } else {
                System.err.println("Graphviz command failed with exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error running Graphviz command: " + e.getMessage());
        }
    }
}
