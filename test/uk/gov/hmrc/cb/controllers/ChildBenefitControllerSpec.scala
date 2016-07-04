/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cb.controllers

import org.scalatest.mock.MockitoSugar
import play.api.mvc.Call
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.helpers.Assertions
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 29/06/2016.
 */
class ChildBenefitControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar with Assertions {

  "ChildBenefitController" should {

    object MockController extends ChildBenefitController {
      override val authConnector = mock[AuthConnector]
      override val cacheClient = mock[ChildBenefitKeystoreService]
    }

    "redirect to the initial controller" in {
      verifyEndpoint(MockController.initialController, "update-child-benefit")
    }

    "redirect to technical difficulties" in {
      verifyEndpoint(MockController.technicalDifficulties, "technical-difficulties")
    }

  }

}
