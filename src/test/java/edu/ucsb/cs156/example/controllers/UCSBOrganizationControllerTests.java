package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsRepository;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

    @MockBean
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for GET /api/ucsborganizations/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations?orgField=SKY"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

            // arrange

            UCSBOrganization organization = UCSBOrganization.builder()
                            .orgField("SKY")
                            .orgTranslationShort("SKYDIVING CLUB")
                            .orgTranslation("SKYDIVING CLUB AT UCSB")
                            .inactive(false)
                            .build();

            when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(organization));

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgField=SKY"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(ucsbOrganizationRepository, times(1)).findById(eq("SKY"));
            String expectedJson = mapper.writeValueAsString(organization);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(ucsbOrganizationRepository.findById(eq("WARRIORS"))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgField=WARRIORS"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(ucsbOrganizationRepository, times(1)).findById(eq("WARRIORS"));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("UCSBOrganization with id WARRIORS not found", json.get("message"));
    }

    // Authorization tests for POST /api/ucsborganizations/post

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsborganizations/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsborganizations/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_commons() throws Exception {
            // arrange

            UCSBOrganization studentLife = UCSBOrganization.builder()
                            .orgField("OSLI")
                            .orgTranslationShort("STUDENT LIFE")
                            .orgTranslation("OFFICE OF STUDENT LIFE")
                            .inactive(true)
                            .build();

            when(ucsbOrganizationRepository.save(eq(studentLife))).thenReturn(studentLife);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/ucsborganizations/post?orgField=OSLI&orgTranslationShort=STUDENT LIFE&orgTranslation=OFFICE OF STUDENT LIFE&inactive=true")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(ucsbOrganizationRepository, times(1)).save(studentLife);
            String expectedJson = mapper.writeValueAsString(studentLife);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganization skydiving = UCSBOrganization.builder()
                                .orgField("SKY")
                                .orgTranslationShort("SKYDIVING CLUB")
                                .orgTranslation("SKY DIVING CLUB AT UCSB")
                                .inactive(false)
                                .build();

                UCSBOrganization zpr = UCSBOrganization.builder()
                                .orgField("ZPR")
                                .orgTranslationShort("ZETA PHI RO")
                                .orgTranslation("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
                expectedOrganizations.addAll(Arrays.asList(skydiving, zpr));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_an_organization() throws Exception {
                // arrange

                UCSBOrganization organization = UCSBOrganization.builder()
                                .orgField("GSA")
                                .orgTranslationShort("GAUCHO SPORTS")
                                .orgTranslation("GAUCHO SPORTS ANALYTICS")
                                .build();
                                

                when(ucsbOrganizationRepository.findById(eq("GSA"))).thenReturn(Optional.of(organization));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?orgField=GSA")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("GSA");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id GSA deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existent_organization_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationRepository.findById(eq("WARRIORS"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?orgField=WARRIORS")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("WARRIORS");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id WARRIORS not found", json.get("message"));
        }
}
