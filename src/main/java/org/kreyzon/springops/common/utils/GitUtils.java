package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for Git operations using JGit.
 * Provides methods to check branch existence and list remote branches excluding "deploy*" ones.
 * Authentication is done via personal access token over HTTPS.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@UtilityClass
public class GitUtils {

    /**
     * Checks if a branch exists in the given remote repository URL.
     *
     * @param remoteUrl  the Git repository URL (HTTPS only)
     * @param branchName the branch name to check (e.g. "develop")
     * @param gitToken   personal access token for authentication
     * @return true if the branch exists remotely
     * @throws GitAPIException if an error occurs while querying the remote repository
     */
    public boolean branchExists(String remoteUrl, String branchName, String gitToken)
            throws GitAPIException {

        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("git", gitToken);
        System.setProperty("org.eclipse.jgit.transport.http.wire", "true");

        LsRemoteCommand cmd = Git.lsRemoteRepository()
                .setHeads(true)
                .setTags(false)
                .setRemote(remoteUrl)
                .setCredentialsProvider(credentialsProvider);

        Collection<Ref> refs = cmd.call();
        return refs.stream()
                .anyMatch(ref -> ref.getName().endsWith("/" + branchName));
    }

    /**
     * Retrieves remote branch names excluding those starting with "deploy",
     * using authentication and redirect tracing.
     *
     * @param remoteUrl Git repo URL (HTTPS)
     * @param gitToken Personal access token
     */
    public List<String> getBranchesExcludingDeploy(String remoteUrl, String gitToken)
            throws GitAPIException {
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("git", gitToken);

        LsRemoteCommand cmd = Git.lsRemoteRepository()
                .setHeads(true)
                .setTags(false)
                .setRemote(remoteUrl)
                .setCredentialsProvider(credentialsProvider);

        System.setProperty("org.eclipse.jgit.transport.http.wire", "true");

        return cmd.call().stream()
                .map(ref -> ref.getName().substring("refs/heads/".length()))
                .filter(name -> !name.startsWith("deploy"))
                .collect(Collectors.toList());
    }

}
