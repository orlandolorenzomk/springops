package org.kreyzon.springops.wiki.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.wiki.service.WikiService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * Controller for managing wiki files.
 * This controller provides an endpoint to retrieve the names of wiki files stored in the classpath.
 *
 * @author Lorenzo Orlando
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/wiki")
public class WikiController {

    private final WikiService wikiService;

    /**
     * Endpoint to retrieve the names of all wiki files.
     *
     * @return a list of wiki file names
     * @throws IOException if an I/O error occurs while accessing the files
     */
    @GetMapping("/files")
    public List<String> getWikiFiles() throws IOException {
        return wikiService.getWikiFileNames();
    }

    /**
     * Endpoint to retrieve the content of a specific wiki file by its name.
     *
     * @param name the name of the wiki file to retrieve
     * @return the content of the wiki file as a String
     */
    @GetMapping("/file")
    public ResponseEntity<String> getWikiFile(@RequestParam String name) {
        try {
            String content = wikiService.getWikiFileContent(name);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }

}

