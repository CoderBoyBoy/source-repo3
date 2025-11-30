package com.gitserver.git;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.util.FS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Controller for Git Smart HTTP protocol support.
 * Implements Git upload-pack (fetch/clone) and receive-pack (push) operations.
 */
@Slf4j
@RestController
@RequestMapping("/git")
@RequiredArgsConstructor
public class GitHttpController {

    @Value("${git.repositories.base-path:./repositories}")
    private String repositoriesBasePath;

    private static final String UPLOAD_PACK_SERVICE = "git-upload-pack";
    private static final String RECEIVE_PACK_SERVICE = "git-receive-pack";
    private static final String UPLOAD_PACK_RESULT = "application/x-git-upload-pack-result";
    private static final String RECEIVE_PACK_RESULT = "application/x-git-receive-pack-result";
    private static final String UPLOAD_PACK_REQUEST = "application/x-git-upload-pack-request";
    private static final String RECEIVE_PACK_REQUEST = "application/x-git-receive-pack-request";
    private static final String UPLOAD_PACK_ADVERTISEMENT = "application/x-git-upload-pack-advertisement";
    private static final String RECEIVE_PACK_ADVERTISEMENT = "application/x-git-receive-pack-advertisement";

    /**
     * Handle Git info/refs request (used by git clone, fetch, push for capability advertisement).
     */
    @GetMapping("/{owner}/{repo}.git/info/refs")
    public void handleInfoRefs(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam("service") String service,
            HttpServletResponse response) throws IOException {
        
        log.debug("Info refs request for {}/{}, service: {}", owner, repo, service);
        
        try (Repository repository = openRepository(owner, repo)) {
            if (service.equals(UPLOAD_PACK_SERVICE)) {
                response.setContentType(UPLOAD_PACK_ADVERTISEMENT);
                response.setStatus(HttpStatus.OK.value());
                advertiseUploadPack(repository, response.getOutputStream());
            } else if (service.equals(RECEIVE_PACK_SERVICE)) {
                response.setContentType(RECEIVE_PACK_ADVERTISEMENT);
                response.setStatus(HttpStatus.OK.value());
                advertiseReceivePack(repository, response.getOutputStream());
            } else {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Unsupported service: " + service);
            }
        } catch (Exception e) {
            log.error("Error handling info/refs for {}/{}", owner, repo, e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    /**
     * Handle Git upload-pack request (used by git clone/fetch).
     */
    @PostMapping(value = "/{owner}/{repo}.git/git-upload-pack", 
                 consumes = UPLOAD_PACK_REQUEST,
                 produces = UPLOAD_PACK_RESULT)
    public void handleUploadPack(
            @PathVariable String owner,
            @PathVariable String repo,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.debug("Upload pack request for {}/{}", owner, repo);
        
        try (Repository repository = openRepository(owner, repo)) {
            response.setContentType(UPLOAD_PACK_RESULT);
            response.setStatus(HttpStatus.OK.value());
            
            UploadPack uploadPack = new UploadPack(repository);
            uploadPack.setTimeout(60);
            uploadPack.setBiDirectionalPipe(false);
            
            uploadPack.upload(request.getInputStream(), response.getOutputStream(), null);
            
            log.debug("Upload pack completed for {}/{}", owner, repo);
        } catch (Exception e) {
            log.error("Error in upload pack for {}/{}", owner, repo, e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    /**
     * Handle Git receive-pack request (used by git push).
     */
    @PostMapping(value = "/{owner}/{repo}.git/git-receive-pack",
                 consumes = RECEIVE_PACK_REQUEST,
                 produces = RECEIVE_PACK_RESULT)
    public void handleReceivePack(
            @PathVariable String owner,
            @PathVariable String repo,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.debug("Receive pack request for {}/{}", owner, repo);
        
        try (Repository repository = openRepository(owner, repo)) {
            response.setContentType(RECEIVE_PACK_RESULT);
            response.setStatus(HttpStatus.OK.value());
            
            ReceivePack receivePack = new ReceivePack(repository);
            receivePack.setTimeout(60);
            receivePack.setBiDirectionalPipe(false);
            receivePack.setAllowNonFastForwards(true);
            receivePack.setAllowDeletes(true);
            
            receivePack.receive(request.getInputStream(), response.getOutputStream(), null);
            
            log.debug("Receive pack completed for {}/{}", owner, repo);
        } catch (Exception e) {
            log.error("Error in receive pack for {}/{}", owner, repo, e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    /**
     * Advertise upload-pack capabilities and refs.
     */
    private void advertiseUploadPack(Repository repository, OutputStream output) throws IOException {
        PacketLineOut pckOut = new PacketLineOut(output);
        
        // Write service announcement
        pckOut.writeString("# service=" + UPLOAD_PACK_SERVICE + "\n");
        pckOut.end();
        
        // Create upload pack and advertise refs
        UploadPack uploadPack = new UploadPack(repository);
        uploadPack.setTimeout(60);
        uploadPack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(pckOut));
    }

    /**
     * Advertise receive-pack capabilities and refs.
     */
    private void advertiseReceivePack(Repository repository, OutputStream output) throws IOException {
        PacketLineOut pckOut = new PacketLineOut(output);
        
        // Write service announcement
        pckOut.writeString("# service=" + RECEIVE_PACK_SERVICE + "\n");
        pckOut.end();
        
        // Create receive pack and advertise refs
        ReceivePack receivePack = new ReceivePack(repository);
        receivePack.setTimeout(60);
        receivePack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(pckOut));
    }

    /**
     * Open a Git repository.
     */
    private Repository openRepository(String owner, String name) throws IOException {
        File repoDir = new File(repositoriesBasePath, owner + "/" + name);
        File gitDir = new File(repoDir, ".git");
        
        if (!repoDir.exists()) {
            throw new IOException("Repository not found: " + owner + "/" + name);
        }
        
        return RepositoryCache.open(
            RepositoryCache.FileKey.exact(gitDir.exists() ? gitDir : repoDir, FS.DETECTED),
            true
        );
    }
}
