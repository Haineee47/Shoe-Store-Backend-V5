package com.shoestore.shared.persistence.naming;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HibernatePhysicalNamingStrategyTest {

    private CamelCaseToUnderscoresNamingStrategy namingStrategy;

    @BeforeEach
    void setUp() {
        namingStrategy =
                new CamelCaseToUnderscoresNamingStrategy();
    }

    @Test
    void shouldConvertCamelCaseColumnNameToSnakeCase() {
        Identifier physicalName =
                namingStrategy.toPhysicalColumnName(
                        Identifier.toIdentifier("createdAt"),
                        null
                );

        assertThat(physicalName.getText())
                .isEqualTo("created_at");
    }

    @Test
    void shouldConvertPascalCaseTableNameToSnakeCase() {
        Identifier physicalName =
                namingStrategy.toPhysicalTableName(
                        Identifier.toIdentifier("ProductVariant"),
                        null
                );

        assertThat(physicalName.getText())
                .isEqualTo("product_variant");
    }

    @Test
    void shouldPreserveExistingSnakeCaseName() {
        Identifier physicalName =
                namingStrategy.toPhysicalTableName(
                        Identifier.toIdentifier("product_variants"),
                        null
                );

        assertThat(physicalName.getText())
                .isEqualTo("product_variants");
    }

    @Test
    void shouldConvertCompoundForeignKeyName() {
        Identifier physicalName =
                namingStrategy.toPhysicalColumnName(
                        Identifier.toIdentifier(
                                "shippingAddressId"
                        ),
                        null
                );

        assertThat(physicalName.getText())
                .isEqualTo("shipping_address_id");
    }
}
