package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.checkerframework.checker.units.qual.s;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

    @MockBean
    RecommendationRequestRepository recommendationRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for GET /api/recommendationrequest/all
    @Test
    public void logged_out_user_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/all"))
                        .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/all"))
                        .andExpect(status().is(200));
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/recommendationrequests?id=7"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    } 

    // Authorization tests for POST /api/recommendationrequest/post

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/recommendationrequests/post"))
                            .andExpect(status().is(403));
    }


    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/recommendationrequests/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

            // arrange
            LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

            RecommendationRequest request = RecommendationRequest.builder()
                            .requesterEmail("testRequesterEmail")
                            .professorEmail("testProfessorEmail")
                            .explanation("testExplanation")
                            .dateRequested(ldt)
                            .dateNeeded(ldt)
                            .done(true)
                            .build();

            when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(request));

            // act
            MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=7"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(recommendationRequestRepository, times(1)).findById(eq(7L));
            String expectedJson = mapper.writeValueAsString(request);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                verify(recommendationRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
        }

  
    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

            RecommendationRequest request1 = RecommendationRequest.builder()
                            .requesterEmail("testRequesterEmail1")
                            .professorEmail("testProfessorEmail1")
                            .explanation("testExplanation1")
                            .dateRequested(ldt1)
                            .dateNeeded(ldt1)
                            .done(true)
                            .build();

            when(recommendationRequestRepository.save(eq(request1))).thenReturn(request1);

            MvcResult response = mockMvc.perform(
                            post("/api/recommendationrequests/post?requesterEmail=testRequesterEmail1&professorEmail=testProfessorEmail1&explanation=testExplanation1&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-01-03T00:00:00&done=true")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            verify(recommendationRequestRepository, times(1)).save(request1);
            String expectedJson = mapper.writeValueAsString(request1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

            RecommendationRequest request1 = RecommendationRequest.builder()
                            .requesterEmail("testRequesterEmail1")
                            .professorEmail("testProfessorEmail1")
                            .explanation("testExplanation1")
                            .dateRequested(ldt1)
                            .dateNeeded(ldt1)
                            .done(false)
                            .build();

            LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

            RecommendationRequest request2 = RecommendationRequest.builder()
                            .requesterEmail("testRequesterEmail2")
                            .professorEmail("testProfessorEmail2")
                            .explanation("testExplanation2")
                            .dateRequested(ldt2)
                            .dateNeeded(ldt2)
                            .done(false)
                            .build();

            ArrayList<RecommendationRequest> expectedDates = new ArrayList<>();
            expectedDates.addAll(Arrays.asList(request1, request2));

            when(recommendationRequestRepository.findAll()).thenReturn(expectedDates);

            MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
                            .andExpect(status().isOk()).andReturn();


            verify(recommendationRequestRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedDates);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    // Test for PUT /api/recommendationrequests/

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
            // arrange

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
            LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

            RecommendationRequest requestOrig = RecommendationRequest.builder()
                            .requesterEmail("testRequesterEmail")
                            .professorEmail("testProfessorEmail")
                            .explanation("testExplanation")
                            .dateRequested(ldt1)
                            .dateNeeded(ldt1)
                            .done(false)
                            .build();

            RecommendationRequest requestEdited = RecommendationRequest.builder()
                            .requesterEmail("testRequesterEmail1")
                            .professorEmail("testProfessorEmail1")
                            .explanation("testExplanation1")
                            .dateRequested(ldt2)
                            .dateNeeded(ldt2)
                            .done(true)
                            .build();

            String requestBody = mapper.writeValueAsString(requestEdited);

            when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(requestOrig));

            MvcResult response = mockMvc.perform(
                            put("/api/recommendationrequests?id=67")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            verify(recommendationRequestRepository, times(1)).findById(67L);
            verify(recommendationRequestRepository, times(1)).save(requestEdited); // should be saved with correct user
            String responseString = response.getResponse().getContentAsString();
            assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

            RecommendationRequest requestEdited = RecommendationRequest.builder()
                            .requesterEmail("testRequesterEmailEdited")
                            .professorEmail("testProfessorEmailEdited")
                            .explanation("testExplanationEdited")
                            .dateRequested(ldt1)
                            .dateNeeded(ldt1)
                            .done(true)
                            .build();

            String requestBody = mapper.writeValueAsString(requestEdited);

            when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/recommendationrequests?id=67")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(recommendationRequestRepository, times(1)).findById(67L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_recommendationrequest() throws Exception {
            // arrange

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

            RecommendationRequest request1 = RecommendationRequest.builder()
                            .requesterEmail("testRequesterEmail1")
                            .professorEmail("testProfessorEmail1")
                            .explanation("testExplanation1")
                            .dateRequested(ldt1)
                            .dateNeeded(ldt1)
                            .done(false)
                            .build();

            when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(request1));

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/recommendationrequests?id=15")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(recommendationRequestRepository, times(1)).findById(15L);
            verify(recommendationRequestRepository, times(1)).delete(any());

            Map<String, Object> json = responseToJson(response);
            assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_recommendationrequest_and_gets_right_error_message()
                    throws Exception {
            // arrange

            when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/recommendationrequests?id=15")
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(recommendationRequestRepository, times(1)).findById(15L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
    }
}