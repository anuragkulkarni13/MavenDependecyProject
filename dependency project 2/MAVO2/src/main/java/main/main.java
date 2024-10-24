package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cache.DependencyCache;
import common.Constants;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import pom.PomOperations;
import pom.TempPomCreator;
import recommendations.POMOptimization;
import recommendations.POMRecommendation;

public class main {

	public static void main(String[] args) {
		
		Map<String, Integer> FinalChanges = new HashMap<>();
		
		// prerequisites
		// cache setup
		DependencyCache.createCache();
		
		// temp pom creation and setup
//		TempPomCreator.createAndSetupTempPOM();
		
		String parentVersion = PomOperations.getParentVersion(Constants.newTempPomFileLocation);
		List<List<POMDependencyDTO>> dependencies = PomOperations.getParentDirectAndIndirectDependencies(Constants.newTempPomFileLocation);
		
		List<POMDependencyDTO> directDependencies = dependencies.get(0);
		List<POMDependencyDTO> externalDependencies = dependencies.get(1);
		List<POMDependencyDTO> allDependencies = new ArrayList<>();
		allDependencies.addAll(directDependencies);
		allDependencies.addAll(externalDependencies);
		
		System.out.println("direct dependencies");
		for(POMDependencyDTO p : directDependencies)
		{
			System.out.println(p.getGroupId());
			System.out.println(p.getArtifactId());
			System.out.println(p.getVersion());
		}
		
		System.out.println("external dependencies");
		for(POMDependencyDTO p : externalDependencies)
		{
			System.out.println(p.getGroupId());
			System.out.println(p.getArtifactId());
			System.out.println(p.getVersion());
		}

		List<DependencyDTO> pomRecommendations = new ArrayList<>();
		for(POMDependencyDTO pomDependency : allDependencies)
		{
			//get recommendations for the pomDependency
//			pomRecommendations.addAll(POMRecommendation.getRecommendationsForPOMDependency(pomDependency, parentVersion));
			List<DependencyDTO> pomDependencyRecommendations = POMRecommendation.getRecommendationsForPOMDependency(pomDependency, parentVersion);
			for(DependencyDTO p : pomDependencyRecommendations)
			{
				boolean pomFind = false;
				for(DependencyDTO p1 : pomRecommendations)
				{
					if(p1.getGroupId().equalsIgnoreCase(p.getGroupId()) && p1.getArtifactId().equalsIgnoreCase(p.getArtifactId()) && p1.getVersion().equalsIgnoreCase(p.getVersion()))
					{
						pomFind = true;
						break;
					}
				}
				if(!pomFind)
				{
					pomRecommendations.add(p);
				}
			}
		}
		
		System.out.println("############################### pom recommendations ##################################");
		for(DependencyDTO d : pomRecommendations)
		{
			System.out.println(d.getGroupId());
			System.out.println(d.getArtifactId());
			System.out.println(d.getVersion());
		}
		System.out.println("############################# pom recommendations end ##################################");
		
		//combinations
		List<POMDependencyDTO> tempPomDependencies = new ArrayList<>();
		
		Map<String, Integer> leastVulCountCombinationMap = POMOptimization.optimizePOMRecommendations(allDependencies, pomRecommendations, Constants.newTempPomFileLocation, Constants.newTempPomLocation, Constants.dependencyCheckReportLocation);
		
		System.out.println(leastVulCountCombinationMap);
		
//		// get direct dependencies from original pom
//		List<POMDependencyDTO> pomDependencies = new ArrayList<>();
//		pomDependencies = PomOperations.getDirectDepndenciesFromOriginalPOM();
//		
//		//parsing of all the dependencies
//		for(POMDependencyDTO pomDependency : pomDependencies)
//		{
//			//get recommendations for the pomDependency
//			List<DependencyDTO> pomRecommendations = POMRecommendation.getRecommendationsForPOMDependency(pomDependency);
//			
//			for(DependencyDTO d : pomRecommendations)
//			{
//				System.out.println("############################# POm Recommendations ############################");
//				System.out.println(d.getArtifactId());
//				System.out.println(d.getGroupId());
//				System.out.println(d.getVersion());
//			}
//			
//			//combinations
//			List<POMDependencyDTO> tempPomDependencies = new ArrayList<>();
//			tempPomDependencies.add(pomDependency);
//			Map<String, Integer> leastVulCountCombinationMap = POMOptimization.optimizePOMRecommendations(pomDependency, pomRecommendations);
//			
//			FinalChanges.putAll(leastVulCountCombinationMap);
//		}
//
//		List<POMDependencyDTO> directDependencies = PomOperations.getDirectDepndenciesFromOriginalPOM();
//		
//		List<POMDependencyDTO> recommendationDependencies = new ArrayList<>();
//		
//		String key = "";
//		int leastVulCount = 0;
//		for (Map.Entry<String, Integer> entry : FinalChanges.entrySet()) {
////		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().size());
//		    key = entry.getKey();
//		    leastVulCount = entry.getValue();
//		}
//		
//		System.out.println();
//		System.out.println("################################ Final Changes #############################");
//
//		String[] arr = key.split(",");
//		for(String a : arr)
//		{
//			System.out.println();
//			String[] brr = a.split("#");
//			System.out.println(brr[0]);
//			System.out.println(brr[1]);
//			System.out.println(brr[2]);
//			recommendationDependencies.add(new POMDependencyDTO(brr[0],brr[1],brr[2]));
//		}
//		
//		System.out.println("############################################################################");
//		System.out.println();
//
//		
//		// comparison beetween the old and new pom
//		
//		Reporting.comparisonCheck(directDependencies, recommendationDependencies);
//		
//		Reporting.createReport(directDependencies, recommendationDependencies);
//		
////		System.out.println("######################################### Vulnerability count #########################################");
////		System.out.println(leastVulCount);
//		
//		// final combinations
////		POMOptimization.optimizePOMRecommendations(pomDependencies, pomRecommendations);
//		
//		//
//		
//		
//		//
	}

}