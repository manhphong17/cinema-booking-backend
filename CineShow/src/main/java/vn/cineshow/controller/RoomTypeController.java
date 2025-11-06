package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.room.RoomTypeCreateRequest;
import vn.cineshow.dto.request.room.RoomTypeUpdateRequest;
import vn.cineshow.service.RoomTypeService;

import java.net.URI;
import java.util.EnumSet;

@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService service;

    @GetMapping
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.findAll(onlyActive));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<?> create(@Valid @RequestBody RoomTypeCreateRequest req) {
        var dto = service.create(req);
        return ResponseEntity.created(URI.create("/api/room-types/" + dto.getId())).body(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody RoomTypeUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody RoomTypeUpdateRequest req) {
        return ResponseEntity.ok(service.patch(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<?> activate(@PathVariable Long id) { return ResponseEntity.ok(service.activate(id)); }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) { return ResponseEntity.ok(service.deactivate(id)); }


}
