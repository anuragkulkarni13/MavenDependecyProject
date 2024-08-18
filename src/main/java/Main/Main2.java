package Main;

import Algorithm.Algo1;
import Constants.Constants;
import DTO.DependencyDTO;
import Operations.DependencyOperations;
import Operations.MavenCommandOperations;

public class Main2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// get the dependency tree from the pom location
		DependencyDTO dependencyRoot = DependencyOperations.getDependencyTree();

		////////////////Algo1
		
		//build the version mapping table
		Algo1.buildVersionMappingTable(dependencyRoot);
		
		// parse the vulnerabilities and create a vulnerability wise solution Map
		
		
		// find the optimal version
		
	}

}
