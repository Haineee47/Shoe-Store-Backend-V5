package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.command.ActivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.command.CreateTestProductWithIdCommand;
import com.shoestore.modules.architecturefixture.application.command.DeactivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.dto.ActivateTestProductResult;
import com.shoestore.modules.architecturefixture.application.dto.TestProductDetails;
import com.shoestore.modules.architecturefixture.application.query.FindTestProductByIdQuery;
import com.shoestore.modules.architecturefixture.application.service.ActivateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.service.CreateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.service.DeactivateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.service.FindTestProductByIdUseCase;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class ApplicationUseCaseSignatureConventionTest {

  @Test
  void activateUseCaseShouldAcceptCommandAndReturnResult() throws Exception {

    Method execute =
        ActivateTestProductUseCase.class.getDeclaredMethod(
            "execute", ActivateTestProductCommand.class);

    assertThat(execute.getReturnType()).isEqualTo(ActivateTestProductResult.class);
  }

  @Test
  void queryUseCaseShouldAcceptQueryAndReturnApplicationDto() throws Exception {

    Method execute =
        FindTestProductByIdUseCase.class.getDeclaredMethod(
            "execute", FindTestProductByIdQuery.class);

    assertThat(execute.getReturnType()).isEqualTo(TestProductDetails.class);
  }

  @Test
  void createUseCaseMayReturnTypedIdentifier() throws Exception {

    Method execute =
        CreateTestProductUseCase.class.getDeclaredMethod(
            "execute", CreateTestProductWithIdCommand.class);

    assertThat(execute.getReturnType()).isEqualTo(TestApplicationProductId.class);
  }

  @Test
  void useCaseMayReturnVoidWhenNoOutputIsRequired() throws Exception {

    Method execute =
        DeactivateTestProductUseCase.class.getDeclaredMethod(
            "execute", DeactivateTestProductCommand.class);

    assertThat(execute.getReturnType()).isEqualTo(void.class);
  }
}
