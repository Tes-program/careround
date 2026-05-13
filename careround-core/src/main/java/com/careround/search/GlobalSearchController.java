package com.careround.search;

import com.careround.search.dto.GlobalSearchResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Tenant-scoped global search across operational resources")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    @Operation(summary = "Global search", description = "Returns grouped patient, ward, staff, team, task, and round search results for the current hospital.")
    public ResponseEntity<ApiResponse<GlobalSearchResponse>> search(
            @Parameter(description = "Search query. Minimum useful length is 2 characters.")
            @RequestParam("q") String query) {
        return ResponseEntity.ok(ApiResponse.ok(globalSearchService.search(HospitalContextHolder.getHospitalId(), query)));
    }
}
