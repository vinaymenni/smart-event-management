package com.smartcampus.events;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin@campus.edu", roles = { "ADMIN" })
    public void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "student@campus.edu", roles = { "STUDENT" })
    public void testStudentDashboard() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
