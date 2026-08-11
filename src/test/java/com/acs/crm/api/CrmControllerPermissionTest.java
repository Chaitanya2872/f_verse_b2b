package com.acs.crm.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.acs.crm.service.CrmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(CrmControllerPermissionTest.TestConfig.class)
class CrmControllerPermissionTest {

    @Autowired private CrmController controller;
    @Autowired private CrmService crmService;

    @Test
    @WithMockUser(authorities = "page.b2b")
    void pagePermissionCanReadDeals() {
        controller.deals(null);

        verify(crmService).getDeals(null);
    }

    @Test
    @WithMockUser(authorities = "page.b2b")
    void pagePermissionCannotCreateDeals() {
        assertThatThrownBy(() -> controller.createDeal(new CreateDealRequest()))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(authorities = "feature.b2b.deals.manage")
    void dealManagementPermissionCanCreateDeals() {
        CreateDealRequest request = new CreateDealRequest();

        controller.createDeal(request);

        verify(crmService).createDeal(request);
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        CrmService crmService() {
            return mock(CrmService.class);
        }

        @Bean
        CrmController crmController(CrmService crmService) {
            return new CrmController(crmService);
        }
    }
}
