package Operations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import Constants.Constants;
import DTO.DependencyDTO;
import DTO.VulnerabilityDTO;

public class DependencyOperations {
	
	public static DependencyDTO getDependencyTree()
	{
		ObjectMapper objectMapper = new ObjectMapper();
		DependencyDTO rootDependency = new DependencyDTO();
        try {
            // Read JSON file and convert it to a List of User objects
            rootDependency = objectMapper.readValue(new File(Constants.dependencyTreeLocation), DependencyDTO.class);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootDependency;
	}
	
	public static Map<String, List<VulnerabilityDTO>> getVulnerabilityList()
	{
		
        Map<String, List<VulnerabilityDTO>> vulnerabilitiesMap = new HashMap<>();

        
		try {
	        String content = new String(Files.readAllBytes(Paths.get(Constants.dependencyCheckReportLocation)));
	        JSONObject jsonObject = new JSONObject(content);
            // Check if the "dependencies" array exists and is not null
            if (jsonObject.has("dependencies") && !jsonObject.isNull("dependencies")) {
                // Ensure that "dependencies" is actually a JSONArray
                if (jsonObject.get("dependencies") instanceof JSONArray) {
        	        JSONArray dependencyArray = jsonObject.getJSONArray("dependencies");
        	        for (int i = 0; i < dependencyArray.length(); i++) {
        	        	
        	        	String fileName = "";
        	        	List<VulnerabilityDTO> vulnerabilityList = new ArrayList<>();

                        JSONObject dependencyObject = dependencyArray.getJSONObject(i);
                        fileName = dependencyObject.getString("fileName");
//                        System.out.println("filename : "+fileName);
                        if (dependencyObject.has("vulnerabilities") && !dependencyObject.isNull("vulnerabilities")) {
                            // Ensure that "dependencies" is actually a JSONArray
                            if (dependencyObject.get("vulnerabilities") instanceof JSONArray) {
                    	        JSONArray vulnerabilityArray = dependencyObject.getJSONArray("vulnerabilities");
                    	        for (int j = 0; j < vulnerabilityArray.length(); j++) {
                    	        	
                                    JSONObject vulnerabilityObject = vulnerabilityArray.getJSONObject(j);
                    	        	String source = "";
                    	            String vulnerabilityName = "";
                    	            String severity = "";
                    	            float baseScore = 0;
                    	            String attackVector = "";
                    	            String attackComplexity = "";
                    	            String privilegesRequired = "";
                    	            String userInteraction = "";
                    	            String scope = "";
                    	            String confidentialityImpact = "";
                    	            String integrityImpact = "";
                    	            String availabilityImpact = "";
                    	            String baseSeverity = "";
                    	            String exploitabilityScore = "";
                    	            String impactScore = "";
                    	            String version = "";
                    	            
                    	            source = vulnerabilityObject.getString("source");
                                    vulnerabilityName = vulnerabilityObject.getString("name");
                                    severity = vulnerabilityObject.getString("severity");
                                    if(vulnerabilityObject.has("cvssv3"))
                                    {
                                        JSONObject cvssv3Object = vulnerabilityObject.getJSONObject("cvssv3");
                                        baseScore = cvssv3Object.has("score")?cvssv3Object.getFloat("baseScore"):0;
                                        attackVector = cvssv3Object.has("score")?cvssv3Object.getString("attackVector"):"";
                                        attackComplexity = cvssv3Object.has("score")?cvssv3Object.getString("attackComplexity"):"";
                                        privilegesRequired = cvssv3Object.has("score")?cvssv3Object.getString("privilegesRequired"):"";
                                        userInteraction = cvssv3Object.has("score")?cvssv3Object.getString("userInteraction"):"";
                                        scope = cvssv3Object.has("score")?cvssv3Object.getString("scope"):"";
                                        confidentialityImpact = cvssv3Object.has("score")?cvssv3Object.getString("confidentialityImpact"):"";
                                        integrityImpact = cvssv3Object.has("score")?cvssv3Object.getString("integrityImpact"):"";
                                        availabilityImpact = cvssv3Object.has("score")?cvssv3Object.getString("availabilityImpact"):"";
                                        baseSeverity = cvssv3Object.has("score")?cvssv3Object.getString("baseSeverity"):"";
                                        exploitabilityScore = cvssv3Object.has("score")?cvssv3Object.getString("exploitabilityScore"):"";
                                        impactScore = cvssv3Object.has("score")?cvssv3Object.getString("impactScore"):"";
                                        version = cvssv3Object.has("score")?cvssv3Object.getString("version"):"";
                                    }
                                    else if(vulnerabilityObject.has("cvssv2"))
                                    {
                                        JSONObject cvssv2Object = vulnerabilityObject.getJSONObject("cvssv2");
                                        baseScore = cvssv2Object.has("score")?cvssv2Object.getFloat("score"):0;
                                        attackVector = cvssv2Object.has("accessVector")?cvssv2Object.getString("accessVector"):"";
                                        attackComplexity = cvssv2Object.has("accessComplexity")?cvssv2Object.getString("accessComplexity"):"";
                                        privilegesRequired = cvssv2Object.has("authenticationr")?cvssv2Object.getString("authenticationr"):"";
                                        confidentialityImpact = cvssv2Object.has("confidentialImpact")?cvssv2Object.getString("confidentialImpact"):"";
                                        integrityImpact = cvssv2Object.has("integrityImpact")?cvssv2Object.getString("integrityImpact"):"";
                                        availabilityImpact = cvssv2Object.has("availabilityImpact")?cvssv2Object.getString("availabilityImpact"):"";
                                        baseSeverity = cvssv2Object.has("severity")?cvssv2Object.getString("severity"):"";
                                        exploitabilityScore = cvssv2Object.has("exploitabilityScore")?cvssv2Object.getString("exploitabilityScore"):"";
                                        impactScore = cvssv2Object.has("impactScore")?cvssv2Object.getString("impactScore"):"";
                                        version = cvssv2Object.has("version")?cvssv2Object.getString("version"):"";
                                    }
                                    
                            		VulnerabilityDTO vulnerabilityDTO = new VulnerabilityDTO();
                            		vulnerabilityDTO.setJarName(fileName);
                            		vulnerabilityDTO.setSource(source);
                            		vulnerabilityDTO.setVulnerabilityName(vulnerabilityName);
                            		vulnerabilityDTO.setSeverity(severity);
                            		vulnerabilityDTO.setBaseScore(baseScore);
                            		vulnerabilityDTO.setAttackVector(attackVector);
                            		vulnerabilityDTO.setAttackComplexity(attackComplexity);
                            		vulnerabilityDTO.setPrivilegesRequired(privilegesRequired);
                            		vulnerabilityDTO.setUserInteraction(userInteraction);
                            		vulnerabilityDTO.setScope(scope);
                            		vulnerabilityDTO.setConfidentialityImpact(confidentialityImpact);
                            		vulnerabilityDTO.setIntegrityImpact(integrityImpact);
                            		vulnerabilityDTO.setAvailabilityImpact(availabilityImpact);
                            		vulnerabilityDTO.setBaseSeverity(baseSeverity);
                            		vulnerabilityDTO.setExploitabilityScore(exploitabilityScore);
                            		vulnerabilityDTO.setImpactScore(impactScore);
                            		vulnerabilityDTO.setVersion(version);
                            		
//                            		System.out.println(vulnerabilityDTO.getJarName());
                            		vulnerabilityList.add(vulnerabilityDTO);
                    	        }
                            }
                        }
                        if(vulnerabilityList.size()>0)
                        {
                            vulnerabilitiesMap.put(fileName, vulnerabilityList);
                        }
        	        }
                }
            }	        
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
        
		return vulnerabilitiesMap;

	}
	
	public static void traverseDependencies(DependencyDTO root) {
        if (root == null) return;

        // Stack to hold nodes to visit
        Stack<DependencyDTO> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            // Pop the top item from the stack
            DependencyDTO current = stack.pop();

            // Process the current node (you can replace this with any operation you need)
//            if(current.getVulnerabilities().size()>0)
//            {
                System.out.println(current.getArtifactId());
                System.out.println(current.getVulnerabilities().size());            	
//            }

            // Push all children of the current node onto the stack
            if (current.getChildren() != null) {
                for (DependencyDTO child : current.getChildren()) {
                    stack.push(child);
                }
            }
        }
	}
	
	
	public static void traverseDFS(DependencyDTO root)
	{
		System.out.println(root.getArtifactId());
		List<DependencyDTO> children = root.getChildren();
		for(DependencyDTO child : children)
		{
			traverseDFS(child);
		}
	}
}
