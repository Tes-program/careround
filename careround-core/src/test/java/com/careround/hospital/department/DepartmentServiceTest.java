package com.careround.hospital.department;

import com.careround.hospital.department.dto.CreateDepartmentRequest;
import com.careround.hospital.department.dto.DepartmentResponse;
import com.careround.hospital.department.dto.UpdateDepartmentRequest;
import com.careround.hospital.entity.Department;
import com.careround.hospital.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @InjectMocks private DepartmentServiceImpl departmentService;

    private Department dept;

    @BeforeEach
    void setUp() {
        dept = new Department();
        dept.setId("dept-1");
        dept.setHospitalId("hosp-1");
        dept.setName("Cardiology");
    }

    @Test
    void create_happyPath_shouldSaveAndReturn() {
        when(departmentRepository.save(any())).thenAnswer(i -> {
            Department d = i.getArgument(0);
            d.setId("dept-new");
            return d;
        });

        DepartmentResponse result = departmentService.create("hosp-1",
                new CreateDepartmentRequest("Cardiology", null));

        assertThat(result.name()).isEqualTo("Cardiology");
        assertThat(result.hospitalId()).isEqualTo("hosp-1");
    }

    @Test
    void getById_withValidIds_shouldReturnDepartment() {
        when(departmentRepository.findByIdAndHospitalId("dept-1", "hosp-1"))
                .thenReturn(Optional.of(dept));

        DepartmentResponse result = departmentService.getById("hosp-1", "dept-1");

        assertThat(result.id()).isEqualTo("dept-1");
        assertThat(result.name()).isEqualTo("Cardiology");
    }

    @Test
    void getById_withUnknownId_shouldThrowResourceNotFoundException() {
        when(departmentRepository.findByIdAndHospitalId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById("hosp-1", "bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_crossTenantAccess_shouldThrowResourceNotFoundException() {
        when(departmentRepository.findByIdAndHospitalId("dept-1", "other-hosp"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById("other-hosp", "dept-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByHospital_shouldReturnAllDepartments() {
        when(departmentRepository.findAllByHospitalId("hosp-1")).thenReturn(List.of(dept));

        List<DepartmentResponse> results = departmentService.listByHospital("hosp-1");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Cardiology");
    }

    @Test
    void update_withValidIds_shouldUpdateFields() {
        when(departmentRepository.findByIdAndHospitalId("dept-1", "hosp-1"))
                .thenReturn(Optional.of(dept));

        DepartmentResponse result = departmentService.update("hosp-1", "dept-1",
                new UpdateDepartmentRequest("Neurology", null));

        assertThat(result.name()).isEqualTo("Neurology");
    }

    @Test
    void delete_withUnknownId_shouldThrowResourceNotFoundException() {
        when(departmentRepository.findByIdAndHospitalId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.delete("hosp-1", "bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_withValidIds_shouldCallRepositoryDelete() {
        when(departmentRepository.findByIdAndHospitalId("dept-1", "hosp-1"))
                .thenReturn(Optional.of(dept));

        departmentService.delete("hosp-1", "dept-1");

        verify(departmentRepository).delete(dept);
    }
}
