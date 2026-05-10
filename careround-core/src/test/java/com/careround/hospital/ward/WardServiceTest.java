package com.careround.hospital.ward;

import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.ward.dto.CreateWardRequest;
import com.careround.hospital.ward.dto.UpdateWardRequest;
import com.careround.hospital.ward.dto.WardResponse;
import com.careround.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WardServiceTest {

    @Mock private WardRepository wardRepository;
    @InjectMocks private WardServiceImpl wardService;

    private Ward ward;

    @BeforeEach
    void setUp() {
        ward = new Ward();
        ward.setId("ward-1");
        ward.setHospitalId("hosp-1");
        ward.setName("ICU");
        ward.setTotalBeds(10);
    }

    @Test
    void create_happyPath_shouldSaveAndReturn() {
        when(wardRepository.save(any())).thenAnswer(i -> {
            Ward w = i.getArgument(0);
            w.setId("ward-new");
            return w;
        });

        WardResponse result = wardService.create("hosp-1",
                new CreateWardRequest("ICU", "Critical Care", 10, null));

        assertThat(result.name()).isEqualTo("ICU");
        assertThat(result.totalBeds()).isEqualTo(10);
    }

    @Test
    void getById_withValidIds_shouldReturnWard() {
        when(wardRepository.findByIdAndHospitalId("ward-1", "hosp-1"))
                .thenReturn(Optional.of(ward));

        WardResponse result = wardService.getById("hosp-1", "ward-1");

        assertThat(result.id()).isEqualTo("ward-1");
        assertThat(result.name()).isEqualTo("ICU");
    }

    @Test
    void getById_withUnknownId_shouldThrowResourceNotFoundException() {
        when(wardRepository.findByIdAndHospitalId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wardService.getById("hosp-1", "bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_crossTenantAccess_shouldThrowResourceNotFoundException() {
        when(wardRepository.findByIdAndHospitalId("ward-1", "other-hosp"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> wardService.getById("other-hosp", "ward-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByHospital_shouldReturnAll() {
        when(wardRepository.findAllByHospitalId("hosp-1")).thenReturn(List.of(ward));

        List<WardResponse> results = wardService.listByHospital("hosp-1");

        assertThat(results).hasSize(1);
    }

    @Test
    void update_withNullFields_shouldOnlyUpdateProvidedFields() {
        when(wardRepository.findByIdAndHospitalId("ward-1", "hosp-1"))
                .thenReturn(Optional.of(ward));

        WardResponse result = wardService.update("hosp-1", "ward-1",
                new UpdateWardRequest("HDU", null, null, null));

        assertThat(result.name()).isEqualTo("HDU");
        assertThat(result.totalBeds()).isEqualTo(10);
    }

    @Test
    void delete_withValidIds_shouldDelete() {
        when(wardRepository.findByIdAndHospitalId("ward-1", "hosp-1"))
                .thenReturn(Optional.of(ward));

        wardService.delete("hosp-1", "ward-1");

        verify(wardRepository).delete(ward);
    }
}
