package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;

import cache.DependencyCache;
import common.Constants;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import common.dto.VersionDTO;
import pom.PomOperations;
import pom.TempPomCreator;
import recommendations.POMOptimization;
import recommendations.POMRecommendation;
import versionmanagement.VersionFetcher;

public class main3 {

	
	public static void main(String[] args) {
		
//		Constants.excludeList.put("org.yaml", "snakeyaml");
//		Constants.excludeList.put("org.springframework.boot", "spring-boot");
		
		// cache setup
		DependencyCache.createCache();
		
		boolean excludeParentDependencies = true;
		parseModule(Constants.originalPomLocation);
		
		
		vulnerabilityCheckModule(Constants.originalPomLocation, excludeParentDependencies);


		System.out.println("########################## dependencyMap ########################");
		for (Map.Entry<String, List<String>> entry : Constants.dependencyMap.entrySet()) {
		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}
		
		System.out.println("########################## keyModule ########################");
		for (Map.Entry<String, String> entry : Constants.keyModule.entrySet()) {
		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}

		
		updateDependencyMap(Constants.dependencyMap, Constants.keyModule);
		
		getDepMgmtRecommendations(Constants.originalPomLocation);
		
		Map<String, List<POMDependencyDTO>> finalChanges = getGroupRecommendations(Constants.dependencyMap, Constants.keyModule);

		System.out.println("########################## finalDepMgmtChanges ########################");
		for (Map.Entry<String, List<POMDependencyDTO>> entry : Constants.finalDepMgmtChanges.entrySet()) {
		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		    System.out.println(entry.getKey());
		    for(POMDependencyDTO dependency : entry.getValue())
		    {
		    	System.out.println(dependency.getGroupId());
		    	System.out.println(dependency.getArtifactId());
		    	System.out.println(dependency.getVersion());
		    }
		}
		System.out.println("########################## finalDepMgmtChanges end ########################");
		
		System.out.println("########################## finalChanges ########################");
		for (Map.Entry<String, List<POMDependencyDTO>> entry : finalChanges.entrySet()) {
		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		    System.out.println(entry.getKey());
		    for(POMDependencyDTO dependency : entry.getValue())
		    {
		    	System.out.println(dependency.getGroupId());
		    	System.out.println(dependency.getArtifactId());
		    	System.out.println(dependency.getVersion());
		    }
		}
		System.out.println("########################## finalChanges end ########################");

		
		

	}
	
	public static void parseModule(String pomPath)
	{
		System.out.println("Module - "+pomPath);
		
		// temp pom creation and setup
		TempPomCreator.createAndSetupTempPOM(pomPath);
		
		// get properties
		String newPomFilePath = pomPath+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		
		Map<String, String> propertiesMap = PomOperations.getPomProperties(newPomFilePath);
		Constants.globalpropertiesMap.putAll(propertiesMap);

		List<List<POMDependencyDTO>> dependencies = PomOperations.getSegregatedDependencies(newPomFilePath, true);
		
		Constants.parentDependencies.addAll(dependencies.get(0));
		List<POMDependencyDTO> externalDependencies = dependencies.get(1);
		
		
		System.out.println("parent dependencies");
		for(POMDependencyDTO p : Constants.parentDependencies)
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
		
		String[] arr = pomPath.split("\\\\");
		String moduleName = arr[arr.length-1];
		PomOperations.addToDependencyMap(externalDependencies, moduleName);
		
		List<String> modules = PomOperations.getPomModules(pomPath+Constants.pomFileName);
		
		for(String module : modules)
		{
//			System.out.println(module);
			parseModule(pomPath+"\\"+module);
		}
	}
	
	public static void vulnerabilityCheckModule(String pomPath, boolean excludeParentDependencies)
	{
		System.out.println("Module - "+pomPath);
		
		// get properties
		String newPomFilePath = pomPath+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		
		List<List<POMDependencyDTO>> dependencies = PomOperations.getSegregatedDependencies(newPomFilePath, false);
		
		List<POMDependencyDTO> externalDependencies = dependencies.get(1);
		System.out.println("ext dep count : "+externalDependencies.size());
		
		List<POMDependencyDTO> allDependencies = new ArrayList<>();

		if(excludeParentDependencies)
		{
			for(POMDependencyDTO dependency : externalDependencies)
			{
				boolean depInParent = false;
				for(POMDependencyDTO parDep : Constants.parentDependencies)
				{
					if(parDep.getGroupId().equals(dependency.getGroupId()) && parDep.getVersion().equals(dependency.getVersion()))
					{
						depInParent = true;
						break;
					}
				}
				if(!depInParent)
				{
					allDependencies.add(dependency);
				}
			}
		}
		else
		{
			allDependencies.addAll(externalDependencies);			
		}
		
		System.out.println(allDependencies.size());
		
		
		List<DependencyDTO> pomRecommendations = new ArrayList<>();
		for(POMDependencyDTO pomDependency : allDependencies)
		{
			//get recommendations for the pomDependency
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
		System.out.println("############################### depMgmt pom recommendations ##################################");
		for(DependencyDTO d : pomRecommendations)
		{
			System.out.println(d.getGroupId());
			System.out.println(d.getArtifactId());
			System.out.println(d.getVersion());
		}
		System.out.println("############################# depMgmt pom recommendations end ##################################");
		System.out.println();
		
		
		// group change of version
		
		
		
		String tempPomFileDirectory = Constants.originalPomLocation+Constants.tempPomFileDirectoryName;
		String tempPomFilePath = Constants.originalPomLocation+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		String tempPomDependencyCheckReportLocation = Constants.originalPomLocation+Constants.tempPomFileDirectoryName+Constants.dependencyCheckReportName;
		
		Map<String, Integer> leastVulCountCombinationMap = new HashMap<>();
		if(pomRecommendations.size()>0)
		{
			leastVulCountCombinationMap = POMOptimization.optimizePOMRecommendations(externalDependencies, pomRecommendations, tempPomFilePath, tempPomFileDirectory, tempPomDependencyCheckReportLocation);
			System.out.println(leastVulCountCombinationMap);
		}
		
		
		for(String key : leastVulCountCombinationMap.keySet())
		{
			String[] arr = pomPath.split("\\\\");
			System.out.println(key+" : "+arr[arr.length-1]);
			Constants.keyModule.put(key, arr[arr.length-1]);
		}
		
		List<String> modules = PomOperations.getPomModules(pomPath+Constants.pomFileName);
		
		for(String module : modules)
		{
//			System.out.println(module);
			vulnerabilityCheckModule(pomPath+"\\"+module, excludeParentDependencies);
		}
	}
	
	public static void updateDependencyMap(Map<String, List<String>> dependencyMap, Map<String, String> keyModule)
	{
		Map<String, String> changes = new HashMap<>();
		
		//update dependency Map
		for (Map.Entry<String, String> keymod : Constants.keyModule.entrySet()) {
		    System.out.println("Key: " + keymod.getKey() + ", Value: " + keymod.getValue());
		    String key = keymod.getKey();
		    String value = keymod.getValue();
		    String[] dependencies = key.split(",");
		    for(String dependency : dependencies)
		    {
		    	String[] arr = dependency.split("#");
		    	String keyGroupId = arr[0];
		    	String keyArtifactId = arr[1];
		    	String keyVersion = arr[2];
		    	
				for (Map.Entry<String, List<String>> depMap : Constants.dependencyMap.entrySet()) {
					String mergedGIDVersion = depMap.getKey();
					String[] brr = mergedGIDVersion.split("_");
					String depMapGroupID = brr[0];
					String depMapVersion = brr[1];
					
					if(keyGroupId.equals(depMapGroupID))
					{
						long depMapTimeStamp = VersionFetcher.getTimeStampforDependency(depMapGroupID, keyArtifactId, depMapVersion);
						long keyTimeStamp = VersionFetcher.getTimeStampforDependency(keyGroupId, keyArtifactId, keyVersion);
						
						if(depMapTimeStamp<keyTimeStamp)
						{
							String newMergedGIDVersion = depMapGroupID+"_"+keyVersion;
							changes.put(mergedGIDVersion, newMergedGIDVersion);
						}
					}
				}
		    }
		}
		
		for (Map.Entry<String, String> change : changes.entrySet())
		{
			String oldkey = change.getKey();
			String newkey = change.getValue();
			
	        if (dependencyMap.containsKey(oldkey)) {
	            // Get the value associated with the old key
	        	List<String> artifactIds = dependencyMap.get(oldkey);
	
	            // Remove the old key-value pair
	        	dependencyMap.remove(oldkey);
	
	            // Add the new key-value pair
	        	dependencyMap.put(newkey, artifactIds);
	        }
			
		}

		System.out.println("########################## updated dependencyMap ########################");
		for (Map.Entry<String, List<String>> entry : Constants.dependencyMap.entrySet()) {
		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}
	}
	
	public static void getDepMgmtRecommendations(String pomPath)
	{
		System.out.println("Module - "+pomPath);
		String[] pomPathArr = pomPath.split("\\\\");
		String pomModule = pomPathArr[pomPathArr.length-1];
		
		List<Dependency> dependencies = PomOperations.getDepMgmtDepndenciesFromPOM(pomPath+Constants.pomFileName);
		
		for(Dependency dependency : dependencies)
		{
			String depGroupId = dependency.getGroupId();
			String depArtifactId = dependency.getArtifactId();
			String depversion = dependency.getVersion();
			if (depversion != null && depversion.startsWith("${") && depversion.endsWith("}")) {
                String propertyName = depversion.substring(2, depversion.length() - 1);
                depversion = Constants.globalpropertiesMap.get(propertyName);
			}
			
			boolean depFound = false;
			for (Map.Entry<String, List<String>> depMap : Constants.dependencyMap.entrySet()) {
				String mergedGIDVersion = depMap.getKey();
				String[] brr = mergedGIDVersion.split("_");
				String depMapGroupID = brr[0];
				String depMapVersion = brr[1];
				
				if(depGroupId.equals(depMapGroupID))
				{
					depFound = true;
					POMDependencyDTO newDep = new POMDependencyDTO(depGroupId, depArtifactId, depMapVersion);
					List<POMDependencyDTO> depList = new ArrayList<>();
					if(Constants.finalDepMgmtChanges.containsKey(pomModule))
					{
						depList = Constants.finalDepMgmtChanges.get(pomModule);
					}
					depList.add(newDep);
					Constants.finalDepMgmtChanges.put(pomModule, depList);
					break;
				}
			}
			if(depFound == false)
			{
				POMDependencyDTO newDep = new POMDependencyDTO(depGroupId, depArtifactId, depversion);
				List<POMDependencyDTO> depList = new ArrayList<>();
				if(Constants.finalDepMgmtChanges.containsKey(pomModule))
				{
					depList = Constants.finalDepMgmtChanges.get(pomModule);
				}
				depList.add(newDep);
				Constants.finalDepMgmtChanges.put(pomModule, depList);
			}
		}
		
		List<String> modules = PomOperations.getPomModules(pomPath+Constants.pomFileName);
		
		for(String module : modules)
		{
//			System.out.println(module);
			getDepMgmtRecommendations(pomPath+"\\"+module);
		}
	}
	
	public static Map<String, List<POMDependencyDTO>> getGroupRecommendations(Map<String, List<String>> dependencyMap, Map<String, String> keyModule)
	{
		Map<String, List<POMDependencyDTO>> finalChanges = new HashMap<>();
		for (Map.Entry<String, String> keymod : Constants.keyModule.entrySet()) {
		    System.out.println("Key: " + keymod.getKey() + ", Value: " + keymod.getValue());
		    
		    String key = keymod.getKey();
		    String module = keymod.getValue();
		    String[] dependencies = key.split(",");
		    for(String dependency : dependencies)
		    {
		    	String[] arr = dependency.split("#");
		    	String keyGroupId = arr[0];
		    	String keyArtifactId = arr[1];
		    	String keyVersion = arr[2];
		    	
		    	boolean depFound = false;
				for (Map.Entry<String, List<String>> depMap : Constants.dependencyMap.entrySet()) {
					String mergedGIDVersion = depMap.getKey();
					String[] brr = mergedGIDVersion.split("_");
					String depMapGroupID = brr[0];
					String depMapVersion = brr[1];
					
					if(keyGroupId.equals(depMapGroupID))
					{
						depFound = true;
						POMDependencyDTO newDep = new POMDependencyDTO(keyGroupId, keyArtifactId, depMapVersion);
						List<POMDependencyDTO> depList = new ArrayList<>();
						if(finalChanges.containsKey(module))
						{
							depList = finalChanges.get(module);
						}
						depList.add(newDep);
						finalChanges.put(module, depList);
					}
				}
				if(depFound == false)
				{
					POMDependencyDTO newDep = new POMDependencyDTO(keyGroupId, keyArtifactId, keyVersion);
					List<POMDependencyDTO> depList = new ArrayList<>();
					if(finalChanges.containsKey(module))
					{
						depList = finalChanges.get(module);
					}
					depList.add(newDep);
					finalChanges.put(module, depList);
				}
		    }
		}
		return finalChanges;
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
