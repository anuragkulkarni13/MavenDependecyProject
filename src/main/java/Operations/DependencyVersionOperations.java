package Operations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

public class DependencyVersionOperations {
	
	public static List<String> getDirectDependencyVersionList(String groupId, String artifactId)
	{
		List<String> versionList = new ArrayList<String>();

//		System.out.println(groupId);
		String groupId_new = groupId.replace(".", "/");
//		System.out.println(groupId_new);

		String MAVEN_METADATA_URL = 
				"https://repo.maven.apache.org/maven2/"+groupId_new+"/"+artifactId+"/maven-metadata.xml";
		
//		System.out.println(MAVEN_METADATA_URL);

		versionList = versionsList(MAVEN_METADATA_URL);
		return versionList;
	}
	
	public static List<String> versionsList(String MAVEN_METADATA_URL)
	{
		List<String> versionList = new ArrayList<String>();

        try {
    		String metadata = "";
    		HttpClient client = HttpClient.newHttpClient();
    		HttpRequest request = HttpRequest.newBuilder()
    				.uri(URI.create(MAVEN_METADATA_URL))
    				.GET()
    				.build();

    		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    		if (response.statusCode() == 200) {
    			versionList = parseAndPrintVersions(response.body());
    			metadata = response.body();
    		} else {
    			throw new IOException("Failed to fetch metadata: " + response.statusCode());
    		}
//    		System.out.println(metadata);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return versionList;
	}
	
	public static List<String> parseAndPrintVersions(String xmlResponse) {
        
		List<String> versionList = new ArrayList<String>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML content
            Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

            // Normalize the XML structure; it's recommended
            document.getDocumentElement().normalize();

            // Get all the <version> elements
            NodeList versionNodes = document.getElementsByTagName("version");

            // Loop over the versions and print them
            for (int i = 0; i < versionNodes.getLength(); i++) {
                String version = versionNodes.item(i).getTextContent();
//                System.out.println("Version: " + version);
                versionList.add(version);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return versionList;
    }
	
	

}
