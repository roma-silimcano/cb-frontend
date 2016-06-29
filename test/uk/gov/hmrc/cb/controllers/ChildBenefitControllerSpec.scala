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
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 29/06/2016.
 */
class ChildBenefitControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  "ChildBenefitController" should {

    object MockController extends ChildBenefitController {
      override val authConnector = mock[AuthConnector]
    }

    "redirect to the initial controller" in {
      MockController.initialController shouldBe Call("GET", "/child-benefit/update-child-benefit")
    }

    "redirect to technical difficulties" in {
      MockController.technicalDifficulties shouldBe Call("GET", "/child-benefit/technical-difficulties")
    }

  }

}
