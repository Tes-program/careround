package com.careround.patient.prescription;

import com.careround.patient.prescription.converter.LocalDateTimeListConverter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeListConverterTest {

    private final LocalDateTimeListConverter converter = new LocalDateTimeListConverter();

    @Test
    void convertToDatabaseColumn_nullList_returnsEmptyJsonArray() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("[]");
    }

    @Test
    void convertToDatabaseColumn_emptyList_returnsEmptyJsonArray() {
        assertThat(converter.convertToDatabaseColumn(List.of())).isEqualTo("[]");
    }

    @Test
    void convertToDatabaseColumn_withValues_returnsValidJsonString() {
        LocalDateTime t1 = LocalDateTime.of(2025, 1, 15, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 1, 15, 14, 0);
        LocalDateTime t3 = LocalDateTime.of(2025, 1, 15, 20, 0);

        String result = converter.convertToDatabaseColumn(List.of(t1, t2, t3));

        assertThat(result).isNotBlank();
        assertThat(result).startsWith("[");
        assertThat(result).endsWith("]");
    }

    @Test
    void convertToEntityAttribute_nullString_returnsEmptyList() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
    }

    @Test
    void convertToEntityAttribute_emptyJsonArray_returnsEmptyList() {
        assertThat(converter.convertToEntityAttribute("[]")).isEmpty();
    }

    @Test
    void convertToEntityAttribute_validJson_returnsCorrectList() {
        LocalDateTime t1 = LocalDateTime.of(2025, 1, 15, 8, 0);
        String json = converter.convertToDatabaseColumn(List.of(t1));

        List<LocalDateTime> result = converter.convertToEntityAttribute(json);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(t1);
    }

    @Test
    void roundTrip_convertThenBack_producesOriginalList() {
        LocalDateTime t1 = LocalDateTime.of(2025, 3, 10, 6, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 3, 10, 12, 0);
        LocalDateTime t3 = LocalDateTime.of(2025, 3, 10, 18, 0);
        List<LocalDateTime> original = List.of(t1, t2, t3);

        String db = converter.convertToDatabaseColumn(original);
        List<LocalDateTime> restored = converter.convertToEntityAttribute(db);

        assertThat(restored).containsExactlyElementsOf(original);
    }
}
