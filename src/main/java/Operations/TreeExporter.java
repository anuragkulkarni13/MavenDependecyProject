package Operations;

import com.fasterxml.jackson.databind.ObjectMapper;

import DTO.DependencyDTO;

import java.io.File;
import java.io.IOException;

public class TreeExporter {

    public static void exportTreeToJson(DependencyDTO root, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            objectMapper.writeValue(new File(filePath), root);
            System.out.println("Tree exported successfully to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

