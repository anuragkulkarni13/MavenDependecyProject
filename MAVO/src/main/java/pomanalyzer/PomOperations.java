package pomanalyzer;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import common.Constants;
import common.dto.POMDependencyDTO;

public class PomOperations {

	//handle pom parsing operations
	
	public static List<POMDependencyDTO> getDirectDepndenciesFromOriginalPOM()
	{
		List<POMDependencyDTO> pomDependencies = new ArrayList<>();

		try
		{
			FileReader reader = new FileReader(Constants.originalPomFileLocation);

			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(reader);

			Parent parent = model.getParent();
			System.out.println("Parent Version: " + parent);

			List<Dependency> dependencies = model.getDependencies();

			for (Dependency dependency : dependencies){
				if(dependency.getVersion() == null){
					pomDependencies.add(new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), parent.getVersion()));					
				}else{
					pomDependencies.add(new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()));
				}
			}
		}catch (Exception e) {
			System.out.println(e);
		}
		return pomDependencies;
	}

	public static void clearAllDependencies(String pomFilePath){
		
		try
		{
			File pomFile = new File(pomFilePath);

			// Parse the existing pom.xml
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(pomFile);
			doc.getDocumentElement().normalize();

			// Get the <dependencies> block
			NodeList dependenciesList = doc.getElementsByTagName("dependencies");

			if (dependenciesList.getLength() > 0) {
				System.out.println(dependenciesList.getLength());
				Node dependenciesNode = dependenciesList.item(0);

				// Remove all <dependency> elements under <dependencies>
				NodeList dependencyElements = dependenciesNode.getChildNodes();
				while (dependencyElements.getLength() > 0) {
					dependenciesNode.removeChild(dependencyElements.item(0));
				}

//				System.out.println("All dependencies have been cleared.");
			} else {
//				System.out.println("No <dependencies> block found.");
			}

			// Save the updated document back to the pom.xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(pomFile);
			transformer.transform(source, result);
		}catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void addDependency(String pomFilePath, String groupId, String artifactId, String version){

		try {
			File pomFile = new File(pomFilePath);

			// Parse the existing pom.xml
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(pomFile);
			doc.getDocumentElement().normalize();

			// Check if the dependency already exists with the same groupId and artifactId
			NodeList dependenciesList = doc.getElementsByTagName("dependency");
			boolean dependencyExists = false;

			for (int i = 0; i < dependenciesList.getLength(); i++) {
				Element dependency = (Element) dependenciesList.item(i);

				String currentGroupId = dependency.getElementsByTagName("groupId").item(0).getTextContent();
				String currentArtifactId = dependency.getElementsByTagName("artifactId").item(0).getTextContent();

				// If the groupId and artifactId match, check version
				if (currentGroupId.equals(groupId) && currentArtifactId.equals(artifactId)) {
					String currentVersion = dependency.getElementsByTagName("version").item(0).getTextContent();

					if (!currentVersion.equals(version)) {
						// If the version is different, update the version
						dependency.getElementsByTagName("version").item(0).setTextContent(version);
//						System.out.println("Updated dependency version from " + currentVersion + " to " + version);
					} else {
//						System.out.println("Dependency already exists with the same version.");
					}

					// Dependency with same groupId and artifactId found, no need to add a duplicate
					dependencyExists = true;
					break;
				}
			}

			// If no matching dependency is found, add a new one
			if (!dependencyExists) {
				// Find or create the <dependencies> element
				NodeList dependenciesListRoot = doc.getElementsByTagName("dependencies");
				Element dependencies;
				if (dependenciesListRoot.getLength() > 0) {
					dependencies = (Element) dependenciesListRoot.item(0);
				} else {
					// Create <dependencies> block if it doesn't exist
					dependencies = doc.createElement("dependencies");
					doc.getDocumentElement().appendChild(dependencies);
				}

				// Create the new dependency element
				Element newDependency = doc.createElement("dependency");

				Element groupIdElem = doc.createElement("groupId");
				groupIdElem.appendChild(doc.createTextNode(groupId));
				newDependency.appendChild(groupIdElem);

				Element artifactIdElem = doc.createElement("artifactId");
				artifactIdElem.appendChild(doc.createTextNode(artifactId));
				newDependency.appendChild(artifactIdElem);

				Element versionElem = doc.createElement("version");
				versionElem.appendChild(doc.createTextNode(version));
				newDependency.appendChild(versionElem);

				// Add the new dependency
				dependencies.appendChild(newDependency);
//				System.out.println("Dependency added successfully to pom.xml.");
			}

			TempPomCreator.removeWhitespaceNodes(doc.getDocumentElement());

			// Save the updated document back to the pom.xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(pomFile);
			transformer.transform(source, result);
		}catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void updateDependency(String pomFileLocation, String groupId, String artifactId, String version) {
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(pomFileLocation));

			// Normalize the XML structure
			doc.getDocumentElement().normalize();

			Node dependency = doc.getElementsByTagName("dependency").item(0);
			if (dependency.getNodeType() == Node.ELEMENT_NODE) {
				Element dependencyElement = (Element) dependency;

				Node groupIdNode = dependencyElement.getElementsByTagName("groupId").item(0);
				Node artifactIdNode = dependencyElement.getElementsByTagName("artifactId").item(0);
				Node versionNode = dependencyElement.getElementsByTagName("version").item(0);

				groupIdNode.setTextContent(groupId);
				artifactIdNode.setTextContent(artifactId);
				versionNode.setTextContent(version);
			}

			// Write the updated document back to the file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(pomFileLocation));
			//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
//			System.out.println("POM file updated successfully to - "+groupId+" - "+artifactId+" - "+version);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateDependencyVersion(String pomFileLocation, String groupId, String artifactId, String newVersion) {
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(pomFileLocation));

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
							//                            System.out.println("Updated version to " + newVersion);
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
			StreamResult result = new StreamResult(new File(pomFileLocation));
			//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);

			System.out.println("POM file updated successfully to version - "+newVersion);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
