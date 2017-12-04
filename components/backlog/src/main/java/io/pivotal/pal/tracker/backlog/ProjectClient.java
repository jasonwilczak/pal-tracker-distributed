package io.pivotal.pal.tracker.backlog;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.web.client.RestOperations;

import java.util.concurrent.ConcurrentHashMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String endpoint;
    private ConcurrentHashMap<Long,ProjectInfo> _projectInfoCache;

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations = restOperations;
        this.endpoint = registrationServerEndpoint;
        this._projectInfoCache = new ConcurrentHashMap<>();
    }
    @HystrixCommand(fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo project = restOperations.getForObject(endpoint + "/projects/" + projectId, ProjectInfo.class);
        updateProjectToCache(projectId,project);
        return project;
    }

    public ProjectInfo getProjectFromCache(long projectId) {
        return _projectInfoCache.getOrDefault(projectId,null);
    }

    private void updateProjectToCache(long projectId,ProjectInfo projectInfo) {
        if(!_projectInfoCache.containsKey(projectId))
        {
            _projectInfoCache.put(projectId,projectInfo);
        } else {
            _projectInfoCache.replace(projectId,projectInfo);
        }
    }
}
