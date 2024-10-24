package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.DependencyManagement;

import cache.DependencyCache;
import common.Constants;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import common.dto.VersionDTO;
import pom.PomOperations;
import pom.TempPomCreator;
import recommendations.POMRecommendation;
import versionmanagement.VersionFetcher;

public class main2 {
	
	public static void main(String[] args) {
		
//		Constants.excludeList.put("org.yaml", "snakeyaml");
//		Constants.excludeList.put("org.springframework.boot", "spring-boot");
		
		// cache setup
		DependencyCache.createCache();
		
		boolean excludeParentDependencies = false;
		parseModule(Constants.originalPomLocation, excludeParentDependencies);
		
//		System.out.println("############### global properties map #####################");
//		for (Map.Entry<String, String> entry : Constants.globalpropertiesMap.entrySet()) {
//		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
//		}
//		System.out.println("############### global properties map #####################");

	}

	public static void parseModule(String pomPath, boolean excludeParentDependencies)
	{
		
		System.out.println("Module - "+pomPath);
		
		// temp pom creation and setup
		TempPomCreator.createAndSetupTempPOM(pomPath);
		
		// get properties
		String newPomFilePath = pomPath+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		
		Map<String, String> propertiesMap = PomOperations.getPomProperties(newPomFilePath);
		Constants.globalpropertiesMap.putAll(propertiesMap);
		
		List<List<POMDependencyDTO>> depMgmtDependencies = PomOperations.getParentDirectAndIndirectDepMgmtDependencies(newPomFilePath);
		
		List<POMDependencyDTO> depMgmtParentDependencies = depMgmtDependencies.get(0);
		List<POMDependencyDTO> depMgmtDirectDependencies = depMgmtDependencies.get(1);
		List<POMDependencyDTO> depMgmtExternalDependencies = depMgmtDependencies.get(2);
		
		//get recommendations for the pom
		List<List<POMDependencyDTO>> dependencies = PomOperations.getParentDirectAndIndirectDependencies(newPomFilePath);
		
		List<POMDependencyDTO> parentDependencies = dependencies.get(0);
		List<POMDependencyDTO> directDependencies = dependencies.get(1);
		List<POMDependencyDTO> externalDependencies = dependencies.get(2);
		
//		System.out.println(directDependencies);
//		System.out.println(externalDependencies);
		
		upgradeParentToLatestMajorPatch(depMgmtParentDependencies, newPomFilePath);
		upgradeParentToLatestMajorPatch(parentDependencies, newPomFilePath);
		
		// loop over all the remaining dep mgmt dependencies
		
		// for depm mgmt
		List<POMDependencyDTO> allDepMgmtDependencies = new ArrayList<>();
		
		if(!excludeParentDependencies)
		{
			allDepMgmtDependencies.addAll(depMgmtDirectDependencies);
			allDepMgmtDependencies.addAll(depMgmtExternalDependencies);			
		}
		else
		{
			allDepMgmtDependencies.addAll(depMgmtExternalDependencies);			
		}
		
		List<DependencyDTO> depMgmtPomRecommendations = new ArrayList<>();
		for(POMDependencyDTO pomDependency : allDepMgmtDependencies)
		{
			//get recommendations for the pomDependency
//			pomRecommendations.addAll(POMRecommendation.getRecommendationsForPOMDependency(pomDependency, parentVersion));
			List<DependencyDTO> pomDependencyRecommendations = POMRecommendation.getRecommendationsForPOMDependency(pomDependency, Constants.originalPomLocation);
			for(DependencyDTO p : pomDependencyRecommendations)
			{
				boolean pomFind = false;
				for(DependencyDTO p1 : depMgmtPomRecommendations)
				{
					if(p1.getGroupId().equalsIgnoreCase(p.getGroupId()) && p1.getArtifactId().equalsIgnoreCase(p.getArtifactId()) && p1.getVersion().equalsIgnoreCase(p.getVersion()))
					{
						pomFind = true;
						break;
					}
				}
				if(!pomFind)
				{
					depMgmtPomRecommendations.add(p);
				}
			}
		}
		
		System.out.println();
		System.out.println("############################### depMgmt pom recommendations ##################################");
		for(DependencyDTO d : depMgmtPomRecommendations)
		{
			System.out.println(d.getGroupId());
			System.out.println(d.getArtifactId());
			System.out.println(d.getVersion());
		}
		System.out.println("############################# depMgmt pom recommendations end ##################################");
		System.out.println();


		
		List<POMDependencyDTO> allDependencies = new ArrayList<>();
		
		if(!excludeParentDependencies)
		{
			allDependencies.addAll(directDependencies);
			allDependencies.addAll(externalDependencies);
		}
		else
		{
			allDependencies.addAll(externalDependencies);
		}
		
		// for depm mgmt

		List<DependencyDTO> pomRecommendations = new ArrayList<>();
		for(POMDependencyDTO pomDependency : allDependencies)
		{
			//get recommendations for the pomDependency
//			pomRecommendations.addAll(POMRecommendation.getRecommendationsForPOMDependency(pomDependency, parentVersion));
			List<DependencyDTO> pomDependencyRecommendations = POMRecommendation.getRecommendationsForPOMDependency(pomDependency, Constants.originalPomLocation);
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
		
		System.out.println();
		System.out.println("############################### pom recommendations ##################################");
		for(DependencyDTO d : pomRecommendations)
		{
			System.out.println(d.getGroupId());
			System.out.println(d.getArtifactId());
			System.out.println(d.getVersion());
		}
		System.out.println("############################# pom recommendations end ##################################");
		System.out.println();
		
		List<String> modules = PomOperations.getPomModules(pomPath+Constants.pomFileName);
		
		for(String module : modules)
		{
//			System.out.println(module);
			parseModule(pomPath+"\\"+module, excludeParentDependencies);
		}
	}
	
	public static void upgradeParentToLatestMajorPatch(List<POMDependencyDTO> parentDependencies, String newPomFilePath)
	{
		// loop over all the parent dependencies
		for(POMDependencyDTO depMgmtParentDependency : parentDependencies)
		{
			List<VersionDTO> versionList = VersionFetcher.fetchAllVersions(depMgmtParentDependency.getGroupId(), depMgmtParentDependency.getArtifactId(), depMgmtParentDependency.getVersion(), true);
			if(versionList.size()>0)
			{
				String updatedVerion = versionList.get(0).getVersion();
				if(!updatedVerion.equalsIgnoreCase(depMgmtParentDependency.getVersion()))
				{
					//update the properties
					PomOperations.updatePomProperties(newPomFilePath, depMgmtParentDependency.getGroupId(), depMgmtParentDependency.getArtifactId(), updatedVerion);
					System.out.println();
					System.out.println("#################### parent upgrade #######################");
					System.out.println(depMgmtParentDependency.getGroupId());
					System.out.println(depMgmtParentDependency.getArtifactId());
					System.out.println(updatedVerion);
					System.out.println("#################### parent upgrade #######################");
					System.out.println();
				}
			}
		}
	}
	
}
