package br.com.jucelio.sentinelfraud.api;

import br.com.jucelio.sentinelfraud.service.DeadLetterReplayService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/dead-letters")
public class DeadLetterAdminController {
    private final DeadLetterReplayService service;

    public DeadLetterAdminController(DeadLetterReplayService service) {
        this.service = service;
    }

    @GetMapping
    public List<DeadLetterResponse> list() {
        return service.list();
    }

    @PostMapping("/{id}/replay")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DeadLetterResponse replay(@PathVariable UUID id) {
        return service.replay(id);
    }
}
