package br.fiap.arena.api;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.fiap.arena.domain.Task;
import br.fiap.arena.domain.TaskStatus;
import br.fiap.arena.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Task task) {
        Task saved = service.create(task);
        return ResponseEntity
            .created(URI.create("/api/tasks/" + saved.getId()))
            .body(saved);
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(name = "status", required = false) TaskStatus status,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        List<Task> data = status == null ? service.listAll() : service.listByStatus(status);
            int from = page * size;
            int to = Math.min(from + size, data.size());
            if (from >= data.size()) {
                from = 0;
                to = Math.min(size, data.size());
            }
        List<Task> slice = data.subList(from, to);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("content", slice);
        resp.put("totalElements", data.size());
        resp.put("page", page);
        resp.put("size", size);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        boolean removed = service.delete(id);
        return removed ? ResponseEntity.noContent().build() 
                     : ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(service.stats());
    }
}
