package org.kreyzon.springops.wiki.service;


import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for managing wiki files.
 * This service provides methods to retrieve the names of wiki files stored in the classpath.
 *
 * @author Lorenzo Orlando
 */
@Service
public class WikiService {

    /**
     * Retrieves the names of all wiki files located in the classpath under the "wiki" directory.
     *
     * @return a list of wiki file names
     * @throws IOException if an I/O error occurs while accessing the files
     */
    public List<String> getWikiFileNames() throws IOException {
        ClassPathResource resource = new ClassPathResource("wiki");
        try (Stream<Path> paths = Files.walk(resource.getFile().toPath(), 1)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Retrieves the content of a specific wiki file by its filename.
     *
     * @param filename the name of the wiki file to retrieve
     * @return the content of the wiki file as a String
     * @throws IOException if an I/O error occurs while accessing the file
     */
    public String getWikiFileContent(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("wiki/" + filename);
        if (!resource.exists()) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        return Files.readString(resource.getFile().toPath());
    }

}

