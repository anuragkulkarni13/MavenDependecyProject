package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cache.DependencyCache;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import common.dto.VulnerabilityDTO;
import pomanalyzer.PomOperations;
import pomanalyzer.TempPomCreator;
import recommendations.POMOptimization;
import recommendations.POMRecommendation;

public class main {

	public static void main(String[] args) {
		
		Map<String, Integer> FinalChanges = new HashMap<>();
		
		// prerequisites
		// cache setup
		DependencyCache.createCache();
			
		// temp pom creation and setup
		TempPomCreator.createAndSetupTempPOM();
		
		// get direct depndencies from original pom
		List<POMDependencyDTO> pomDependencies = new ArrayList<>();
		pomDependencies = PomOperations.getDirectDepndenciesFromOriginalPOM();
		
		//parsing of all the dependencies
		for(POMDependencyDTO pomDependency : pomDependencies)
		{
			//get recommendations for the pomDependency
			List<DependencyDTO> pomRecommendations = POMRecommendation.getRecommendationsForPOMDependency(pomDependency);
			
			for(DependencyDTO d : pomRecommendations)
			{
				System.out.println("############################# POm Recommendations ############################");
				System.out.println(d.getArtifactId());
				System.out.println(d.getGroupId());
				System.out.println(d.getVersion());
			}
			
			//combinations
			List<POMDependencyDTO> tempPomDependencies = new ArrayList<>();
			tempPomDependencies.add(pomDependency);
			Map<String, Integer> leastVulCountCombinationMap = POMOptimization.optimizePOMRecommendations(pomDependency, pomRecommendations);
			
			FinalChanges.putAll(leastVulCountCombinationMap);
		}
		
		String key = "";
		int leastVulCount = 0;
		for (Map.Entry<String, Integer> entry : FinalChanges.entrySet()) {
//		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().size());
		    key = entry.getKey();
		    leastVulCount = entry.getValue();
		}
		
		System.out.println();
		System.out.println("############################################ Final Changes #################################");

		String[] arr = key.split(",");
		for(String a : arr)
		{
			System.out.println();
			String[] brr = a.split("#");
			System.out.println(brr[0]);
			System.out.println(brr[1]);
			System.out.println(brr[2]);
		}
//		System.out.println("######################################### Vulnerability count #########################################");
//		System.out.println(leastVulCount);
		
		// final combinations
//		POMOptimization.optimizePOMRecommendations(pomDependencies, pomRecommendations);
		
		//
		
		
		//
	}

}