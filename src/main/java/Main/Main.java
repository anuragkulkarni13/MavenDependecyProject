package Main;

import java.util.List;
import java.util.Map;

import Constants.Constants;
import DTO.DependencyDTO;
import DTO.VulnerabilityDTO;
import Operations.DependencyOperations;
import Operations.MavenCommandOperations;
import Operations.TreeExporter;
import Operations.TreeToDotConvertor;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// generate the dependency tree
		MavenCommandOperations.generateDependencyTree();
		
		// generate the dependency check report
		MavenCommandOperations.generateDependencyCheckReport();
		
		// parse the dependency tree
		DependencyDTO dependencyRoot = DependencyOperations.getDependencyTree();
		
		// get vulnerability list
		// fetching the metrics from cvssv3 and not cvssv2 as cvssv3 is latest check of the dependencies
		// there are some dependencies where there is only cvssv2 which are older checked dependencies before 2015
		Map<String, List<VulnerabilityDTO>> vulnerabilityMap = DependencyOperations.getVulnerabilityList();
		
		// add the vulnerability values in the DependencyDTO JSON
		dependencyRoot.updateVulberabilities(vulnerabilityMap);
		
		// traverse the tree
//		DependencyOperations.traverseDependencies(dependencyRoot);
		DependencyOperations.traverseDFS(dependencyRoot);

		//Export the JSON to a JSON file
		TreeExporter.exportTreeToJson(dependencyRoot, Constants.pomLocation+"\\tree-json.json");
        
		// Convert the JSON file to DOT file for GraphVIZ
//      TreeToDotConvertor.convertToDTO(dependencyRoot);
		TreeToDotConvertor.jsonTODOT(dependencyRoot);
		
		// algo for dependency change
		// bfs traversal
		

	}

}
