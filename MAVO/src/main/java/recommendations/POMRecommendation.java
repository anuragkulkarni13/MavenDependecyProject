package recommendations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import common.Constants;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import dependency.DependencyOperations;
import pomanalyzer.PomOperations;
import vulnerability.VulnerabilityAnalyzer;

public class POMRecommendation {

	public static List<DependencyDTO> getRecommendationsForPOMDependency(POMDependencyDTO pomDependency)
	{
		System.out.println("inside getRecommendationsForPOMDependency");
		
		List<DependencyDTO> pomRecommendations = new ArrayList<DependencyDTO>();

		try {

			Connection connection = DriverManager.getConnection(Constants.DB_URL_VUL);

			PomOperations.clearAllDependencies(Constants.newTempPomFileLocation);

			PomOperations.addDependency(Constants.newTempPomFileLocation, pomDependency.getGroupId(), pomDependency.getArtifactId(), pomDependency.getVersion());

			DependencyOperations.generateDependencyTreeWithPath(Constants.newTempPomFileLocation, Constants.newTempPomLocation, Constants.dependencyTreeLocation);
			
			DependencyDTO root = DependencyOperations.getDependencyTree(Constants.dependencyTreeLocation);
		
			for(DependencyDTO child : root.getChildren())
			{
				List<String> depList = new ArrayList<>();

				Queue<DependencyDTO> depQueue = new LinkedList<>();
				depList.add(child.getArtifactId());
				depQueue.offer(child);
				System.out.println("child artifact id - "+child.getArtifactId());
				while(!depQueue.isEmpty())
				{
					DependencyDTO node = depQueue.poll();
//					System.out.println("parent "+node.getArtifactId());
					PomOperations.updateDependency(Constants.newTempPomFileLocation, node.getGroupId(), node.getArtifactId(), node.getVersion());
					
					String leastVulCountVersion = VulnerabilityAnalyzer.getleastVulCountVersion(Constants.newTempPomFileLocation, Constants.newTempPomLocation, Constants.dependencyTreeLocation, Constants.dependencyCheckReportLocation, node.getGroupId(), node.getArtifactId(), node.getVersion(), Constants.filePath, connection);
					if(!leastVulCountVersion.equals(node.getVersion()))
					{
						System.out.println("#################################################### add dep ############################################# "+node.getArtifactId()+" - "+node.getGroupId()+" - "+leastVulCountVersion);
						node.setVersion(leastVulCountVersion);
						pomRecommendations.add(node);
					}
					
					PomOperations.updateDependencyVersion(Constants.newTempPomFileLocation, node.getGroupId(),node.getArtifactId(),leastVulCountVersion);
					DependencyOperations.generateDependencyTreeWithPath(Constants.newTempPomFileLocation, Constants.newTempPomLocation, Constants.dependencyTreeLocation);

					node = DependencyOperations.getDependencyTree(Constants.dependencyTreeLocation);
					for(DependencyDTO nodeChild : node.getChildren().get(0).getChildren())
					{
						if(!depList.contains(nodeChild.getArtifactId()))
						{
							depList.add(nodeChild.getArtifactId());
							depQueue.offer(nodeChild);
//							System.out.println("child - "+nodeChild.getArtifactId());
						}
					}
				}
			}
			connection.close();

		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return pomRecommendations;
	}
}
