package org.kreyzon.springops.core.git;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kreyzon.springops.common.utils.GitUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Git operations.
 * Provides endpoints to interact with Git repositories, such as retrieving available branches.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RequestMapping("/git")
@RestController
@RequiredArgsConstructor
public class GitController {

    private final ApplicationConfig applicationConfig;

    /**
     * Endpoint to retrieve a list of available branches in a Git repository,
     * excluding branches that start with "deploy".
     *
     * @param gitUrl the Git repository URL (HTTP(s) or SSH)
     * @return ResponseEntity containing a list of branch names
     * @throws GitAPIException if an error occurs while querying the remote repository
     */
    @GetMapping("/available-branches")
    private ResponseEntity<List<String>> getAvailableBranches(@RequestParam String gitUrl) throws GitAPIException {
        List<String> availableBranches = GitUtils.getBranchesExcludingDeploy(gitUrl, applicationConfig.getGitToken());
        return ResponseEntity.ok(availableBranches);
    }
}
