package Algorithm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Constants.Constants;
import DTO.DependencyDTO;
import Operations.DependencyOperations;
import Operations.DependencyVersionOperations;
import Operations.MavenCommandOperations;

public class Algo1 {

	// function to build the version mapping table
	public static void buildVersionMappingTable(DependencyDTO rootDependency)
	{
		
		// delete the directory structure which holds the temp pom files
		deleteDirectoryStructure();
		
		for(DependencyDTO child : rootDependency.getChildren())
		{
			System.out.println(child.getArtifactId());

			// create the directory structure and temporary pom files for all the direct dependencies
			String newChildPomPath = VersionMappingTableOperations.createTempPOMForDirectDependencies(child);
			
			// get the version list of the given dependency
			List<String> versionList = new ArrayList<String>();
			versionList = DependencyVersionOperations.getDirectDependencyVersionList(child.getGroupId(), child.getArtifactId());

			// get all the versions for the given dependency
			List<String> versionSubList = VersionMappingTableOperations.getAllVersionsAfterCurrentVersion(child, child.getVersion(), versionList);
			
			List<Map<String,String>> treeVersionHashMapList = new ArrayList<>();
			int count = 0;
			for(String version : versionSubList)
			{
				// change the version in the pom
				VersionMappingTableOperations.updateDependencyVersion(newChildPomPath, child.getGroupId(), child.getArtifactId(), version);

				// generate the dependency tree json for the child dependency
				MavenCommandOperations.generateDependencyTreeWithPath(newChildPomPath, child.getArtifactId());
				
				// record the tree dependency versions in hashmap
				// child.getArtifactId()-dependency-tree
//				System.out.println(newChildPomPath);
				DependencyDTO dependencyRoot = VersionMappingTableOperations.getUpdatedDependencyTree(Constants.newPomFileDirectory+"\\"+child.getArtifactId()+"-dependency-tree.json");
				Map<String,String> treeVersionHashMap = new HashMap<>();
				System.out.println(child.getVersion());
				VersionMappingTableOperations.traverseAndRecordAllDependencyVersions(dependencyRoot, treeVersionHashMap);
//				System.out.println(treeVersionHashMap);
				
				treeVersionHashMapList.add(treeVersionHashMap);
//				count++;
//				if(count > 3)
//				{
//					break;
//				}
			}
			System.out.println(treeVersionHashMapList);
			
			break;
		}
	}
	
	
	
	
	// function to delete the Directory structure for temporary pom files
	public static void deleteDirectoryStructure()
	{
		String newPomFileDirectory = Constants.pomLocation+"\\tempPOMForDirectDependencies"; // Replace with the new location and name
		try {
			if(Files.exists(Paths.get(newPomFileDirectory)) && Files.isDirectory(Paths.get(newPomFileDirectory)))
			{
		        deleteDirectoryRecursively(Paths.get(newPomFileDirectory));					
			}
		}catch (Exception e) {
			e.printStackTrace();
		}		
	}

	// function to recursively delete the files in the directory
    private static void deleteDirectoryRecursively(Path path) throws IOException {
        // Walk the file tree and delete files and subdirectories first
        Files.walk(path)
            .sorted((p1, p2) -> p2.compareTo(p1)) // Reverse order so that files and subdirectories are deleted before the parent directory
            .forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete " + p, e);
                }
            });
    }
}
