package com.statesmartlife.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("STATE SMART LIFE — MO ODISHA SUPER APP API")
                        .version("1.0.0")
                        .description("Production-oriented, secure, scalable digital Super App REST API for Odisha.")
                        .contact(new Contact()
                                .name("Government of Odisha - Digital State Initiative")
                                .email("support@smartlife.odisha.gov.in"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .tags(java.util.List.of(
                        new io.swagger.v3.oas.models.tags.Tag().name("Auth").description("Authentication & Multi-Role RBAC"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Commerce").description("Stores & Vendor Management"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Orders").description("Cart & Order Management Engine"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Citizen Services").description("Public Grievances & Service Filings"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Notifications").description("In-App & Push Notifications"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Identity").description("Document Locker & Digital Identity Vault"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Payments").description("Digital Wallet & Double-Entry Ledger"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Logistics").description("Delivery Agent Assignment & Real-Time Tracking"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Community").description("Citizen Forums & Moderation Engine"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Healthcare").description("Hospitals, Doctors & Telehealth Appointments"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Emergency").description("SOS Alert Dispatch & Responder Operations"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Trust").description("Incident Dispute Tickets & Citizen Trust Score"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Tourism").description("Tour Guides, Profiles & Destination Discovery"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Transport").description("Mo Bus Routes & Smart Event Venues"),
                        new io.swagger.v3.oas.models.tags.Tag().name("Governance").description("State Overview & 30 District Analytics Portal")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer Token to authorize requests")));
    }
}
