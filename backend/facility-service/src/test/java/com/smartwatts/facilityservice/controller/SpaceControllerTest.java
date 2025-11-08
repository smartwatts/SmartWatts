package com.smartwatts.facilityservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.facilityservice.model.Space;
import com.smartwatts.facilityservice.model.SpaceStatus;
import com.smartwatts.facilityservice.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpaceController.class)
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpaceService spaceService;

    @Autowired
    private ObjectMapper objectMapper;

    private Space testSpace;
    private Long testSpaceId;

    @BeforeEach
    void setUp() {
        testSpaceId = 1L;
        
        testSpace = new Space();
        testSpace.setId(testSpaceId);
        testSpace.setSpaceCode("SPACE-001");
        testSpace.setName("Test Space");
        // Space model doesn't have status field - removing this line
    }

    @Test
    void createSpace_Success_ReturnsCreated() throws Exception {
        // Given
        when(spaceService.createSpace(any(Space.class))).thenReturn(testSpace);

        // When & Then
        mockMvc.perform(post("/api/v1/spaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSpace)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Space"));

        verify(spaceService).createSpace(any(Space.class));
    }

    @Test
    void getSpaceById_Success_ReturnsSpace() throws Exception {
        // Given
        when(spaceService.getSpaceById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // When & Then
        mockMvc.perform(get("/api/v1/spaces/{id}", testSpaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSpaceId))
                .andExpect(jsonPath("$.name").value("Test Space"));

        verify(spaceService).getSpaceById(testSpaceId);
    }

    @Test
    void getSpaceById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(spaceService.getSpaceById(testSpaceId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/spaces/{id}", testSpaceId))
                .andExpect(status().isNotFound());

        verify(spaceService).getSpaceById(testSpaceId);
    }

    @Test
    void getSpaceByCode_Success_ReturnsSpace() throws Exception {
        // Given
        when(spaceService.getSpaceByCode("SPACE-001")).thenReturn(Optional.of(testSpace));

        // When & Then
        mockMvc.perform(get("/api/v1/spaces/code/{spaceCode}", "SPACE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spaceCode").value("SPACE-001"));

        verify(spaceService).getSpaceByCode("SPACE-001");
    }

    @Test
    void getAllSpaces_Success_ReturnsPage() throws Exception {
        // Given
        Page<Space> page = new PageImpl<>(Arrays.asList(testSpace));
        when(spaceService.getSpaces(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/spaces")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Space"));

        verify(spaceService).getSpaces(any(Pageable.class));
    }

    @Test
    void getAllSpacesList_Success_ReturnsList() throws Exception {
        // Given
        List<Space> spaces = Arrays.asList(testSpace);
        when(spaceService.getAllSpaces()).thenReturn(spaces);

        // When & Then
        mockMvc.perform(get("/api/v1/spaces/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Space"));

        verify(spaceService).getAllSpaces();
    }

    @Test
    void getSpacesByStatus_Success_ReturnsList() throws Exception {
        // Given
        List<Space> spaces = Arrays.asList(testSpace);
        when(spaceService.getSpacesByStatus(any(SpaceStatus.class))).thenReturn(spaces);

        // When & Then
        mockMvc.perform(get("/api/v1/spaces/status/{status}", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(spaceService).getSpacesByStatus(SpaceStatus.AVAILABLE);
    }

    @Test
    void updateSpace_Success_ReturnsUpdatedSpace() throws Exception {
        // Given
        testSpace.setName("Updated Space");
        when(spaceService.updateSpace(eq(testSpaceId), any(Space.class))).thenReturn(testSpace);

        // When & Then
        mockMvc.perform(put("/api/v1/spaces/{id}", testSpaceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSpace)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Space"));

        verify(spaceService).updateSpace(eq(testSpaceId), any(Space.class));
    }

    @Test
    void deleteSpace_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(spaceService).deleteSpace(testSpaceId);

        // When & Then
        mockMvc.perform(delete("/api/v1/spaces/{id}", testSpaceId))
                .andExpect(status().isNoContent());

        verify(spaceService).deleteSpace(testSpaceId);
    }
}

