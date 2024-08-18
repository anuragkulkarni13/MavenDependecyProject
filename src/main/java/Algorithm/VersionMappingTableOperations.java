package Algorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;

import Constants.Constants;
import DTO.DependencyDTO;

public class VersionMappingTableOperations {

	public static String createTempPOMForDirectDependencies(DependencyDTO dependency)
	{
		String originalPomPath = Constants.pomLocation+"\\pom.xml"; // Replace with the actual path
        String newPomFileDirectory = Constants.pomLocation+"\\tempPOMForDirectDependencies"; // Replace with the new location and name
        String newPomPath = Constants.pomLocation+"\\tempPOMForDirectDependencies\\"+dependency.getArtifactId()+"-pom.xml"; // Replace with the new location and name

		try {
			
			// Step 1: Copy the POM file to a new location and rename it
			Files.createDirectories(Paths.get(newPomFileDirectory));
            Files.copy(Paths.get(originalPomPath), Paths.get(newPomPath));

            // Step 2: Parse the new POM file=
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(newPomPath));
            
            // Step 3: Remove all dependencies and plugins
//            removeElementsByTagName(document, "dependency", newPomPath);
//            removeElementsByTagName(document, "plugin", newPomPath);
			removeElementsByTagName(document, "dependencies");
            
            
            // Step 4: Add the required dependency
            Element dependencyNode = document.createElement("dependency");

            Element groupId = document.createElement("groupId");
            groupId.appendChild(document.createTextNode(dependency.getGroupId()));
            dependencyNode.appendChild(groupId);

            Element artifactId = document.createElement("artifactId");
            artifactId.appendChild(document.createTextNode(dependency.getArtifactId()));
            dependencyNode.appendChild(artifactId);

            Element version = document.createElement("version");
            version.appendChild(document.createTextNode(dependency.getVersion()));
            dependencyNode.appendChild(version);

            Node dependenciesNode = document.createElement("dependencies");
            dependenciesNode.appendChild(dependencyNode);
            document.getDocumentElement().appendChild(dependenciesNode);
            
            // Step 5: Save the changes to the new POM file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(newPomPath));
            transformer.transform(source, result);

            System.out.println("POM file modified and saved successfully.");
		} catch (Exception e) {
            e.printStackTrace();
        }
		
		return newPomPath;
	}
	
    private static void removeElementsByTagName(Document document, String tagName) {
        NodeList elements = document.getElementsByTagName(tagName);
        for (int i = elements.getLength() - 1; i >= 0; i--) {
            Node node = elements.item(i);
            node.getParentNode().removeChild(node);
        }
    }
    
    
    
    
    
    // parsing all the poms with all the versions
    public static List<String> getAllVersionsAfterCurrentVersion(DependencyDTO dependency, String currentVersion, List<String> versionList)
    {
    	int index = getCurrentVersionIndex(currentVersion, versionList);
    	for(int i=index;i<versionList.size();i++)
    	{
    		System.out.println(versionList.get(i));
    	}
    	return versionList.subList(index, versionList.size());
    }
    
    public static int getCurrentVersionIndex(String currentVersion, List<String> versionList)
    {
    	for(int i=0;i<versionList.size();i++)
    	{
    		if(currentVersion.equals(versionList.get(i)))
    		{
    			return i;
    		}
    	}
    	return -1;
    }
    
	public static void updateDependencyVersion(String pomFilePath, String groupId, String artifactId, String newVersion) {
        try {
            // Parse the pom.xml file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(pomFilePath));

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            // Get the list of dependencies
            NodeList dependencies = doc.getElementsByTagName("dependency");

            // Iterate through the dependencies to find the correct one
            for (int i = 0; i < dependencies.getLength(); i++) {
                Node dependency = dependencies.item(i);

                if (dependency.getNodeType() == Node.ELEMENT_NODE) {
                    Element dependencyElement = (Element) dependency;

                    String currentGroupId = dependencyElement.getElementsByTagName("groupId").item(0).getTextContent();
                    String currentArtifactId = dependencyElement.getElementsByTagName("artifactId").item(0).getTextContent();

                    if (currentGroupId.equals(groupId) && currentArtifactId.equals(artifactId)) {
                        // Found the dependency, now update the version
                        Node versionNode = dependencyElement.getElementsByTagName("version").item(0);
                        if (versionNode != null) {
                            versionNode.setTextContent(newVersion);
                            System.out.println("Updated version to " + newVersion);
                        } else {
                            // If no version tag exists, add it
                            Element versionElement = doc.createElement("version");
                            versionElement.appendChild(doc.createTextNode(newVersion));
                            dependencyElement.appendChild(versionElement);
//                            System.out.println("Added version " + newVersion);
                        }
                        break; // Dependency found and updated; exit loop
                    }
                }
            }

            // Write the updated document back to the file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(pomFilePath));
//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

            System.out.println("POM file updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static DependencyDTO getUpdatedDependencyTree(String newChildPomPath)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		DependencyDTO rootDependency = new DependencyDTO();
        try {
            // Read JSON file and convert it to a List of User objects
            rootDependency = objectMapper.readValue(new File(newChildPomPath), DependencyDTO.class);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootDependency;
	}
	
    public static void traverseAndRecordAllDependencyVersions(DependencyDTO root, Map<String, String> treeVersionHashMap)
    {
		String recordName = root.getGroupId()+":"+root.getArtifactId();
		String recordValue = root.getVersion();
		treeVersionHashMap.put(recordName, recordValue);
		List<DependencyDTO> children = root.getChildren();
		for(DependencyDTO child : children)
		{
			traverseAndRecordAllDependencyVersions(child, treeVersionHashMap);
		}
    }
}
