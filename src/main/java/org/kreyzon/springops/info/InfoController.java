package org.kreyzon.springops.info;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.kreyzon.springops.common.dto.info.InfoDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.FileReader;

/**
 * Controller to provide application information such as group ID, artifact ID, and version.
 * This information is read from the `pom.xml` file of the application.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestController
@RequestMapping("/info")
@Slf4j
public class InfoController {

    /**
     * Retrieves application information from the `pom.xml` file.
     *
     * @return InfoDto containing group ID, artifact ID, and version of the application.
     */
    @GetMapping
    public InfoDto getInfo() {
        try (FileReader reader = new FileReader("pom.xml")) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Model model = mavenReader.read(reader);
            return new InfoDto(
                    model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId(),
                    model.getArtifactId(),
                    model.getVersion() != null ? model.getVersion() : model.getParent().getVersion()
            );
        } catch (Exception e) {
            log.error("Failed to read pom.xml", e);
            throw new RuntimeException("Could not read pom.xml");
        }
    }
}
