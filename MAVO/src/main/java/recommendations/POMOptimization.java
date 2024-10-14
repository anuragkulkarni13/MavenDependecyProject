package recommendations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cache.DependencyCache;
import common.Constants;
import common.Utils;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import common.dto.VulnerabilityDTO;
import dependency.DependencyOperations;
import pomanalyzer.PomOperations;
import vulnerability.VulnerabilityAnalyzer;

public class POMOptimization {
	
	public static Map<String, Integer> optimizePOMRecommendations(POMDependencyDTO pomDependency, List<DependencyDTO> pomRecommendations)
	{
		Map<String, Integer> leastVulCountCombinationMap = new HashMap<>();
		int leastVulCount = 0;
		String leastVulCountCombination = "";
		boolean first = false;
		String key = "";
		try {
			Connection connection = DriverManager.getConnection(Constants.DB_URL_COMB);
			List<List<Integer>> comb = Utils.combinations(pomRecommendations.size());
			
	        for(List<Integer> co : comb)
	        {
	        	key = "";
	            // Clear all dependencies first
	        	PomOperations.clearAllDependencies(Constants.newTempPomFileLocation);

	        	PomOperations.addDependency(Constants.newTempPomFileLocation, pomDependency.getGroupId(), pomDependency.getArtifactId(), pomDependency.getVersion());
	            key += pomDependency.getGroupId()+"#"+pomDependency.getArtifactId()+"#"+pomDependency.getVersion();
	        	for(Integer c : co)
	        	{	            	
	            	DependencyDTO p = pomRecommendations.get(c);
	            	
	            	String groupId = p.getGroupId();
	            	String artifactId = p.getArtifactId();
	            	String version = p.getVersion();
	            	
	            	System.out.println(c+" - "+groupId+" - "+artifactId+" - "+version);
	                PomOperations.addDependency(Constants.newTempPomFileLocation, groupId, artifactId, version);

	                if(!key.contains(groupId+"#"+artifactId+"#"+version))
	                {
		                key += ","+groupId+"#"+artifactId+"#"+version;	                	
	                }
	        	}
	        	
	        	int vulCount = 0;
	        	if(DependencyCache.isKeyPresent(connection, key))
	        	{
	        		vulCount = Integer.parseInt(DependencyCache.getCache(connection, key));
	        	}else {
	        		DependencyOperations.generateDependencyCheckReportWithPath(Constants.newTempPomFileLocation, Constants.newTempPomLocation, Constants.dependencyCheckReportLocation);
					Map<String, List<VulnerabilityDTO>> vulnerabilityMap = VulnerabilityAnalyzer.getVulnerabilityList(Constants.dependencyCheckReportLocation);
					
					for (Map.Entry<String, List<VulnerabilityDTO>> entry : vulnerabilityMap.entrySet()) {
//					    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().size());
					    vulCount += entry.getValue().size();
					}
					System.out.println(vulCount);
					DependencyCache.putCache(connection, key, String.valueOf(vulCount));
	        	}
				
	        	if(first == false)
	        	{
	        		leastVulCount = vulCount;
	        		leastVulCountCombination = key;
	        		first = true;
	        	}
	        	else
	        	{
	        		if(vulCount<leastVulCount)
	        		{
	        			leastVulCount = vulCount;
	        			leastVulCountCombination = key;
	        		}
	        	}
			}
	        
	        leastVulCountCombinationMap.put(leastVulCountCombination, leastVulCount);
		}catch (Exception e) {
			System.out.println(e);
		}
		return leastVulCountCombinationMap;
	}
}
